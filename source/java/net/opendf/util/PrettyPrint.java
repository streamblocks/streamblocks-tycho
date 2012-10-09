package net.opendf.util;

import java.util.List;
import java.util.Map;

import net.opendf.ir.cal.*;
import net.opendf.ir.common.*;

public class PrettyPrint implements ExpressionVisitor<String,String>, StatementVisitor{
	private java.io.PrintStream out = System.out;
	private int indentDepth = 0;

	public void print(Actor actor){
		indent();
		if(actor == null){
			out.append("Actor is null");
		}
		out.append("Actor ");
		out.append(actor.getName());
		//--- type parameters
		if(actor.getTypeParameters().length>0){
			String sep = "";
			out.append(" [");
			for(ParDeclType param : actor.getTypeParameters()){
				out.append(sep);
				sep = ", ";
				print(param);
			}
			out.append("]");
		}
		//--- value parameters
		out.append(" (");
		String sep = "";
		for(ParDeclValue param : actor.getValueParameters()){
			out.append(sep);
			sep = ", ";
			print(param);
		}
		out.append(") ");
		print(actor.getInputPorts());
		out.append(" ==>");
		//--- CompositePortDecl outputPorts
		print(actor.getOutputPorts());
		out.append(" : ");
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
				sep = "";
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
				sep = "";
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
			if(p.getPortname() != null){
				out.append(p.getPortname().toString());
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
			if(p.getPortname() != null){
				out.append(p.getPortname().toString());
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
	public void print(PortDecl portDecl) {
		if(portDecl instanceof CompositePortDecl){
			CompositePortDecl cpd = (CompositePortDecl)portDecl;
			String sep = "";
			for(PortDecl p : cpd.getChildren()){
				out.append(sep);
				sep = ", ";
				print(p);
			}
		} else {
			AtomicPortDecl port = (AtomicPortDecl)portDecl;
			if(port.getType() != null){
				print(port.getType());
				out.append(" ");
			}
			out.append(port.getLocalName());
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
			out.append(" ");
		}
		print(gen.getVariables());
		out.append(" in ");
		gen.getCollectionExpr().accept(this, null);
		for(Expression filter : gen.getFilters()){
			out.append(", ");
			filter.accept(this, null);
		}
	}

//--- Expression --------------------------------------------------------------
	public String visitExprApplication(ExprApplication e, String p) {
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
	public String visitExprBinaryOp(ExprBinaryOp e, String p) {
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
			out.append(operations[i]);
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
	public String visitExprEntry(ExprEntry e, String p) {
		e.getEnclosingExpr().accept(this, null);
		out.append(".");
		out.append(e.getName());
		return null;
	}
	public String visitExprIf(ExprIf e, String p) {
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
	public String visitExprIndexer(ExprIndexer e, String p) {
		e.getStructure().accept(this, null);
		out.append("[");
		String sep = "";
		for(Expression arg : e.getLocation()){
			out.append(sep);
			sep = ", ";
			arg.accept(this, null);
		}
		out.append("]");
		return null;
	}
	public String visitExprInput(ExprInput e, String p) {
		out.append(" input ");
		// TODO input expression
		return null;
	}
	public String visitExprLambda(ExprLambda e, String p) {
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
	public String visitExprLet(ExprLet e, String p) {
		out.append("let ");
		print(e.getVarDecls());
		//TODO type declarations
		out.append(" : ");
		e.getBody().accept(this, null);
		out.append(" endlet");
		return null;
	}
	public String visitExprList(ExprList e, String p) {
		out.append("[");
		String sep = "";
		for(Expression body : e.getElements()){
			out.append(sep);
			sep = ", ";
			body.accept(this, null);
		}
		if(e.getGenerators() != null && e.getGenerators().length>0){
			sep = " : ";
			for(GeneratorFilter gen : e.getGenerators()){
				out.append(sep);
				sep = ", ";
				print(gen, "for");
			}
		}
		//TODO tail
		out.append("]");
		return null;
	}
	public String visitExprLiteral(ExprLiteral e, String p) {
		out.append(e.getText());
		return null;
	}
	public String visitExprMap(ExprMap e, String p) {
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
			sep = " : ";
			for(GeneratorFilter gen : e.getGenerators()){
				out.append(sep);
				sep = ", ";
				print(gen, "for");
			}
		}
		out.append("}");
		return null;
	}
	public String visitExprProc(ExprProc e, String p) {
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
	public String visitExprSet(ExprSet e, String p) {
		out.append("{");
		String sep = "";
		for(Expression body : e.getElements()){
			out.append(sep);
			sep = ", ";
			body.accept(this, null);
		}
		if(e.getGenerators() != null && e.getGenerators().length>0){
			sep = " : ";
			for(GeneratorFilter gen : e.getGenerators()){
				out.append(sep);
				sep = ", ";
				print(gen, "for");
			}
		}
		out.append("}");
		return null;
	}
	public String visitExprUnaryOp(ExprUnaryOp e, String p) {
		out.append(e.getOperation());
		e.getOperand().accept(this, null);
		return null;
	}
	public String visitExprVariable(ExprVariable e, String p) {
		out.append(e.getName());
		return null;
	}
//--- Statement ---------------------------------------------------------------
	public void visitStmtAssignment(StmtAssignment s) {
		out.append(s.getVar());
		//TODO field and index
		out.append(" := ");
		s.getVal().accept(this, null);
		out.append(";");
	}
	public void visitStmtBlock(StmtBlock s) {
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
	}
	public void visitStmtIf(StmtIf s) {
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
	}
	public void visitStmtCall(StmtCall s) {
		s.getProcedure().accept(this, null);
		out.append("(");
		String sep = "";
		for(Expression arg : s.getArgs()){
			out.append(sep);
			sep = ", ";
			arg.accept(this, null);
		}
		out.append(");");
	}
	public void visitStmtOutput(StmtOutput s) {
		out.append("output;");
		// TODO output statement
	}
	public void visitStmtWhile(StmtWhile s) {
		out.append("while ");
		s.getCondition().accept(this, null);
		indent();
		s.getBody().accept(this);
		indent();
		out.append("endwhile");
	}
	public void visitStmtForeach(StmtForeach s) {
		String sep = "";
		for(GeneratorFilter f : s.getGenerators()){
			out.append(sep);
			sep = ", ";
			print(f, "foreach");
		}
		out.append(" do");
		indent();
		s.getBody().accept(this);
		indent();
		out.append("endforeach");
	}
}
