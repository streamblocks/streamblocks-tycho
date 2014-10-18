package se.lth.cs.tycho.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.EntityDefinition;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.entity.cal.Transition;
import se.lth.cs.tycho.ir.entity.nl.EntityExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityExprVisitor;
import se.lth.cs.tycho.ir.entity.nl.EntityIfExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityListExpr;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.entity.nl.PortReference;
import se.lth.cs.tycho.ir.entity.nl.StructureConnectionStmt;
import se.lth.cs.tycho.ir.entity.nl.StructureForeachStmt;
import se.lth.cs.tycho.ir.entity.nl.StructureIfStmt;
import se.lth.cs.tycho.ir.entity.nl.StructureStatement;
import se.lth.cs.tycho.ir.entity.nl.StructureStmtVisitor;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprField;
import se.lth.cs.tycho.ir.expr.ExprIf;
import se.lth.cs.tycho.ir.expr.ExprIndexer;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprList;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprMap;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.expr.ExprSet;
import se.lth.cs.tycho.ir.expr.ExprUnaryOp;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.expr.ExpressionVisitor;
import se.lth.cs.tycho.ir.expr.GlobalValueReference;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StatementVisitor;
import se.lth.cs.tycho.ir.stmt.StmtAssignment;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtCall;
import se.lth.cs.tycho.ir.stmt.StmtConsume;
import se.lth.cs.tycho.ir.stmt.StmtForeach;
import se.lth.cs.tycho.ir.stmt.StmtIf;
import se.lth.cs.tycho.ir.stmt.StmtOutput;
import se.lth.cs.tycho.ir.stmt.StmtWhile;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueField;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueIndexer;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVisitor;
import se.lth.cs.tycho.ir.util.ImmutableList;

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
		printToolAttributes(network.getToolAttributes());
		decIndent();  // calActor body
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
		out.append("CalActor ");
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
			out.append("endschedule");
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
