package net.opendf.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.opendf.ir.cal.*;
import net.opendf.ir.common.*;
import net.opendf.ir.net.ToolAttribute;
import net.opendf.ir.net.ast.EntityExpr;
import net.opendf.ir.net.ast.EntityExprVisitor;
import net.opendf.ir.net.ast.EntityIfExpr;
import net.opendf.ir.net.ast.EntityInstanceExpr;
import net.opendf.ir.net.ast.EntityListExpr;
import net.opendf.ir.net.ast.NetworkDefinition;
import net.opendf.ir.net.ast.PortReference;
import net.opendf.ir.net.ast.StructureConnectionStmt;
import net.opendf.ir.net.ast.StructureForeachStmt;
import net.opendf.ir.net.ast.StructureIfStmt;
import net.opendf.ir.net.ast.StructureStatement;
import net.opendf.ir.net.ast.StructureStmtVisitor;

public class PrettyPrint implements ExpressionVisitor<Void,Void>, StatementVisitor<Void, Void>, EntityExprVisitor<Void, Void>, StructureStmtVisitor<Void, Void>, LValueVisitor<Void, Void> {
	private java.io.PrintStream out = System.out;
	private int indentDepth = 0;

	public void print(NetworkDefinition network){
		indent();
		if(network == null){
			out.append("Network is null");
		}
		out.append("network ");
		printEntityDecl(network);
		//--- variable declaration
		incIndent();  // actor body
		if(network.getVarDecls().length>0){
			indent();
			out.append("var");
			incIndent();
			for(DeclVar v : network.getVarDecls()){
				indent();
				print(v);
				out.append(";");
			}
			decIndent();
		}
		if(network.getEntities() != null){
			indent();
			out.append("entities");
			incIndent();
			for(Entry<String, EntityExpr> entity : network.getEntities()){
				indent();
				out.append(entity.getKey());
				out.append(" = ");
				print((EntityExpr)entity.getValue());
				out.append(";");
			}
			decIndent();
		}
		if(network.getStructure() != null && network.getStructure().length>0){
			indent();
			out.append("structure");
			incIndent();
			for(StructureStatement structure : network.getStructure()){
				indent();
				print(structure);
			}
			decIndent();
		}
		print(network.getToolAttributes());
		decIndent();  // actor body
		indent();
		out.append("end network\n");
	}
	public void print(ToolAttribute[] toolAttributes){
		if(toolAttributes != null && toolAttributes.length>0){
			indent();
			out.append("{");
			incIndent();
			for(ToolAttribute a : toolAttributes){
				indent();
				a.print(out);
				out.append(";");
			}
			decIndent();
			indent();
			out.append("}");
		}
	}
	public void printEntityDecl(DeclEntity entity){
		out.append(entity.getName());
		//--- type parameters
		if(entity.getTypeParameters().length>0){
			String sep = "";
			out.append(" [");
			for(ParDeclType param : entity.getTypeParameters()){
				out.append(sep);
				sep = ", ";
				print(param);
			}
			out.append("]");
		}
		//--- value parameters
		out.append(" (");
		String sep = "";
		for(ParDeclValue param : entity.getValueParameters()){
			out.append(sep);
			sep = ", ";
			print(param);
		}
		out.append(") ");
		print(entity.getInputPorts());
		out.append(" ==> ");
		//--- CompositePortDecl outputPorts
		print(entity.getOutputPorts());
		out.append(" : ");
	}

	public void print(Actor actor){
		indent();
		if(actor == null){
			out.append("Actor is null");
		}
		out.append("Actor ");
		printEntityDecl(actor);
		//TODO DeclType[] typeDecls
		//--- variable declaration
		incIndent();  // actor body
		for(DeclVar v : actor.getVarDecls()){
			indent();
			print(v);
			out.append(";");
		}
		//--- initializers
		for(Action a : actor.getInitializers()){
			print(a, "initialize");
		}
		//--- action
		for(Action a : actor.getActions()){
			print(a);
		}

		if(actor.getScheduleFSM() != null){
			indent();
			out.append("schedule fsm ");
			out.append(actor.getScheduleFSM().getInitialState());
			out.append(" : ");
			incIndent();
			for(Transition t : actor.getScheduleFSM().getTransitions()){
				indent();
				out.append(t.getSourceState());
				out.append("(");
				String sep = "";
				for(QID tag : t.getActionTags()){
					out.append(sep);
					sep = ", ";
					out.append(tag.toString());
				}
				out.append(") --> ");
				out.append(t.getDestinationState());
				out.append(";");
			}
			decIndent();
			indent();
			out.append("endschedule");
		}
		if(actor.getPriorities() != null && actor.getPriorities().length>0){
			indent();
			out.append("priority");
			incIndent();
			for(List<QID> priority : actor.getPriorities()){
				indent();
				String sep = "";
				for(QID qid : priority){
					out.append(sep);
					sep = " > ";
					out.append(qid.toString());
				}
				out.append(";");
			}
			decIndent();
			indent();
			out.append("end");
		}
		if(actor.getInvariants() != null && actor.getInvariants().length>0){
			indent();
			out.append("invariant ");
			print(actor.getInvariants());
			out.append(" endinvariant");
		}
		decIndent();
		indent();
		out.append("endactor\n");
	}
	public void print(Action a){
		print(a, "action");
	}
	private void print(Action a, String kind) {
		indent();
		if(a.getTag() != null){
			out.append(a.getTag().toString());
			out.append(" : ");
		}
		out.append(kind);
		incIndent();
		print(a.getInputPatterns());
		out.append(" ==> ");
		print(a.getOutputExpressions()); 
		if(a.getGuards() != null && a.getGuards().length>0){
			indent();
			out.append("guard ");
			print(a.getGuards());
		}
		if(a.getVarDecls() != null && a.getVarDecls().length>0){
			indent();
			out.append("var ");
			print(a.getVarDecls());
		}
		//TODO DeclType[] typeDecls
		//TODO Expression[] preconditions, Expression[] postconditions) 
		if(a.getDelay() != null){
			indent();
			out.append("delay ");
			a.getDelay().accept(this, null);
		}
		if(a.getBody() != null && a.getBody().length>0){
			indent();
			out.append("do");
			incIndent();
			print(a.getBody());
			decIndent();
		}
		decIndent();
		indent();
		out.append("endaction");
	}
	private void print(OutputExpression[] outputExpressions) {
		String portSep = " ";
		for(OutputExpression p : outputExpressions){
			out.append(portSep);
			portSep = ", ";
			// port name
			if(p.getPort() != null){
				out.append(p.getPort().getName());
				out.append(":");
			}
			// sequence of token names
			String varSep = "[";
			for(Expression expression : p.getExpressions()){
				out.append(varSep);
				varSep = ", ";
				expression.accept(this, null);
			}
			out.append("]");
		}
	}
	private void print(InputPattern[] inputPatterns) {
		String portSep = " ";
		for(InputPattern p : inputPatterns){
			out.append(portSep);
			portSep = ", ";
			// port name
			if(p.getPort() != null){
				out.append(p.getPort().getName());
				out.append(":");
			}
			// sequence of token names
			String varSep = "[";
			for(String var : p.getVariables()){
				out.append(varSep);
				varSep = ", ";
				out.append(var);
			}
			out.append("]");
		}
	}
	public void print(List<PortDecl> portDecls) {
		String sep = "";
		for(PortDecl p : portDecls){
			out.append(sep);
			sep = ", ";
			if(p.getType() != null){
				print(p.getType());
				out.append(" ");
			}
			out.append(p.getName());
		}
	}
	public void print(DeclVar var){
		if(var.getType() != null){
			print(var.getType());
			out.append(" ");
		}
		out.append(var.getName());
		if(var.getInitialValue() != null){
			if(var.isAssignable()){
				out.append(" := ");
			} else {
				out.append(" = ");
			}
			var.getInitialValue().accept(this, null);
		}
	}
	public void print(DeclVar[] varDecls) { // comma separated list
		String sep = "";
		for(DeclVar v : varDecls){
			out.append(sep);
			sep = ", ";
			print(v);
		}
	}

	public void print(Expression e){
		e.accept(this, null);
	}
	public void print(Expression[] expressions) {  // comma separated expressions
		if(expressions != null){
			String sep = "";
			for(Expression e : expressions){
				out.append(sep);
				sep = ", ";
				e.accept(this, null);
			}
		}
	}

	public void print(ParDeclType param){
		out.append(param.getName());
	}
	public void print(ParDeclValue param){
		if(param.getType() != null){
			print(param.getType());
			out.append(" ");
		}
		out.append(param.getName());
	}
	public void print(Statement s) {
		s.accept(this);
	}
	public void print(Statement[] list) {
		for(Statement stmt : list){
			indent();
			stmt.accept(this);
		}
	}
	public void print(TypeExpr type){
		out.append(type.getName());
		if(type.getParameters() != null && type.getParameters().length>0){
			out.append("[");
			String sep = "";
			for(TypeExpr t : type.getParameters()){
				out.append(sep);
				sep = ", ";
				print(t);
			}
			out.append("]");
		} else if((type.getTypeParameters() != null && !type.getTypeParameters().isEmpty()) || 
   				  (type.getValueParameters() != null && !type.getValueParameters().isEmpty())){
			out.append("(");
			String sep = "";
			for(Map.Entry<String, Expression>par : type.getValueParameters().entrySet()){
				out.append(sep);
				sep = ", ";
				out.append(par.getKey());
				out.append("=");
				par.getValue().accept(this, null);
			}
			for(Map.Entry<String, TypeExpr>par : type.getTypeParameters().entrySet()){
				out.append(sep);
				sep = ", ";
				out.append(par.getKey());
				out.append(":");
				print(par.getValue());
			}
			out.append(")");
		}
	}

	private void incIndent(){ 
		indentDepth++;
	}
	private void decIndent(){ 
		indentDepth--;
	}
	private void indent(){
		out.append("\n");
		for(int i=0; i<indentDepth; i++){
			out.append("  ");
		}
	}
	public void print(GeneratorFilter gen, String label) {
		out.append(label + " ");
		// type
		if(gen.getVariables()[0].getType() != null){
			print(gen.getVariables()[0].getType());
			out.append(' ');
		}
		print(gen.getVariables());
		out.append(" in ");
		gen.getCollectionExpr().accept(this, null);
		for(Expression filter : gen.getFilters()){
			out.append(", ");
			filter.accept(this, null);
		}
	}
	public void print(GeneratorFilter[] generators, String prefix){  // prefix is "for" in expressions, "foreach" in statements
		if(generators != null && generators.length>0){
			String sep = "";
			for(GeneratorFilter gen : generators){
				out.append(sep);
				sep = ", ";
				print(gen, prefix);
			}
		}
	}

/******************************************************************************
 * Expression
 */
	public Void visitExprApplication(ExprApplication e, Void p) {
		e.getFunction().accept(this, null);
		out.append("(");
		String sep = "";
		for(Expression arg : e.getArgs()){
			out.append(sep);
			sep = ", ";
			arg.accept(this, null);
		}
		out.append(")");
		return null;
	}
	public Void visitExprBinaryOp(ExprBinaryOp e, Void p) {
		String[] operations = e.getOperations();
		Expression[] operands = e.getOperands();
		boolean parentheses = operands[0] instanceof ExprBinaryOp;
		if(parentheses){
			out.append("(");
		}
		operands[0].accept(this, null);
		if(parentheses){
			out.append(")");
		}
		for(int i=0; i<operations.length; i++){
			String operation = operations[i];
			boolean useSpace = Character.isJavaIdentifierPart(operation.charAt(0)) || Character.isJavaIdentifierPart(operation.charAt(operation.length()-1));
			if(useSpace){
				out.append(' ');
			}
			out.append(operation);
			if(useSpace){
				out.append(' ');
			}
			parentheses = operands[i+1] instanceof ExprBinaryOp;
			if(parentheses){
				out.append("(");
			}
			operands[i+1].accept(this, null);
			if(parentheses){
				out.append(")");
			}
		}
		return null;
	}
	public Void visitExprField(ExprField e, Void p) {
		e.getStructure().accept(this, null);
		out.append(".");
		out.append(e.getField().getName());
		return null;
	}
	public Void visitExprIf(ExprIf e, Void p) {
		out.append("if ");
		e.getCondition().accept(this, null);
		out.append(" then ");
		e.getThenExpr().accept(this, null);
		if(e.getElseExpr() != null){
			out.append(" else ");
			e.getElseExpr().accept(this, null);
		}
		out.append(" end");
		return null;
	}
	public Void visitExprIndexer(ExprIndexer e, Void p) {
		e.getStructure().accept(this, null);
		out.append("[");
		e.getIndex().accept(this, null);
		out.append("]");
		return null;
	}
	public Void visitExprInput(ExprInput e, Void p) {
		out.append(" input ");
		// TODO input expression
		return null;
	}
	public Void visitExprLambda(ExprLambda e, Void p) {
		//TODO out.append("const ");
		out.append("lambda(");
		String sep = "";
		for(ParDeclValue param : e.getValueParameters()){
			out.append(sep);
			sep = ", ";
			print(param);
		}
		//TODO type parameters
		out.append(")");
		if(e.getReturnType() != null){
			out.append(" --> ");
			print(e.getReturnType());
		}
		sep = " var ";
		for(DeclVar v : e.getVarDecls()){
			out.append(sep);
			sep = ", ";
			print(v);
		}
		out.append(" : ");
		e.getBody().accept(this, null);
		out.append(" endlambda");
		return null;
	}
	public Void visitExprLet(ExprLet e, Void p) {
		out.append("let ");
		print(e.getVarDecls());
		//TODO type declarations
		out.append(" : ");
		e.getBody().accept(this, null);
		out.append(" endlet");
		return null;
	}
	public Void visitExprList(ExprList e, Void p) {
		out.append("[");
		String sep = "";
		for(Expression body : e.getElements()){
			out.append(sep);
			sep = ", ";
			body.accept(this, null);
		}
		if(e.getGenerators() != null && e.getGenerators().length>0){
			out.append(" : ");
			print(e.getGenerators(), "for");
		}
		//TODO tail
		out.append("]");
		return null;
	}
	public Void visitExprLiteral(ExprLiteral e, Void p) {
		out.append(e.getText());
		return null;
	}
	public Void visitExprMap(ExprMap e, Void p) {
		out.append("map {");
		String sep = "";
		for(Map.Entry<Expression, Expression> body : e.getMappings()){
			out.append(sep);
			sep = ", ";
			body.getKey().accept(this, null);
			out.append("->");
			body.getValue().accept(this, null);
		}
		if(e.getGenerators() != null && e.getGenerators().length>0){
			out.append(" : ");
			print(e.getGenerators(), "for");
		}
		out.append("}");
		return null;
	}
	public Void visitExprProc(ExprProc e, Void p) {
		out.append("proc(");
		String sep = "";
		for(ParDeclValue param : e.getValueParameters()){
			out.append(sep);
			sep = ", ";
			print(param);
		}
		//TODO type parameters
		out.append(")");
		sep = " var ";
		for(DeclVar v : e.getVarDecls()){
			out.append(sep);
			sep = ", ";
			print(v);
		}
		out.append(" begin ");
		incIndent();
		for(Statement s : e.getBody()){
			indent();
			print(s);
		}
		decIndent();
		indent();
		out.append("endproc");
		return null;
	}
	public Void visitExprSet(ExprSet e, Void p) {
		out.append("{");
		String sep = "";
		for(Expression body : e.getElements()){
			out.append(sep);
			sep = ", ";
			body.accept(this, null);
		}
		if(e.getGenerators() != null && e.getGenerators().length>0){
			out.append(" : ");
			print(e.getGenerators(), "for");
		}
		out.append("}");
		return null;
	}
	public Void visitExprUnaryOp(ExprUnaryOp e, Void p) {
		String operation = e.getOperation();
		out.append(operation);
		if(Character.isJavaIdentifierPart(operation.charAt(operation.length()-1))){
			out.append(' ');
		}
		e.getOperand().accept(this, null);
		return null;
	}
	public Void visitExprVariable(ExprVariable e, Void p) {
		out.append(e.getVariable().getName());
		return null;
	}
/******************************************************************************
 * Statement
 */
	public Void visitStmtAssignment(StmtAssignment s, Void p) {
		s.getLValue().accept(this, null);
		out.append(" := ");
		s.getExpression().accept(this, null);
		out.append(";");
		return null;
	}
	public Void visitStmtBlock(StmtBlock s, Void p) {
		out.append("begin");
		incIndent();
		if(s.getVarDecls() != null && s.getVarDecls().length>0){
			decIndent();
			indent();
			out.append("var");
			incIndent();
			indent();
			print(s.getVarDecls());
			decIndent();
			indent();
			out.append("do");
			incIndent();
		}
		//TODO type declarations
		print(s.getStatements());
		decIndent();
		indent();
		out.append("end");
		return null;
	}
	public Void visitStmtIf(StmtIf s, Void p) {
		out.append("if ");
		s.getCondition().accept(this, null);
		out.append(" then");
		incIndent();
		indent();
		s.getThenBranch().accept(this);
		if(s.getElseBranch() != null){
			decIndent();
			indent();
			out.append("else");
			incIndent();
			indent();
			s.getThenBranch().accept(this);
		}
		decIndent();
		indent();
		out.append("end");
		return null;
	}
	public Void visitStmtCall(StmtCall s, Void p) {
		s.getProcedure().accept(this, null);
		out.append("(");
		String sep = "";
		for(Expression arg : s.getArgs()){
			out.append(sep);
			sep = ", ";
			arg.accept(this, null);
		}
		out.append(");");
		return null;
	}
	public Void visitStmtOutput(StmtOutput s, Void p) {
		out.append("output;");
		// TODO output statement
		return null;
	}
	public Void visitStmtConsume(StmtConsume s, Void p) {
		out.append("consume;");
		// TODO consume statement
		return null;
	}
	
	public Void visitStmtWhile(StmtWhile s, Void p) {
		out.append("while ");
		s.getCondition().accept(this, null);
		indent();
		s.getBody().accept(this);
		indent();
		out.append("endwhile");
		return null;
	}
	public Void visitStmtForeach(StmtForeach s, Void p) {
		if(s.getGenerators() != null && s.getGenerators().length>0){
			print(s.getGenerators(), "foreach");
		}
		out.append(" do");
		indent();
		s.getBody().accept(this);
		indent();
		out.append("endforeach");
		return null;
	}
	
/******************************************************************************
 * LValue
 */

	@Override
	public Void visitLValueVariable(LValueVariable lvalue, Void parameter) {
		out.append(lvalue.getVariable().getName());
		return null;
	}

	@Override
	public Void visitLValueIndexer(LValueIndexer lvalue, Void parameter) {
		lvalue.getStructure().accept(this, null);
		out.append("[");
		lvalue.getIndex().accept(this, null);
		out.append("]");
		return null;
	}

	@Override
	public Void visitLValueField(LValueField lvalue, Void parameter) {
		lvalue.getStructure().accept(this, null);
		out.append(".");
		out.append(lvalue.getField().getName());
		return null;
	}

/******************************************************************************
 * EntityExpr (Network)
 */
	public void print(EntityExpr entity){
		entity.accept(this, null);
	}
	public Void visitEntityInstanceExpr(EntityInstanceExpr e, Void p) {
		out.append(e.getEntityName());
		out.append("(");
		String sep = "";
		for(java.util.Map.Entry<String, Expression> param : e.getParameterAssignments()){
			out.append(sep);
			out.append(param.getKey());
			out.append(" = ");
			print(param.getValue());
			sep = ", ";
		}
		out.append(")");
		print(e.getToolAttributes());
		return null;
	}
	public Void visitEntityIfExpr(EntityIfExpr e, Void p) {
		out.append("if ");
		print(e.getCondition());
		out.append(" then ");
		print(e.getTrueEntity());
		out.append(" else ");
		print(e.getFalseEntity());
		out.append(" end");
		return null;
	}
	public Void visitEntityListExpr(EntityListExpr e, Void p) {
		out.append("[");
		String sep = "";
		for(EntityExpr entity : e.getEntityList()){
			out.append(sep);
			sep = ", ";
			print(entity);
		}
		if(e.getGenerators() != null && e.getGenerators().length>0){
			out.append(" : ");
			print(e.getGenerators(), "for");
		}
		out.append("]");
		return null;
	}
/******************************************************************************
 * Structure Statement (Network)
 */
	public void print(StructureStatement structure){
		structure.accept(this, null);
	}
	public void print(StructureStatement[] structure){
		incIndent();
		for(StructureStatement s : structure){
			indent();
			s.accept(this, null);
		}
		decIndent();
	}
	public Void visitStructureConnectionStmt(StructureConnectionStmt stmt, Void p) {
		print(stmt.getSrc());
		out.append(" --> ");
		print(stmt.getDst());
		print(stmt.getToolAttributes());
		out.append(';');
		return null;
	}
	public Void visitStructureIfStmt(StructureIfStmt stmt, Void p) {
		out.append("if ");
		print(stmt.getCondition());
		out.append(" then");
		print(stmt.getTrueStmt());
		if(stmt.getFalseStmt() != null){
			indent();
			out.append("else");
			print(stmt.getFalseStmt());
		}
		indent();
		out.append("end");
		return null;
	}
	public Void visitStructureForeachStmt(StructureForeachStmt stmt, Void p) {
		print(stmt.getGenerators(), "foreach");
		out.append(" do ");
		print(stmt.getStatements());
		indent();
		out.append("end");
		return null;
	}
	public void print(PortReference port){
		if(port.getEntityName() != null){
			out.append(port.getEntityName());
			if(port.getEntityIndex() != null){
				for(Expression e : port.getEntityIndex()){
					out.append('[');
					print(e);
					out.append(']');
				}
			}
			out.append('.');
		}
		out.append(port.getPortName());
	}
}
