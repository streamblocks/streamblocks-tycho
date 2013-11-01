package net.opendf.interp.preprocess;

import java.util.ArrayList;
import java.util.Stack;

import net.opendf.errorhandling.ErrorModule;
import net.opendf.interp.GeneratorFilterHelper;
import net.opendf.interp.VariableLocation;
import net.opendf.interp.exception.CALCompiletimeException;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Scope;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprApplication;
import net.opendf.ir.common.ExprLambda;
import net.opendf.ir.common.ExprLet;
import net.opendf.ir.common.ExprList;
import net.opendf.ir.common.ExprMap;
import net.opendf.ir.common.ExprProc;
import net.opendf.ir.common.ExprSet;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.GeneratorFilter;
import net.opendf.ir.common.NamespaceDecl;
import net.opendf.ir.common.ParDeclType;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.Statement;
import net.opendf.ir.common.StmtBlock;
import net.opendf.ir.common.StmtForeach;
import net.opendf.ir.common.TypeExpr;
import net.opendf.ir.common.Variable;
import net.opendf.ir.net.ast.EntityExpr;
import net.opendf.ir.net.ast.EntityListExpr;
import net.opendf.ir.net.ast.NetworkDefinition;
import net.opendf.ir.net.ast.StructureForeachStmt;
import net.opendf.ir.net.ast.StructureStatement;
import net.opendf.ir.util.ImmutableEntry;
import net.opendf.ir.util.ImmutableList;
import net.opendf.parser.SourceCodeOracle;
import net.opendf.transform.util.ActorMachineTransformerWrapper;
import net.opendf.transform.util.ErrorAwareBasicTransformer;
import net.opendf.transform.util.NetworkDefinitionTransformerWrapper;

/**
 * Compute the offset for all variable accesses.
 * All Variable objects are replaced with VariableLocation objects.
 * 
 * To ensure that the stack have a correct content the IR must be traversed in 
 * the exact same order as the interpreter do, pushing names/values to the 
 * stack in the same order.
 * 
 * In an {@link ActorMachine} all global and action scope variables have the 
 * scopeId set. Only offset is computed.
 * 
 * In a {@link Network} there are two global scopes:
 * scopeId 0 : global variables
 * scopeId 1 : parameters
 * 
 * Semantic Checks:
 * - report error if a name i used that is not declared (variable/parameter).
 * - report an error if a local variable in a let expression do not have an initialization expression.
 * 
 * Prerequisites:
 * - for {@link ActorMachine}s the scopeId is assumed to be correct and set for all {@link Variable}s in a scope.
 * - free variables of lambda and procedures must be computed before, {@link VariableInitOrderTransformer}, 
 *   else the offsets for initializing the lambda/procedure scope is not computed.
 * 
 * @author pera
 */
public class VariableOffsetTransformer extends ErrorAwareBasicTransformer<VariableOffsetTransformer.LookupTable> implements ErrorModule {
	public VariableOffsetTransformer(SourceCodeOracle sourceOracle) {
		super(sourceOracle);
	}

	public static final int NetworkParamScopeId = 0;  // global variables may depend on parameters. Ensure parameters are initialized first by giving it a lower id.
	public static final int NetworkGlobalScopeId = 1;  


	/**
	 * 
	 * globalScopeId - scope in which names for the network/actor global variables are located. Use -1 not to search for global variables, i.e. when transforming {@link ActorMachines}
	 * paramScopeId - scope in which names for the network/actor parameters are located. Use -1 not to search for parameters
	 */
	private int globalScopeId = -1;
	private int paramScopeId = -1;

	ImmutableList<Scope> scopes;

	//--- wrappers ------------------------------------------------------------
	/**
	 * Replace all Variable objects with VariableLocation objects.
	 * @param net
	 * @return
	 * @throws CALCompiletimeException if an error occurs
	 */
	public static NetworkDefinition transformNetworkDefinition(NetworkDefinition net, SourceCodeOracle sourceOracle) throws CALCompiletimeException {
		VariableOffsetTransformer transformer = new VariableOffsetTransformer(sourceOracle);
		NetDefVarOffsetTransformer wrapper = transformer.new NetDefVarOffsetTransformer(transformer);

		LookupTable table = transformer.new LookupTable();
		net = wrapper.transformNetworkDefinition(net, table);
		transformer.printWarnings();
		transformer.abortIfError();
		return net;
	}

	/**
	 * Replace all Variable objects with VariableLocation objects.
	 * @param net
	 * @return
	 * @throws CALCompiletimeException if an error occurs
	 */
	public static ActorMachine transformActorMachine(ActorMachine actorMachine, SourceCodeOracle sourceOracle) throws CALCompiletimeException {
		VariableOffsetTransformer transformer = new VariableOffsetTransformer(sourceOracle);
		ActorMachineTransformerWrapper<VariableOffsetTransformer.LookupTable> wrapper = new ActorMachineTransformerWrapper<VariableOffsetTransformer.LookupTable>(transformer);

		transformer.scopes = actorMachine.getScopes();
		LookupTable table = transformer.new LookupTable();
		actorMachine = wrapper.transformActorMachine(actorMachine, table);
		assert table.isEmpty();
		transformer.printWarnings();
		transformer.abortIfError();
		return actorMachine;
	}
	
	//--- transformations -----------------------------------------------------
	@Override
	public Variable transformVariable(Variable var, LookupTable table) {
		return table.lookup(var);
	}

	@Override
	public Expression visitExprApplication(ExprApplication expr, LookupTable table) {
		Expression function = expr.getFunction();
		ImmutableList.Builder<Expression> builder = new ImmutableList.Builder<Expression>();
		for(Expression arg : expr.getArgs()){
			builder.add(transformExpression(arg, table));
			table.addName("");  //the arguments are added to the stack one by one
		}
		for(int i=expr.getArgs().size(); i>0; i--){
			table.pop();
		}
		return expr.copy(transformExpression(function, table), builder.build());
	}
	
	@Override
	public Expression visitExprLet(ExprLet let, LookupTable table) {
		ImmutableList.Builder<DeclVar> builder = ImmutableList.builder();

		for (DeclVar decl : let.getVarDecls()) {
			builder.add(transformVarDecl(decl, table));
			if(decl.getInitialValue() == null){
				error("local variable " + decl.getName() + " in let expression do not have any initialization", decl);
			}
			table.addName(decl.getName());
		}
		Expression body = let.getBody().accept(this, table);
		for (int i=let.getVarDecls().size(); i>0; i--) {
			table.pop();
		}
		return  let.copy(transformTypeDecls(let.getTypeDecls(), table), 
				         builder.build(), 
				         body);
	}

	@Override
	public Expression visitExprLambda(ExprLambda lambda, LookupTable table) {
		ImmutableList<ParDeclType> typeParameters = transformTypeParameters(lambda.getTypeParameters(), table);
		ImmutableList<ParDeclValue> valueParameters = transformValueParameters(lambda.getValueParameters(), table);

		LookupTable frame = table.startClosure();		
		for (ParDeclValue par : lambda.getValueParameters()) {
			frame.addName(par.getName());
		}		
		Expression body = transformExpression(lambda.getBody(), frame);
		
		TypeExpr returnTypeExpr = transformTypeExpr(lambda.getReturnType(), table);

		return lambda.copy(typeParameters, valueParameters, body, returnTypeExpr, ImmutableList.copyOf(frame.getClosure()), true);
	}

	@Override
	public Expression visitExprList(final ExprList expr, final LookupTable table) {
		final ImmutableList.Builder<Expression> elementBuilder = ImmutableList.builder();
		Runnable buildList = new Runnable() {
			public void run() {
				for(Expression element : expr.getElements()){
					elementBuilder.add(transformExpression(element, table));
				}
			}
		};
		ImmutableList<GeneratorFilter> transformedGenerators = GeneratorFilterHelper.setVariableOffsets(expr.getGenerators(), buildList, table, this);
		return expr.copy(elementBuilder.build(), transformedGenerators);
	}

	@Override
	public Expression visitExprMap(final ExprMap expr, final LookupTable table) {
		final ImmutableList.Builder<ImmutableEntry<Expression, Expression>> elementBuilder = ImmutableList.builder();
		Runnable buildList = new Runnable() {
			public void run() {
				for(ImmutableEntry<Expression, Expression> element : expr.getMappings()){
					Expression key = transformExpression(element.getKey(), table);
					Expression value = transformExpression(element.getValue(), table);
					elementBuilder.add(new ImmutableEntry<Expression, Expression>(key, value));
				}
			}
		};
		ImmutableList<GeneratorFilter> transformedGenerators = GeneratorFilterHelper.setVariableOffsets(expr.getGenerators(), buildList, table, this);
		return expr.copy(elementBuilder.build(), transformedGenerators);
	}
	
	@Override
	public Expression visitExprSet(final ExprSet expr, final LookupTable table) {
		final ImmutableList.Builder<Expression> elementBuilder = ImmutableList.builder();
		Runnable buildList = new Runnable() {
			public void run() {
				for(Expression element : expr.getElements()){
					Expression value = transformExpression(element, table);
					// TODO remove duplicates from the set
					elementBuilder.add(value);
				}
			}
		};
		ImmutableList<GeneratorFilter> transformedGenerators = GeneratorFilterHelper.setVariableOffsets(expr.getGenerators(), buildList, table, this);
		return expr.copy(elementBuilder.build(), transformedGenerators);
	}

	@Override
	public Expression visitExprProc(ExprProc proc, LookupTable table) {
		ImmutableList<ParDeclType> typeParameters = transformTypeParameters(proc.getTypeParameters(), table);
		ImmutableList<ParDeclValue> valueParameters = transformValueParameters(proc.getValueParameters(), table);

		LookupTable frame = table.startClosure();
		for (ParDeclValue par : proc.getValueParameters()) {
			frame.addName(par.getName());
		}
		Statement body = transformStatement(proc.getBody(), frame);
		return proc.copy(typeParameters, valueParameters, body, ImmutableList.copyOf(frame.getClosure()), true);
	}

	@Override
	public Statement visitStmtBlock(StmtBlock block, LookupTable table) {
		ImmutableList.Builder<DeclVar> builderVar = ImmutableList.builder();
		for (DeclVar decl : block.getVarDecls()) {
			builderVar.add(transformVarDecl(decl, table));
			table.addName(decl.getName());
		}
		ImmutableList<Statement> stmts = transformStatements(block.getStatements(), table);
		for(int i=block.getVarDecls().size(); i>0; i--){
			table.pop();
		}
		return block.copy(transformTypeDecls(block.getTypeDecls(), table), 
				          builderVar.build(), stmts);
	}
	
	@Override
	public Statement visitStmtForeach(final StmtForeach stmt, final LookupTable table) {
		final Statement[] newBody = new Statement[1];  // newBody must be final for Runnable, use a pointer so Runnable can change it.
		Runnable transformer = new Runnable() {
			public void run() {
				newBody[0] = transformStatement(stmt.getBody(), table);
			}
		};
		ImmutableList<GeneratorFilter> transformedGenerators = GeneratorFilterHelper.setVariableOffsets(stmt.getGenerators(), transformer, table, this);
		return stmt.copy(transformedGenerators, newBody[0]);
	}
	
	public class LookupTable{
		private LookupTable parent;
		private Stack<String> stack = new Stack<String>();
		private ArrayList<Variable> closure = new ArrayList<Variable>();

		public LookupTable(){
			this.parent = null;
		}

		public ArrayList<Variable> getClosure() {
			return closure;
		}

		private LookupTable(LookupTable outerClosure){
			this.parent = outerClosure;
		}

		public void addName(String name){
			stack.push(name);
		}
		
		public void pop(){
			stack.pop();
		}

		private int addToClosure(Variable var){
			int pos;
			// check if this name already is in the closure
			for(pos=0; pos<closure.size(); pos++){
				if(closure.get(pos).getName().equals(var.getName())){
					assert closure.get(pos).equals(var);
					return pos;
				}
			}
			// add a new name to the closure
			pos = closure.size();
			//TODO the closure must have VaraibleLocation objects
			closure.add(var);
			return pos;
		}

		public Variable lookup(Variable var){
			String name = var.getName();
			if(parent == null){
				// outside lambda/procedure expression, do not create closure
				if(var.isScopeVariable()){
					VariableLocation result = searchScope(var.getScopeId(), var);
					if(result != null){
						return result;
					}
					// This should not happen. The actor machine translator has given a scopeId to a variable, but the scope do not declare the name.
					throw new RuntimeException("unknown scope variable: " + name);

				} else {
					// look for the name inside this lambda/process block and static scopes
					int offset = stack.search(name) -  1;  // in search(), top of stack = 1
					if(offset>=0){
						return VariableLocation.stackVariable(var, name, offset);
					}
				}
				if(globalScopeId>=0){
					VariableLocation result = searchScope(globalScopeId, var);
					if(result != null){
						return result;
					}
				}
				if(paramScopeId>=0){
					VariableLocation result = searchScope(paramScopeId, var);
					if(result != null){
						return result;
					}
				}
				error("unknown variable name " + name, var);
				return var;
			} else {
				// inside lambda/procedure expression
				// is var in the static scope? Then we do not search the stack for the name.
				if(var.isScopeVariable()){
					// all variables in a scope is part of the closure, add it
					VariableLocation closureVar = searchScope(var.getScopeId(), var);
					if(closureVar == null){
						error("unknown variable name " + name, var);
						return var;
					}
					int colosureOffset = addToClosure(closureVar);
					// the closure has scopeId 0
					return VariableLocation.scopeVariable(var, name, 0, colosureOffset);
				}
				// the variable is not known to be in a scope, look for it on the stack
				LookupTable frame = parent;
				// look for the name inside this lambda/process block. The parameters are on the stack.
				int stackOffset = stack.search(name) -  1;  // in search(), top of stack = 1
				if(stackOffset>=0){
					// parameters and locally declared variables is not part of a closure. 
					return VariableLocation.stackVariable(var, name, stackOffset);
				}
				// the name was not declared within the closest lambda/proc expression. Search for it in the closure
				int scopeDist = 0;                     // the distance to the variable on the stack when the closure is created
				while(frame!=null){
					stackOffset = frame.stack.search(name) -  1;  // in search(), top of stack = 1
					if(stackOffset<0){
						// name was not here, continue to the parent scope
						scopeDist += frame.stack.size();
					} else {
						int colosureOffset = addToClosure(VariableLocation.stackVariable(var, name, stackOffset + scopeDist)); // pos = offset + innerScopeDist
						return VariableLocation.scopeVariable(var, name, 0, colosureOffset);
					}
					frame = frame.parent;
				}
				// For Networks, search the global and parameter names to
				if(globalScopeId>=0){
					VariableLocation closureVar = searchScope(globalScopeId, var);
					if(closureVar != null){
						int colosureOffset = addToClosure(closureVar);
						// the closure has scopeId 0
						return VariableLocation.scopeVariable(var, name, 0, colosureOffset);
					}
				}
				if(paramScopeId>=0){
					VariableLocation closureVar = searchScope(paramScopeId, var);
					if(closureVar != null){
						int colosureOffset = addToClosure(closureVar);
						// the closure has scopeId 0
						return VariableLocation.scopeVariable(var, name, 0, colosureOffset);
					}
				}
			}
			error("unknown variable name " + name, var);
			return var;
		}

		private VariableLocation searchScope(int scopeId, Variable var){
			String name = var.getName();
			ImmutableList<DeclVar> declList = scopes.get(scopeId).getDeclarations();
			for(int i=0; i<declList.size(); i++){
				DeclVar v = declList.get(i);
				if(v.getName().equals(name)){
					return VariableLocation.scopeVariable(var, name, scopeId, i);
				}
			}
			return null;
		}

		public boolean isEmpty(){
			return stack.isEmpty();
		}

		public LookupTable startClosure(){
			return new LookupTable(this);
		}
		public LookupTable endClosure(){
			return parent;
		}
	}
	
	public class NetDefVarOffsetTransformer extends NetworkDefinitionTransformerWrapper<VariableOffsetTransformer.LookupTable>{

		public NetDefVarOffsetTransformer(SourceCodeOracle sourceOracle) {
			super(new VariableOffsetTransformer(sourceOracle));
		}
		public NetDefVarOffsetTransformer(VariableOffsetTransformer innerT) {
			super(innerT);
		}

		@Override
		public NetworkDefinition transformNetworkDefinition(NetworkDefinition net, LookupTable table){
			globalScopeId = NetworkGlobalScopeId;
			paramScopeId = NetworkParamScopeId;
			Scope globalScope[] = new Scope[Math.max(paramScopeId, globalScopeId)+1];
			globalScope[globalScopeId] = new Scope(net.getVarDecls());
			//FIXME this is a bit over engineered. The generated list of declarations is not saved.
			globalScope[paramScopeId] = buildParamScope(net.getValueParameters());
			scopes = ImmutableList.copyOf(globalScope);
			net = super.transformNetworkDefinition(net, table);
			scopes = null;
			assert table.isEmpty();
			return net;
		}

		private Scope buildParamScope(ImmutableList<ParDeclValue> valueParameters) {
			ImmutableList.Builder<DeclVar> builder = new ImmutableList.Builder<DeclVar>();
			for(ParDeclValue par : valueParameters){
				//TODO create a proper NamespaceDecl
				NamespaceDecl ns = null;
				builder.add(new DeclVar(par, par.getType(), par.getName(), ns, null, false));
			}
			return new Scope(builder.build());
		}

		@Override
		public EntityExpr visitEntityListExpr(final EntityListExpr e, final LookupTable table) {
			final ImmutableList.Builder<EntityExpr> builder = new ImmutableList.Builder<EntityExpr>();
			final NetDefVarOffsetTransformer entityExprTransformer = this;
			
			Runnable transformer = new Runnable() {
				public void run() {
					for(EntityExpr expr : e.getEntityList()){
						builder.add(expr.accept(entityExprTransformer, table));
					}
				}
			};
			ImmutableList<GeneratorFilter> transformedGenerators = GeneratorFilterHelper.setVariableOffsets(e.getGenerators(), transformer, table, (VariableOffsetTransformer)inner);
			return e.copy(builder.build(), transformedGenerators);
		}
		@Override
		public StructureStatement visitStructureForeachStmt(final StructureForeachStmt stmt, final LookupTable table) {
			final ImmutableList.Builder<StructureStatement> builder = new ImmutableList.Builder<StructureStatement>();
			final NetDefVarOffsetTransformer structStmtTransformer = this;
			
			Runnable transformer = new Runnable() {
				public void run() {
					for(StructureStatement s : stmt.getStatements()){
						builder.add(s.accept(structStmtTransformer, table));
					}
				}
			};
			ImmutableList<GeneratorFilter> transformedGenerators = GeneratorFilterHelper.setVariableOffsets(stmt.getGenerators(), transformer, table, (VariableOffsetTransformer)inner);
			return stmt.copy(transformedGenerators, builder.build());
		}
	}
}
