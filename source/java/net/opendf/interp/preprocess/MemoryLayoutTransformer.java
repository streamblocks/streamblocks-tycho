package net.opendf.interp.preprocess;

import java.util.Stack;

import net.opendf.interp.Environment;
import net.opendf.interp.values.BasicList;
import net.opendf.interp.values.RefView;
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
			if(decl.getInitialValue() != null){
				builder.add(transformVarDecl(decl, stack));
			} else{
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
	public Expression visitExprList(ExprList expr, Stack<String> stack) {
		ImmutableList.Builder<GeneratorFilter> generatorBuilder = ImmutableList.builder();
		for(GeneratorFilter gen : expr.getGenerators()){
			// first evaluate all values this generator should iterate over. At this point the local names are not visible
			Expression collectionExpr = transformExpression(gen.getCollectionExpr(), stack);
			// introduce all local names by pushing the values to the stack
			for(DeclVar decl : gen.getVariables()){
				assert decl.getInitialValue() == null;  // initial value is not allowed, the generator creates the values
				stack.push(decl.getName());
			}
			// evaluate the filters
			ImmutableList.Builder<Expression> filterBuilder = ImmutableList.builder();
			for(Expression filter : gen.getFilters()){
				filterBuilder.add(transformExpression(filter, stack));
			}
			// now all offsets for named accesses in this generator has been computed
			generatorBuilder.add(gen.copy(gen.getVariables(), collectionExpr, filterBuilder.build()));
		}
		ImmutableList.Builder<Expression> elementBuilder = ImmutableList.builder();			
		for(Expression element : expr.getElements()){
			elementBuilder.add(transformExpression(element, stack));
		}
		// pop the local names from the stack
		for(GeneratorFilter gen : expr.getGenerators()){
			for(int i=gen.getVariables().size(); i>0; i--){
				stack.pop();
			}
		}

		return expr.copy(elementBuilder.build(), generatorBuilder.build());
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
			if(decl.getInitialValue() != null){
				builderVar.add(transformVarDecl(decl, stack));
			}
			stack.push(decl.getName());
		}
		return block.copy(transformTypeDecls(block.getTypeDecls(), stack), 
				          builderVar.build(),
				          transformStatements(block.getStatements(), stack));
	}
}
