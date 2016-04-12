package se.lth.cs.tycho.util;

import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.ToolAttribute;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.entity.cal.Transition;
import se.lth.cs.tycho.ir.entity.nl.*;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.stmt.*;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueField;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueIndexer;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVisitor;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PrettyPrint implements ExpressionVisitor<Void,Void>, StatementVisitor<Void, Void>, EntityExprVisitor<Void, Void>, StructureStmtVisitor<Void, Void>, LValueVisitor<Void, Void> {
	private java.io.PrintStream out = System.out;
	private int indentDepth = 0;

	public void print(NlNetwork network, String name){
		indent();
		if(network == null){
			out.append("Network is null");
		}
		out.append("network ");
		out.append(name);
		printEntityDef(network);
		//--- variable declaration
		incIndent();  // calActor body
		if(!network.getVarDecls().isEmpty()){
			indent();
			out.append("var");
			incIndent();
			for(VarDecl v : network.getVarDecls()){
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
		if(network.getStructure() != null && !network.getStructure().isEmpty()){
			indent();
			out.append("structure");
			incIndent();
			for(StructureStatement structure : network.getStructure()){
				indent();
				print(structure);
			}
			decIndent();
		}
		printToolAttributes(network.getAttributes());
		decIndent();  // calActor body
		indent();
		out.append("end\n");
	}
	public void printToolAttributes(Collection<ToolAttribute> toolAttributes){
		if(toolAttributes != null && !toolAttributes.isEmpty()){
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
	public void printEntityDef(Entity entity){
		//--- type parameters
		if(!entity.getTypeParameters().isEmpty()){
			String sep = "";
			out.append(" [");
			for(TypeDecl param : entity.getTypeParameters()){
				out.append(sep);
				sep = ", ";
				print(param);
			}
			out.append("]");
		}
		//--- value parameters
		out.append(" (");
		String sep = "";
		for(VarDecl param : entity.getValueParameters()){
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

	public void print(CalActor calActor, String name){
		indent();
		if(calActor == null){
			out.append("CalActor is null");
		}
		out.append("actor ");
		out.append(name);
		printEntityDef(calActor);
		//TODO DeclType[] typeDecls
		//--- variable declaration
		incIndent();  // calActor body
		for(VarDecl v : calActor.getVarDecls()){
			indent();
			print(v);
			out.append(";");
		}
		//--- initializers
		for(Action a : calActor.getInitializers()){
			print(a, "initialize");
		}
		//--- action
		for(Action a : calActor.getActions()){
			print(a);
		}

		if(calActor.getScheduleFSM() != null){
			indent();
			out.append("schedule fsm ");
			out.append(calActor.getScheduleFSM().getInitialState());
			out.append(" : ");
			incIndent();
			for(Transition t : calActor.getScheduleFSM().getTransitions()){
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
			out.append("end");
		}
		if(calActor.getPriorities() != null && !calActor.getPriorities().isEmpty()){
			indent();
			out.append("priority");
			incIndent();
			for(List<QID> priority : calActor.getPriorities()){
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
		if(calActor.getInvariants() != null && !calActor.getInvariants().isEmpty()){
			indent();
			out.append("invariant ");
			printExpressions(calActor.getInvariants());
			out.append(" end");
		}
		decIndent();
		indent();
		out.append("end\n");
	}
	public void print(Action a){
		print(a, "action");
	}
	private void print(Action a, String kind) {
		indent();
		if(a.getTag() != null){
			out.append(a.getTag().toString());
			out.append(":");
			indent();
		}
		out.append(kind);
		printInputPatterns(a.getInputPatterns());
		out.append(" ==>");
		printOutputExpressions(a.getOutputExpressions()); 
		if(a.getGuards() != null && !a.getGuards().isEmpty()){
			indent();
			out.append("guard ");
			printExpressions(a.getGuards());
		}
		if(a.getVarDecls() != null && !a.getVarDecls().isEmpty()){
			indent();
			out.append("var ");
			printVarDecls(a.getVarDecls());
		}
		//TODO DeclType[] typeDecls
		//TODO Expression[] preconditions, Expression[] postconditions) 
		if(a.getDelay() != null){
			indent();
			out.append("delay ");
			a.getDelay().accept(this, null);
		}
		if(a.getBody() != null && !a.getBody().isEmpty()){
			indent();
			out.append("do");
			incIndent();
			printStatements(a.getBody());
			decIndent();
		}
		indent();
		out.append("end\n");
	}
	private void printOutputExpressions(Iterable<OutputExpression> outputExpressions) {
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
	private void printInputPatterns(Iterable<InputPattern> inputPatterns) {
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
			for(VarDecl var : p.getVariables()){
				out.append(varSep);
				varSep = ", ";
				out.append(var.getName());
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
	public void print(VarDecl var){
		if(var.getType() != null){
			print(var.getType());
			out.append(" ");
		}
		out.append(var.getName());
		if(var.getValue() != null){
			if(var.isConstant()){
				out.append(" = ");
			} else {
				out.append(" := ");
			}
			var.getValue().accept(this, null);
		}
	}
	public void printVarDecls(Iterable<VarDecl> varDecls) { // comma separated list
		String sep = "";
		for(VarDecl v : varDecls){
			out.append(sep);
			sep = ", ";
			print(v);
		}
	}

	public void print(Expression e){
		e.accept(this, null);
	}
	
	public void printExpressions(Expression[] expressions) {
		printExpressions(Arrays.asList(expressions));
	}
	public void printExpressions(Iterable<Expression> expressions) {  // comma separated expressions
		if(expressions != null){
			String sep = "";
			for(Expression e : expressions){
				out.append(sep);
				sep = ", ";
				e.accept(this, null);
			}
		}
	}

	public void print(TypeDecl param){
		out.append(param.getName());
	}
	public void print(Statement s) {
		s.accept(this);
	}
	public void printStatements(Iterable<Statement> list) {
		for(Statement stmt : list){
			indent();
			stmt.accept(this);
		}
	}
	public void print(TypeExpr type){
		out.append(type.getName());
		if((type.getTypeParameters() != null && !type.getTypeParameters().isEmpty()) || 
   				  (type.getValueParameters() != null && !type.getValueParameters().isEmpty())){
			out.append("(");
			String sep = "";
			for(Parameter<Expression> par : type.getValueParameters()){
				out.append(sep);
				sep = ", ";
				out.append(par.getName());
				out.append("=");
				par.getValue().accept(this, null);
			}
			for(Parameter<TypeExpr> par : type.getTypeParameters()){
				out.append(sep);
				sep = ", ";
				out.append(par.getName());
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
		ImmutableList<String> operations = e.getOperations();
		ImmutableList<Expression> operands = e.getOperands();
		boolean parentheses = operands.get(0) instanceof ExprBinaryOp;
		if(parentheses){
			out.append("(");
		}
		operands.get(0).accept(this, null);
		if(parentheses){
			out.append(")");
		}
		for(int i=0; i<operations.size(); i++){
			String operation = operations.get(i);
			boolean useSpace = Character.isJavaIdentifierPart(operation.charAt(0)) || Character.isJavaIdentifierPart(operation.charAt(operation.length()-1));
			if(useSpace){
				out.append(' ');
			}
			out.append(operation);
			if(useSpace){
				out.append(' ');
			}
			parentheses = operands.get(i+1) instanceof ExprBinaryOp;
			if(parentheses){
				out.append("(");
			}
			operands.get(i+1).accept(this, null);
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
		for(VarDecl param : e.getValueParameters()){
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
		out.append(" : ");
		e.getBody().accept(this, null);
		out.append(" endlambda");
		return null;
	}
	public Void visitExprLet(ExprLet e, Void p) {
		out.append("let ");
		printVarDecls(e.getVarDecls());
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
//		if(e.getGenerators() != null && !e.getGenerators().isEmpty()){
//			out.append(" : ");
//			printGenerators(e.getGenerators(), "for");
//		}
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
//		if(e.getGenerators() != null && !e.getGenerators().isEmpty()){
//			out.append(" : ");
//			printGenerators(e.getGenerators(), "for");
//		}
		out.append("}");
		return null;
	}
	public Void visitExprProc(ExprProc e, Void p) {
		out.append("proc(");
		String sep = "";
		for(VarDecl param : e.getValueParameters()){
			out.append(sep);
			sep = ", ";
			print(param);
		}
		//TODO type parameters
		out.append(")");
		out.append(" begin ");
		incIndent();
		indent();
		e.getBody().forEach(this::print);
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
//		if(e.getGenerators() != null && !e.getGenerators().isEmpty()){
//			out.append(" : ");
//			printGenerators(e.getGenerators(), "for");
//		}
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
		if(s.getVarDecls() != null && !s.getVarDecls().isEmpty()){
			decIndent();
			indent();
			out.append("var");
			incIndent();
			indent();
			printVarDecls(s.getVarDecls());
			decIndent();
			indent();
			out.append("do");
			incIndent();
		}
		//TODO type declarations
		printStatements(s.getStatements());
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
		s.getThenBranch().forEach(x -> x.accept(this));
		if(s.getElseBranch() != null){
			decIndent();
			indent();
			out.append("else");
			incIndent();
			indent();
			s.getElseBranch().forEach(x -> x.accept(this));
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
	public Void visitStmtConsume(StmtConsume s, Void p) {
		out.append("consume;");
		// TODO consume statement
		return null;
	}
	
	public Void visitStmtWhile(StmtWhile s, Void p) {
		out.append("while ");
		s.getCondition().accept(this, null);
		indent();
		s.getBody().forEach(x -> x.accept(this));
		indent();
		out.append("endwhile");
		return null;
	}
	public Void visitStmtForeach(StmtForeach s, Void p) {
//		if(s.getGenerators() != null && !s.getGenerators().isEmpty()){
//			printGenerators(s.getGenerators(), "foreach");
//		}
		out.append(" do");
		indent();
		s.getBody().forEach(x -> x.accept(this));
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
		for(Parameter<Expression> param : e.getParameterAssignments()){
			out.append(sep);
			out.append(param.getName());
			out.append(" = ");
			print(param.getValue());
			sep = ", ";
		}
		out.append(")");
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
//		if(e.getGenerators() != null && !e.getGenerators().isEmpty()){
//			out.append(" : ");
//			printGenerators(e.getGenerators(), "for");
//		}
		out.append("]");
		return null;
	}
/******************************************************************************
 * Structure Statement (Network)
 */
	public void print(StructureStatement structure){
		structure.accept(this, null);
	}
	public void printStructureStatements(Iterable<StructureStatement> structure){
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
		printToolAttributes(stmt.getAttributes());
		out.append(';');
		return null;
	}
	public Void visitStructureIfStmt(StructureIfStmt stmt, Void p) {
		out.append("if ");
		print(stmt.getCondition());
		out.append(" then");
		printStructureStatements(stmt.getTrueStmt());
		if(stmt.getFalseStmt() != null){
			indent();
			out.append("else");
			printStructureStatements(stmt.getFalseStmt());
		}
		indent();
		out.append("end");
		return null;
	}
	public Void visitStructureForeachStmt(StructureForeachStmt stmt, Void p) {
//		printGenerators(stmt.getGenerators(), "foreach");
		out.append(" do ");
		printStructureStatements(stmt.getStatements());
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
