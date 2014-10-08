package net.opendf.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.opendf.ir.GeneratorFilter;
import net.opendf.ir.QID;
import net.opendf.ir.TypeExpr;
import net.opendf.ir.decl.LocalVarDecl;
import net.opendf.ir.decl.ParDeclType;
import net.opendf.ir.decl.ParDeclValue;
import net.opendf.ir.decl.VarDecl;
import net.opendf.ir.entity.EntityDefinition;
import net.opendf.ir.entity.PortDecl;
import net.opendf.ir.entity.cal.Action;
import net.opendf.ir.entity.cal.Actor;
import net.opendf.ir.entity.cal.InputPattern;
import net.opendf.ir.entity.cal.OutputExpression;
import net.opendf.ir.entity.cal.Transition;
import net.opendf.ir.entity.nl.EntityExpr;
import net.opendf.ir.entity.nl.EntityExprVisitor;
import net.opendf.ir.entity.nl.EntityIfExpr;
import net.opendf.ir.entity.nl.EntityInstanceExpr;
import net.opendf.ir.entity.nl.EntityListExpr;
import net.opendf.ir.entity.nl.NetworkDefinition;
import net.opendf.ir.entity.nl.PortReference;
import net.opendf.ir.entity.nl.StructureConnectionStmt;
import net.opendf.ir.entity.nl.StructureForeachStmt;
import net.opendf.ir.entity.nl.StructureIfStmt;
import net.opendf.ir.entity.nl.StructureStatement;
import net.opendf.ir.entity.nl.StructureStmtVisitor;
import net.opendf.ir.expr.ExprApplication;
import net.opendf.ir.expr.ExprBinaryOp;
import net.opendf.ir.expr.ExprField;
import net.opendf.ir.expr.ExprIf;
import net.opendf.ir.expr.ExprIndexer;
import net.opendf.ir.expr.ExprInput;
import net.opendf.ir.expr.ExprLambda;
import net.opendf.ir.expr.ExprLet;
import net.opendf.ir.expr.ExprList;
import net.opendf.ir.expr.ExprLiteral;
import net.opendf.ir.expr.ExprMap;
import net.opendf.ir.expr.ExprProc;
import net.opendf.ir.expr.ExprSet;
import net.opendf.ir.expr.ExprUnaryOp;
import net.opendf.ir.expr.ExprVariable;
import net.opendf.ir.expr.Expression;
import net.opendf.ir.expr.ExpressionVisitor;
import net.opendf.ir.expr.GlobalValueReference;
import net.opendf.ir.net.Connection;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;
import net.opendf.ir.net.ToolAttribute;
import net.opendf.ir.stmt.Statement;
import net.opendf.ir.stmt.StatementVisitor;
import net.opendf.ir.stmt.StmtAssignment;
import net.opendf.ir.stmt.StmtBlock;
import net.opendf.ir.stmt.StmtCall;
import net.opendf.ir.stmt.StmtConsume;
import net.opendf.ir.stmt.StmtForeach;
import net.opendf.ir.stmt.StmtIf;
import net.opendf.ir.stmt.StmtOutput;
import net.opendf.ir.stmt.StmtWhile;
import net.opendf.ir.stmt.lvalue.LValueField;
import net.opendf.ir.stmt.lvalue.LValueIndexer;
import net.opendf.ir.stmt.lvalue.LValueVariable;
import net.opendf.ir.stmt.lvalue.LValueVisitor;
import net.opendf.ir.util.ImmutableEntry;
import net.opendf.ir.util.ImmutableList;

public class PrettyPrint implements ExpressionVisitor<Void,Void>, StatementVisitor<Void, Void>, EntityExprVisitor<Void, Void>, StructureStmtVisitor<Void, Void>, LValueVisitor<Void, Void> {
	private java.io.PrintStream out = System.out;
	private int indentDepth = 0;

	public static String toString(Network net){
		StringBuffer sb = new StringBuffer("input ports:\n");
		for(PortDecl port : net.getInputPorts()){
			sb.append("  ");
			sb.append(port);
			sb.append("\n");
		}
		sb.append("output ports:\n");
		for(PortDecl port : net.getOutputPorts()){
			sb.append("  ");
			sb.append(port);
			sb.append("\n");
		}
		sb.append("entities:\n");
		for(Node n : net.getNodes()){
			sb.append(" ");
			sb.append(n.getName());
			sb.append("\n");
		}
		sb.append("connections:\n");
		for(Connection c : net.getConnections()){
			sb.append(" ");
			sb.append(c.getSrcNodeId());
			sb.append(".");
			sb.append(c.getSrcPort());
			sb.append("->");
			sb.append(c.getDstNodeId());
			sb.append(".");
			sb.append(c.getDstPort());
			sb.append("\n");
		}
		
		return sb.toString();
	}

	public void print(NetworkDefinition network, String name){
		indent();
		if(network == null){
			out.append("Network is null");
		}
		out.append("network ");
		out.append(name);
		printEntityDef(network);
		//--- variable declaration
		incIndent();  // actor body
		if(!network.getVarDecls().isEmpty()){
			indent();
			out.append("var");
			incIndent();
			for(LocalVarDecl v : network.getVarDecls()){
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
		printToolAttributes(network.getToolAttributes());
		decIndent();  // actor body
		indent();
		out.append("end network\n");
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
	public void printEntityDef(EntityDefinition entity){
		//--- type parameters
		if(!entity.getTypeParameters().isEmpty()){
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

	public void print(Actor actor, String name){
		indent();
		if(actor == null){
			out.append("Actor is null");
		}
		out.append("Actor ");
		out.append(name);
		printEntityDef(actor);
		//TODO DeclType[] typeDecls
		//--- variable declaration
		incIndent();  // actor body
		for(LocalVarDecl v : actor.getVarDecls()){
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
		if(actor.getPriorities() != null && !actor.getPriorities().isEmpty()){
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
		if(actor.getInvariants() != null && !actor.getInvariants().isEmpty()){
			indent();
			out.append("invariant ");
			printExpressions(actor.getInvariants());
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
		printInputPatterns(a.getInputPatterns());
		out.append(" ==> ");
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
		decIndent();
		indent();
		out.append("endaction");
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
	public void print(LocalVarDecl var){
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
	public void printVarDecls(Iterable<LocalVarDecl> varDecls) { // comma separated list
		String sep = "";
		for(LocalVarDecl v : varDecls){
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
			for(ImmutableEntry<String, Expression>par : type.getValueParameters()){
				out.append(sep);
				sep = ", ";
				out.append(par.getKey());
				out.append("=");
				par.getValue().accept(this, null);
			}
			for(ImmutableEntry<String, TypeExpr>par : type.getTypeParameters()){
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
		if(gen.getVariables().get(0).getType() != null){
			print(gen.getVariables().get(0).getType());
			out.append(' ');
		}
		printVarDecls(gen.getVariables());
		out.append(" in ");
		gen.getCollectionExpr().accept(this, null);
		for(Expression filter : gen.getFilters()){
			out.append(", ");
			filter.accept(this, null);
		}
	}
	public void printGenerators(Collection<GeneratorFilter> generators, String prefix){  // prefix is "for" in expressions, "foreach" in statements
		if(generators != null && !generators.isEmpty()){
			String sep = "";
			for(GeneratorFilter gen : generators){
				out.append(sep);
				sep = ", ";
				print(gen, prefix);
			}
		}
	}
	public void printGenerators(GeneratorFilter[] generators, String prefix) {
		printGenerators(Arrays.asList(generators), prefix);
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
		if(e.getGenerators() != null && !e.getGenerators().isEmpty()){
			out.append(" : ");
			printGenerators(e.getGenerators(), "for");
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
		if(e.getGenerators() != null && !e.getGenerators().isEmpty()){
			out.append(" : ");
			printGenerators(e.getGenerators(), "for");
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
		out.append(" begin ");
		incIndent();
		indent();
		print(e.getBody());
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
		if(e.getGenerators() != null && !e.getGenerators().isEmpty()){
			out.append(" : ");
			printGenerators(e.getGenerators(), "for");
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

	@Override
	public Void visitGlobalValueReference(GlobalValueReference e, Void p) {
		out.append(e.getQualifiedIdentifier().toString());
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
		s.getThenBranch().accept(this);
		if(s.getElseBranch() != null){
			decIndent();
			indent();
			out.append("else");
			incIndent();
			indent();
			s.getElseBranch().accept(this);
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
		if(s.getGenerators() != null && !s.getGenerators().isEmpty()){
			printGenerators(s.getGenerators(), "foreach");
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
		printToolAttributes(e.getToolAttributes());
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
		if(e.getGenerators() != null && !e.getGenerators().isEmpty()){
			out.append(" : ");
			printGenerators(e.getGenerators(), "for");
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
		printToolAttributes(stmt.getToolAttributes());
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
		printGenerators(stmt.getGenerators(), "foreach");
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