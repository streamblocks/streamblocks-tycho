package net.opendf.interp.preprocess;

import java.util.ArrayList;
import java.util.Stack;

import net.opendf.interp.Environment;
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
import net.opendf.ir.common.ExprProc;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.GeneratorFilter;
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
import net.opendf.ir.util.ImmutableList;
import net.opendf.transform.util.AbstractBasicTransformer;
import net.opendf.transform.util.ActorMachineTransformerWrapper;
import net.opendf.transform.util.NetworkDefinitionTransformerWrapper;

/**
 * Compute the offset for all variable accesses.
 * All Variable objects are replaced with VariableLocation objects
 * The IR must be traversed in the exact same order as the interpreter.
 * 
 * @author pera
 */
public class VariableOffsetTransformer extends AbstractBasicTransformer<VariableOffsetTransformer.LookupTable> {

	ImmutableList<Scope> scopes;
	
	private void error(DeclVar decl, String msg) {
		System.err.println(msg);
		throw new RuntimeException(msg);
	}

	public NetworkDefinition transformNetworkDefinition(NetworkDefinition net){
		NetDefVarOffsetTransformer wrapper = new NetDefVarOffsetTransformer();

		LookupTable table = new LookupTable();
		net = wrapper.transformNetworkDefinition(net, table);
		return net;
	}

	public ActorMachine transformActorMachine(ActorMachine actorMachine){
		scopes = actorMachine.getScopes();
		LookupTable table = new LookupTable();
		VariableOffsetTransformer transformer = new VariableOffsetTransformer();
		ActorMachineTransformerWrapper<VariableOffsetTransformer.LookupTable> wrapper = new ActorMachineTransformerWrapper<VariableOffsetTransformer.LookupTable>(transformer);
		actorMachine = wrapper.transformActorMachine(actorMachine, table);
		assert table.isEmpty();
		return actorMachine;
	}
	
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
				error(decl, "local variable " + decl.getName() + " in let expression do not have any initialization");
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

		public VariableLocation lookup(Variable var){
			String name = var.getName();
			if(parent == null){
				// outside lambda/procedure expression, do not create closure
				if(var.isScopeVariable()){
					Scope scope = scopes.get(var.getScopeId());
					ImmutableList<DeclVar> list = scope.getDeclarations();
					for(int i=0; i<list.size(); i++){
						DeclVar decl = list.get(i);
						if(decl.getName().equals(name)){
							return VariableLocation.scopeVariable(var, name, var.getScopeId(), i);
						}
					}
				} else {
					// look for the name inside this lambda/process block and static scopes
					int offset = stack.search(name) -  1;  // in search(), top of stack = 1
					if(offset>=0){
						return VariableLocation.stackVariable(var, name, offset);
					}
				}
				//TODO this is a quick fix for network definitions
				// In network definitions scope variables are not marked, i.e. variable.isScopeVariable==false for all variable accesses
				ImmutableList<DeclVar> declList = scopes.get(0).getDeclarations();
				for(int i=0; i<declList.size(); i++){
					DeclVar v = declList.get(i);
					if(v.getName().equals(name)){
						return VariableLocation.scopeVariable(var, name, 0, i);
					}
				}
				
				throw new CALCompiletimeException("unknown variable name " + name);
			} else {
				// inside lambda/procedure expression
				LookupTable frame = parent;
				// is var in the static scope? Then we do not search the stack for the name.
				if(var.isScopeVariable()){
					// add the variable to the closure
					int colosureOffset = addToClosure(var);
					// the closure has scopeId 0
					return VariableLocation.scopeVariable(var, name, 0, colosureOffset);
				}
				// look for the name inside this lambda/process block
				int stackOffset = stack.search(name) -  1;  // in search(), top of stack = 1
				if(stackOffset>=0){
					return VariableLocation.stackVariable(var, name, stackOffset);
				}
				// the name was not declared within the closest lambda/proc expression. search for it in the closure
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
			}
			throw new CALCompiletimeException("unknown variable name " + name);
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

		public NetDefVarOffsetTransformer() {
			super(new VariableOffsetTransformer());
		}

		@Override
		public NetworkDefinition transformNetworkDefinition(NetworkDefinition net, LookupTable table){
			Scope scope = new Scope(net.getVarDecls());
			scopes = ImmutableList.of(scope);
			net = super.transformNetworkDefinition(net, table);
			scopes = null;
			assert table.isEmpty();
			return net;
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
