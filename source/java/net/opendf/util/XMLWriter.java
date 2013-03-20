package net.opendf.util;

/**
 *  copyright (c) 2013, Per Andersson
 *  
 *  This code translates a cal IR to an XML document (org.w3c.dom.Document)
 *  The translation is incomplete!!!
 *  Currently only expressions are exported.
 **/

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

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

public class XMLWriter implements ExpressionVisitor<Void,Element>, StatementVisitor<Void, Element>, EntityExprVisitor<Void, Void>, StructureStmtVisitor<Void, Void>{
	private java.io.PrintStream out = System.out;
	Document doc;

	private int indentDepth = 0;

	public Document getDocument(){ return doc; }
	public void print(){
        try {
        	OutputFormat format = new OutputFormat(doc);
        	format.setLineWidth(65);
        	format.setIndenting(true);
        	format.setIndent(2);
        	XMLSerializer serializer = new XMLSerializer(out, format);
        	serializer.serialize(doc);
        } catch (IOException e) {
			e.printStackTrace();
		}
	}
	public XMLWriter(NetworkDefinition network){
		try{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();
			
			Element actorElement = doc.createElement("NetworkDefinition");
			doc.appendChild(actorElement);
			actorElement.setAttribute("name", network.getName());

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
		
		}catch(ParserConfigurationException pce){
			pce.printStackTrace();
		}
	}
	public XMLWriter(Actor actor){
		try{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();
			
			Element actorElement = doc.createElement("Actor");
			doc.appendChild(actorElement);
			actorElement.setAttribute("name", actor.getName());

			printEntityDecl(actor);
			//TODO DeclType[] typeDecls
			//--- variable declaration
			generateXML(actor.getVarDecls(), actorElement);
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

		}catch(ParserConfigurationException pce){
			pce.printStackTrace();
		}
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
		out.append("name=" + entity.getName());
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

	public void generateXML(DeclVar[] varDecls, Element parent){
		Element declElements = doc.createElement("DeclVarList");
		for(DeclVar v : varDecls){
			Element varDeclElem = doc.createElement("DeclVar");
			varDeclElem.setAttribute("name", v.getName());
			if(v.getInitialValue() != null){
				Element initElem = doc.createElement("InitialValue");
				v.getInitialValue().accept(this, initElem);
				varDeclElem.appendChild(initElem);
			}
			declElements.appendChild(varDeclElem);
		}
		parent.appendChild(declElements);
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
//			print(a.getBody());
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
	private void generateXML(ParDeclValue[] valueParameters, Element p) {
		if(valueParameters != null && valueParameters.length >0){
			Element top = doc.createElement("ValueParameters");
			p.appendChild(top);
			for(ParDeclValue param : valueParameters){
				generateXML(param, top);
			}
		}
	}
	private void generateXML(ParDeclValue param, Element p) {
		Element top = doc.createElement("ParDeclValue");
		p.appendChild(top);
		top.setAttribute("name", param.getName());
		generateXML(param.getType(), top);
	}
	public void generateXML(TypeExpr type, Element parent){
		if(type != null){
			Element top = doc.createElement("TypeExpr");
			parent.appendChild(top);
			top.setAttribute("TODO", "Types are not supported by XML writer");
			//TODO
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
	private void generateXML(GeneratorFilter[] generators, Element p) {
		if(generators != null && generators.length>0){
			Element top = doc.createElement("GeneratorFilters");
			p.appendChild(top);
			for(GeneratorFilter gen : generators){
				generateXML(gen, top);
			}
		}
	}

	private void generateXML(GeneratorFilter gen, Element p) {
		Element top = doc.createElement("GenertatorFilter");
		p.appendChild(top);
		generateXML(gen.getVariables(), top);
		Element collExpr = doc.createElement("CollectionExpression");
		top.appendChild(collExpr);
		gen.getCollectionExpr().accept(this, collExpr);
		generateXML(gen.getFilters(), top, "Filters");
	}
/******************************************************************************
 * Expression
 */
	public void generateXML(Expression[] exprVector, Element p, String lablel){
		if(exprVector != null && exprVector.length>0){
			Element top = doc.createElement(lablel);
			p.appendChild(top);
			for(Expression e : exprVector){
				e.accept(this, top);
			}
		}
	}
	public Void visitExprApplication(ExprApplication e, Element p) {
		Element appl = doc.createElement("ExprApplication");
		p.appendChild(appl);
		//--- function
		Element fun = doc.createElement("Function");
		e.getFunction().accept(this, fun);
		appl.appendChild(fun);
		//--- arguments
		Element args = doc.createElement("Arguments");
		if(e.getArgs() != null && e.getArgs().length<0){
			appl.appendChild(args);
			for(Expression arg : e.getArgs()){
				arg.accept(this, args);
			}
		}
		return null;
	}
	@Override
	public Void visitExprBinaryOp(ExprBinaryOp e, Element p) {
		Element binOp = doc.createElement("ExprBinaryOp");
		p.appendChild(binOp);
		//--- operations
		Element allOperations = doc.createElement("Operations");
		binOp.appendChild(allOperations);
		for(String opString : e.getOperations()){
			Element operationElement = doc.createElement("Operation");
			operationElement.setAttribute("operation", opString);
			allOperations.appendChild(operationElement);
		}
		//--- operands
		Element allOperands = doc.createElement("Operands");
		binOp.appendChild(allOperands);
		for(Expression operand : e.getOperands()){
			operand.accept(this, allOperands);
		}
		return null;
	}
	@Override
	public Void visitExprEntry(ExprEntry e, Element p) {
		Element elem = doc.createElement("ExprEntry");
		p.appendChild(elem);
		elem.setAttribute("field", e.getName());
		e.getEnclosingExpr().accept(this, elem);
		return null;
	}
	@Override
	public Void visitExprIf(ExprIf e, Element p) {
		Element elem = doc.createElement("ExprIf");
		p.appendChild(elem);
		//--- condition
		Element cond = doc.createElement("Condition");
		elem.appendChild(cond);
		e.getCondition().accept(this, cond);
		//--- true branch
		Element trueExpr = doc.createElement("TrueExpr");
		elem.appendChild(trueExpr);
		e.getThenExpr().accept(this, trueExpr);
		//--- else expr
		Element elseExpr = doc.createElement("ElseExpr");
		elem.appendChild(elseExpr);
		e.getElseExpr().accept(this, elseExpr);
		return null;
	}
	@Override
	public Void visitExprIndexer(ExprIndexer e, Element p) {
		Element top = doc.createElement("ExprIndexer");
		p.appendChild(top);
		// structure
		Element struct = doc.createElement("Structure");
		top.appendChild(struct);
		e.getStructure().accept(this, struct);
		// location
		Element location = doc.createElement("Location");
		top.appendChild(location);
		for(Expression arg : e.getLocation()){
			arg.accept(this, location);
		}
		return null;
	}
	@Override
	public Void visitExprInput(ExprInput e, Element p) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Void visitExprLambda(ExprLambda e, Element p) {
		//TODO out.append("const ");
		Element top = doc.createElement("ExprLambda");
		p.appendChild(top);
		generateXML(e.getValueParameters(), top);
		//TODO type parameters
		if(e.getReturnType() != null){
			Element rt = doc.createElement("ReturnType");
			top.appendChild(rt);
			generateXML(e.getReturnType(), rt);
		}
		generateXML(e.getVarDecls(), top);
		Element body = doc.createElement("BodyExpr");
		top.appendChild(body);
		e.getBody().accept(this, body);
		return null;
	}
	@Override
	public Void visitExprLet(ExprLet e, Element p) {
		Element top = doc.createElement("ExprLet");
		p.appendChild(top);
		generateXML(e.getVarDecls(), top);
		//TODO type declarations
		Element body = doc.createElement("BodyExpr");
		top.appendChild(body);
		e.getBody().accept(this, body);
		out.append(" endlet");
		return null;
	}
	@Override
	public Void visitExprList(ExprList e, Element p) {
		Element top = doc.createElement("ExprList");
		p.appendChild(top);
		generateXML(e.getElements(), top, "Elements");
		generateXML(e.getGenerators(), top);
		//TODO tail
		return null;
	}
	@Override
	public Void visitExprLiteral(ExprLiteral e, Element p) {
		Element litteralElement = doc.createElement("ExprLiteral");
		litteralElement.setAttribute("text", e.getText());
		p.appendChild(litteralElement);
		return null;
	}
	@Override
	public Void visitExprMap(ExprMap e, Element p) {
		Element top = doc.createElement("ExprMap");
		p.appendChild(top);
		Element maps = doc.createElement("Maps");
		top.appendChild(maps);
		for(Map.Entry<Expression, Expression> body : e.getMappings()){
			Element map = doc.createElement("MapEntry");
			maps.appendChild(map);
			Element key = doc.createElement("MapKey");
			map.appendChild(key);
			body.getKey().accept(this, key);
			Element value = doc.createElement("MapValue");
			map.appendChild(value);
			body.getValue().accept(this, value);
		}
		generateXML(e.getGenerators(), top);
		return null;
	}
	@Override
	public Void visitExprProc(ExprProc e, Element p) {
		Element top = doc.createElement("ExprProc");
		p.appendChild(top);
		generateXML(e.getValueParameters(), top);
		//TODO type parameters
		generateXML(e.getVarDecls(), top);
		generateXML(e.getBody(), top, "BodyStmt");
		return null;
	}
	@Override
	public Void visitExprSet(ExprSet e, Element p) {
		Element top = doc.createElement("ExprSet");
		p.appendChild(top);
		generateXML(e.getElements(), top, "Elements");
		generateXML(e.getGenerators(), top);
		return null;
	}
	@Override
	public Void visitExprUnaryOp(ExprUnaryOp e, Element p) {
		Element op = doc.createElement("UnaryOp");
		op.setAttribute("operation", e.getOperation());
		e.getOperand().accept(this, op);
		p.appendChild(op);
		return null;
	}
	@Override
	public Void visitExprVariable(ExprVariable e, Element p) {
		Element var = doc.createElement("ExprVariable");
		var.setAttribute("name", e.getName());
		p.appendChild(var);
		return null;
	}
/******************************************************************************
 * Statement
 */
	void generateXML(Statement[] body, Element p, String label) {
		Element top = doc.createElement(label);
		p.appendChild(top);
		for(Statement s : body){
			s.accept(this, top);
		}
	}
	@Override
	public Void visitStmtBlock(StmtBlock s, Element p) {
		Element top = doc.createElement("StmtBlock");
		p.appendChild(top);
		return null;
	}
	@Override
	public Void visitStmtIf(StmtIf s, Element p) {
		Element top = doc.createElement("StmtIf");
		p.appendChild(top);
		return null;
	}
	@Override
	public Void visitStmtCall(StmtCall s, Element p) {
		Element top = doc.createElement("StmtCall");
		p.appendChild(top);
		return null;
	}
	@Override
	public Void visitStmtOutput(StmtOutput s, Element p) {
		Element top = doc.createElement("StmtOutput");
		p.appendChild(top);
		return null;
	}
	@Override
	public Void visitStmtWhile(StmtWhile s, Element p) {
		Element top = doc.createElement("StmtWhile");
		p.appendChild(top);
		return null;
	}
	@Override
	public Void visitStmtForeach(StmtForeach s, Element p) {
		Element top = doc.createElement("StmtForeach");
		p.appendChild(top);
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Void visitStmtAssignment(StmtAssignment s, Element p) {
		Element top = doc.createElement("StmtAssignment");
		p.appendChild(top);
		top.setAttribute("variable", s.getVar());
		if(s.getField() != null){
			Element field = doc.createElement("Field");
			top.appendChild(field);
			field.setAttribute("name", s.getField());
		} else if(s.getLocation() != null && s.getLocation().length>0){
			Element location = doc.createElement("Locations");
			top.appendChild(location);
			for(Expression arg : s.getLocation()){
				arg.accept(this, location);
			}
		}
		Element val = doc.createElement("Value");
		top.appendChild(val);
		s.getVal().accept(this, val);
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
//		print(s.getStatements());
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
//		s.getThenBranch().accept(this);
		if(s.getElseBranch() != null){
			decIndent();
			indent();
			out.append("else");
			incIndent();
			indent();
//			s.getThenBranch().accept(this);
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
	@Override
	public Void visitStmtConsume(StmtConsume s, Element p) {
		// TODO Auto-generated method stub
		return null;
	}
	public Void visitStmtOutput(StmtOutput s, Void p) {
		out.append("output;");
		// TODO output statement
		return null;
	}
	public Void visitStmtWhile(StmtWhile s, Void p) {
		out.append("while ");
		s.getCondition().accept(this, null);
		indent();
//		s.getBody().accept(this);
		indent();
		out.append("endwhile");
		return null;
	}
	public Void visitStmtForeach(StmtForeach s, Void p) {
		if(s.getGenerators() != null && s.getGenerators().length>0){
//			print(s.getGenerators(), "foreach");
		}
		out.append(" do");
		indent();
//		s.getBody().accept(this);
		indent();
		out.append("endforeach");
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
//			print(e.getGenerators(), "for");
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
//		print(stmt.getGenerators(), "foreach");
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
