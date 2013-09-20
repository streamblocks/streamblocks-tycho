package net.opendf.interp.preprocess;

import java.util.ArrayList;
import java.util.Stack;

import net.opendf.interp.GeneratorFilterHelper;
import net.opendf.ir.am.ActorMachine;
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
import net.opendf.ir.util.ImmutableList;
import net.opendf.transform.util.AbstractActorMachineTransformer;

/**
 * Compute the stack offset for all stack accesses on the stack.
 * The IR must be traversed in the exact same order as the interpreter.
 * 
 * @author pera
 */
public class MemoryLayoutTransformer extends
AbstractActorMachineTransformer<MemoryLayoutTransformer.LookupTable> {

	private void error(DeclVar decl, String msg) {
		System.err.println(msg);
		throw new RuntimeException(msg);
	}

	public ActorMachine transformActorMachine(ActorMachine actorMachine){
		LookupTable table = new LookupTable();
		ActorMachine a = transformActorMachine(actorMachine, table);
		assert table.isEmpty();
		return a;
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
		ImmutableList<GeneratorFilter> transformedGenerators = GeneratorFilterHelper.memoryLayout(expr.getGenerators(), buildList, table, this);
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
		final Statement[] body = new Statement[1];
		Runnable transformer = new Runnable() {
			public void run() {
				body[0] = transformStatement(stmt.getBody(), table);
			}
		};
		ImmutableList<GeneratorFilter> transformedGenerators = GeneratorFilterHelper.memoryLayout(stmt.getGenerators(), transformer, table, this);
		return stmt.copy(transformedGenerators, body[0]);
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
			for(pos=0; pos<closure.size(); pos++){
				if(closure.get(pos).getName().equals(var.getName())){
					assert closure.get(pos).equals(var);
					return pos;
				}
			}
			pos = closure.size();
			closure.add(var);
			return pos;
		}

		public Variable lookup(Variable var){
			String name = var.getName();
			if(parent == null){
				// outside lambda/procedure expression, do not create closure
				// look for the name inside this lambda/process block
				int offset = stack.search(name) -  1;  // in search(), top of stack = 1
				if(offset>=0){
					return var.copy(name, 0, offset, true);
				}
				return var;
			} else {
				// inside lambda/procedure expression, do not create closure
				LookupTable frame = parent;
				// is var in the static scope?
				//TODO, this assumes a correct static scope labeling
				if(var.hasLocation()){
					int colosureOffset = addToClosure(var);
					return var.copy(name, 0, colosureOffset, false);
				}
				// look for the name inside this lambda/process block
				int offset = stack.search(name) -  1;  // in search(), top of stack = 1
				if(offset>=0){
					return var.copy(name, 0, offset, true);
				}
				// the name was not declared within the closest lambda/proc expression. search for it in the closure
				int scopeDist = 0;                     // the distance to the variable on the stack when the closure is created
				while(frame!=null){
					offset = frame.stack.search(name) -  1;  // in search(), top of stack = 1
					if(offset<0){
						// name was not here, continue to the parent scope
						scopeDist += frame.stack.size();
					} else {
						int colosureOffset = addToClosure(var.copy(name, 0, offset + scopeDist, true)); // pos = offset + innerScopeDist
						return var.copy(name, 0, colosureOffset, false);
					}
					frame = frame.parent;
				}
			}
			return var;
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
}
