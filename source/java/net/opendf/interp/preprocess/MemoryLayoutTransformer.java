package net.opendf.interp.preprocess;

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
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.Statement;
import net.opendf.ir.common.StmtBlock;
import net.opendf.ir.common.StmtForeach;
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
AbstractActorMachineTransformer<Stack<String>> {

	private void error(DeclVar decl, String msg) {
		System.err.println(msg);
		throw new RuntimeException(msg);
	}

	public ActorMachine transformActorMachine(ActorMachine actorMachine){
		Stack<String> stack = new Stack<String>();
		ActorMachine a = transformActorMachine(actorMachine, stack);
		assert stack.isEmpty();
		return a;
	}
	
	@Override
	public Variable transformVariable(Variable var, Stack<String> stack) {
		if(!var.hasLocation()){
			int offset = stack.search(var.getName()) -  1;  // in search(), top of stack = 1
			if(offset >= 0){
				return var.copy(var.getName(), 0, offset, true);
			}
		}
		return super.transformVariable(var, stack);
	}

	@Override
	public Expression visitExprApplication(ExprApplication expr, Stack<String> stack) {
		Expression function = expr.getFunction();
		ImmutableList.Builder<Expression> builder = new ImmutableList.Builder<Expression>();
		for(Expression arg : expr.getArgs()){
			builder.add(transformExpression(arg, stack));
			stack.push("");  //the arguments are added to the stack one by one
		}
		for(int i=expr.getArgs().size(); i>0; i--){
			stack.pop();
		}
		return expr.copy(transformExpression(function, stack), builder.build());
	}
	
	@Override
	public Expression visitExprLet(ExprLet let, Stack<String> stack) {
		ImmutableList.Builder<DeclVar> builder = ImmutableList.builder();

		for (DeclVar decl : let.getVarDecls()) {
			builder.add(transformVarDecl(decl, stack));
			if(decl.getInitialValue() == null){
				error(decl, "local variable " + decl.getName() + " in let expression do not have any initialization");
			}
			stack.push(decl.getName());
		}
		Expression body = let.getBody().accept(this, stack);
		for (int i=let.getVarDecls().size(); i>0; i--) {
			stack.pop();
		}
		return  let.copy(transformTypeDecls(let.getTypeDecls(), stack), 
				         builder.build(), 
				         body);
	}

	@Override
	public Expression visitExprLambda(ExprLambda lambda, Stack<String> stack) {
		//FIXME, closure
		Stack<String> frame = new Stack<String>();
		for (ParDeclValue par : lambda.getValueParameters()) {
			frame.push(par.getName());
		}
		return super.visitExprLambda(lambda, frame);
	}

	@Override
	public Expression visitExprList(final ExprList expr, final Stack<String> stack) {
		final ImmutableList.Builder<Expression> elementBuilder = ImmutableList.builder();
		Runnable buildList = new Runnable() {
			public void run() {
				for(Expression element : expr.getElements()){
					elementBuilder.add(transformExpression(element, stack));
				}
			}
		};
		ImmutableList<GeneratorFilter> transformedGenerators = GeneratorFilterHelper.memoryLayout(expr.getGenerators(), buildList, stack, this);
		return expr.copy(elementBuilder.build(), transformedGenerators);
	}

	@Override
	public Expression visitExprProc(ExprProc proc, Stack<String> stack) {
		//FIXME, closure
		Stack<String> frame = new Stack<String>();
		for (ParDeclValue par : proc.getValueParameters()) {
			frame.push(par.getName());
		}
		return super.visitExprProc(proc, frame);
	}

	@Override
	public Statement visitStmtBlock(StmtBlock block, Stack<String> stack) {
		ImmutableList.Builder<DeclVar> builderVar = ImmutableList.builder();
		for (DeclVar decl : block.getVarDecls()) {
			builderVar.add(transformVarDecl(decl, stack));
			stack.push(decl.getName());
		}
		ImmutableList<Statement> stmts = transformStatements(block.getStatements(), stack);
		for(int i=block.getVarDecls().size(); i>0; i--){
			stack.pop();
		}
		return block.copy(transformTypeDecls(block.getTypeDecls(), stack), 
				          builderVar.build(), stmts);
	}
	
	@Override
	public Statement visitStmtForeach(final StmtForeach stmt, final Stack<String> stack) {
		final Statement[] body = new Statement[1];
		Runnable transformer = new Runnable() {
			public void run() {
				body[0] = transformStatement(stmt.getBody(), stack);
			}
		};
		ImmutableList<GeneratorFilter> transformedGenerators = GeneratorFilterHelper.memoryLayout(stmt.getGenerators(), transformer, stack, this);
		return stmt.copy(transformedGenerators, body[0]);
	}
}
