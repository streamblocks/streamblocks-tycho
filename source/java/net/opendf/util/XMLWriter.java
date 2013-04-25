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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import net.opendf.ir.cal.*;
import net.opendf.ir.common.*;
import net.opendf.ir.net.ast.EntityExpr;
import net.opendf.ir.net.ast.EntityExprVisitor;
import net.opendf.ir.net.ast.EntityIfExpr;
import net.opendf.ir.net.ast.EntityInstanceExpr;
import net.opendf.ir.net.ast.EntityListExpr;
import net.opendf.ir.net.ast.NetworkDefinition;
import net.opendf.ir.net.ast.StructureConnectionStmt;
import net.opendf.ir.net.ast.StructureForeachStmt;
import net.opendf.ir.net.ast.StructureIfStmt;
import net.opendf.ir.net.ast.StructureStmtVisitor;

public class XMLWriter implements ExpressionVisitor<Void,Element>, StatementVisitor<Void, Element>, EntityExprVisitor<Void, Void>, StructureStmtVisitor<Void, Void>, LValueVisitor<Void, Element>{
	private java.io.PrintStream out = System.out;
	Document doc;

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
			
			Element networkElement = doc.createElement("NetworkDefinition");
			doc.appendChild(networkElement);
			networkElement.setAttribute("name", network.getName());
			//-- type/value parameters, in/out ports, type/value declarations
			generateEntityXML(network, networkElement);
			
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
			//-- type/value parameters, in/out ports, type/value declarations
			generateEntityXML(actor, actorElement);

		//--- initializers
		//--- action
		//--- schedule
		//--- priorities
		//--- invariants
		}catch(ParserConfigurationException pce){
			pce.printStackTrace();
		}
	}
	//-- type/value parameters, in/out ports, type/value declarations
	public void generateEntityXML(DeclEntity entity, Element top){
		//-- type parameters 
		//TODO
		//-- value parameters 
		generateXML(entity.getValueParameters(), top);
		//-- input ports 
		generateXML(entity.getInputPorts(), top, "InputPortList");
		//-- output ports 
		generateXML(entity.getInputPorts(), top, "OutputPortList");
		//--- type declaration
		generateXML(entity.getTypeDecls(), top);
		//--- variable declaration
		generateXML(entity.getVarDecls(), top);
	}
	public void generateXML(List<PortDecl> ports, Element parent, String kind){
		if(ports != null && !ports.isEmpty()){
			Element top = doc.createElement(kind);
			parent.appendChild(top);
			for(PortDecl port : ports){
				Element elem = doc.createElement("PortDecl");
				top.appendChild(elem);
				elem.setAttribute("name", port.getName());
				if(port.getType() != null){
					generateXML(port.getType(), elem);
				}
			}
		}
	}
	public void generateXML(DeclType[] typeDecls, Element parent){
		if(typeDecls != null && typeDecls.length>0){
			Element declTypeList = doc.createElement("DeclTypeList");
			for(DeclType typeDecl : typeDecls){
				Element typeDeclElem = doc.createElement("DeclType");
				typeDeclElem.setAttribute("name", typeDecl.getName());
				declTypeList.appendChild(typeDeclElem);
			}
			parent.appendChild(declTypeList);
		}
	}
	public void generateXML(DeclVar[] varDecls, Element parent){
		if(varDecls != null && varDecls.length>0){
			Element declVarList = doc.createElement("DeclVarList");
			for(DeclVar v : varDecls){
				Element varDeclElem = doc.createElement("DeclVar");
				varDeclElem.setAttribute("name", v.getName());
				if(v.getInitialValue() != null){
					Element initElem = doc.createElement("InitialValue");
					v.getInitialValue().accept(this, initElem);
					varDeclElem.appendChild(initElem);
				}
				declVarList.appendChild(varDeclElem);
			}
			parent.appendChild(declVarList);
		}
	}
	
	private void generateXML(Port port, Element p) {
		Element e = doc.createElement("Port");
		p.appendChild(e);
		e.setAttribute("name", port.toString());
		if(port.hasLocation()){
			e.setAttribute("offset", Integer.toString(port.getOffset()));
		}
	}
	private void generateXML(ParDeclValue[] valueParameters, Element p) {
		if(valueParameters != null && valueParameters.length > 0){
			Element top = doc.createElement("ValueParameterList");
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
	private void generateXML(GeneratorFilter[] generators, Element p) {
		if(generators != null && generators.length>0){
			Element top = doc.createElement("GeneratorFilterList");
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
	private void generateXML(Variable var, Element p){
		Element varElem = doc.createElement("Variable");
		p.appendChild(varElem);
		varElem.setAttribute("name", var.getName());
		if(var.isDynamic()){
			varElem.setAttribute("level", Integer.toString(var.getLevel()));
		}
		if(var.hasLocation()){
			varElem.setAttribute("offset", Integer.toString(var.getOffset()));
		}
	}
	private void generateXML(Field f, Element p){
		Element fieldElem = doc.createElement("Field");
		p.appendChild(fieldElem);
		fieldElem.setAttribute("name", f.getName());
		if(f.hasOffset()){
			fieldElem.setAttribute("offset", Integer.toString(f.getOffset()));
		}
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
		generateXML(e.getArgs(), appl, "ArgumentList");
		return null;
	}
	@Override
	public Void visitExprBinaryOp(ExprBinaryOp e, Element p) {
		Element binOp = doc.createElement("ExprBinaryOp");
		p.appendChild(binOp);
		//--- operations
		Element allOperations = doc.createElement("OperationList");
		binOp.appendChild(allOperations);
		for(String opString : e.getOperations()){
			Element operationElement = doc.createElement("Operation");
			operationElement.setAttribute("operation", opString);
			allOperations.appendChild(operationElement);
		}
		//--- operands
		Element allOperands = doc.createElement("OperandList");
		binOp.appendChild(allOperands);
		for(Expression operand : e.getOperands()){
			operand.accept(this, allOperands);
		}
		return null;
	}
	@Override
	public Void visitExprField(ExprField e, Element p) {
		Element elem = doc.createElement("ExprField");
		p.appendChild(elem);
		e.getStructure().accept(this, elem);
		generateXML(e.getField(), elem);
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
		e.getIndex().accept(this, location);
		return null;
	}
	@Override
	public Void visitExprInput(ExprInput e, Element p) {
		Element top = doc.createElement("ExprInput");
		p.appendChild(top);
		top.setAttribute("offset", Integer.toString(e.getOffset()));
		top.setAttribute("hasRepeat", e.hasRepeat() ? "true" : "false");
		if(e.hasRepeat()){
			top.setAttribute("repeat", Integer.toString(e.getRepeat()));
			top.setAttribute("patternLength", Integer.toString(e.getPatternLength()));
		}
		generateXML(e.getPort(), top);
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
		generateXML(e.getElements(), top, "ElementList");
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
		generateXML(e.getElements(), top, "ElementList");
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
		var.setAttribute("name", e.getVariable().getName());
		p.appendChild(var);
		return null;
	}
	
/******************************************************************************
 * LValue
 */
	@Override
	public Void visitLValueVariable(LValueVariable lvalue, Element p) {
		Element var = doc.createElement("LValueVariable");
		p.appendChild(var);
		generateXML(lvalue.getVariable(), var);
		return null;
	}
	@Override
	public Void visitLValueIndexer(LValueIndexer lvalue, Element p) {
		Element top = doc.createElement("LValueIndexer");
		p.appendChild(top);
		//-- structure
		lvalue.getStructure().accept(this, top);
		//-- index
		Element index = doc.createElement("Index");
		top.appendChild(index);
		lvalue.getIndex().accept(this, index);
		return null;
	}
	@Override
	public Void visitLValueField(LValueField lvalue, Element p) {
		Element top = doc.createElement("LValueField");
		p.appendChild(top);
		//-- structure
		lvalue.getStructure().accept(this, top);
		//-- field
		generateXML(lvalue.getField(), top);
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
	public Void visitStmtAssignment(StmtAssignment s, Element p) {
		Element top = doc.createElement("StmtAssignment");
		p.appendChild(top);
		s.getLValue().accept(this, top);
		Element v = doc.createElement("Value");
		top.appendChild(v);
		s.getExpression().accept(this, v);
		return null;
	}
	@Override
	public Void visitStmtBlock(StmtBlock s, Element p) {
		Element top = doc.createElement("StmtBlock");
		p.appendChild(top);
		generateXML(s.getTypeDecls(), top);
		generateXML(s.getVarDecls(), top);
		Element stmtElement = doc.createElement("StatementList");
		top.appendChild(stmtElement);
		for(Statement stmt : s.getStatements()){
			stmt.accept(this, stmtElement);
		}
		return null;
	}
	@Override
	public Void visitStmtCall(StmtCall s, Element p) {
		Element top = doc.createElement("StmtCall");
		p.appendChild(top);
		Element proc = doc.createElement("Procedure");
		top.appendChild(proc);
		s.getProcedure().accept(this, proc);
		generateXML(s.getArgs(), top, "ArgumentList");
		return null;
	}
	@Override
	public Void visitStmtConsume(StmtConsume s, Element p) {
		Element top = doc.createElement("StmtConsume");
		p.appendChild(top);
		top.setAttribute("numberOfTokens", Integer.toString(s.getNumberOfTokens()));
		generateXML(s.getPort(), top);
		return null;
	}
	@Override
	public Void visitStmtIf(StmtIf s, Element p) {
		Element top = doc.createElement("StmtIf");
		p.appendChild(top);
		//-- condition
		Element cond = doc.createElement("Condition");
		top.appendChild(cond);
		s.getCondition().accept(this, cond);
		//-- then branch
		Element thenElement = doc.createElement("ThenBranch");
		top.appendChild(thenElement);
		s.getThenBranch().accept(this, thenElement);
		//-- else branch
		if(s.getElseBranch() != null){
			Element elseElement = doc.createElement("ElseBranch");
			top.appendChild(elseElement);
			s.getElseBranch().accept(this, elseElement);
		}
		return null;
	}
	@Override
	public Void visitStmtForeach(StmtForeach s, Element p) {
		Element top = doc.createElement("StmtForeach");
		p.appendChild(top);
		//-- body
		Element bodyElement = doc.createElement("Body");
		top.appendChild(bodyElement);
		s.getBody().accept(this, bodyElement);
		generateXML(s.getGenerators(), top);
		return null;
	}
	@Override
	public Void visitStmtOutput(StmtOutput s, Element p) {
		Element top = doc.createElement("StmtOutput");
		p.appendChild(top);
		top.setAttribute("hasRepeat", s.hasRepeat() ? "true" : "false");
		if(s.hasRepeat()){
			top.setAttribute("repeat", Integer.toString(s.getRepeat()));
		}
		generateXML(s.getPort(), top);
		generateXML(s.getValues(), top, "ValueList");
		return null;
	}
	@Override
	public Void visitStmtWhile(StmtWhile s, Element p) {
		Element top = doc.createElement("StmtWhile");
		p.appendChild(top);
		//-- condition
		Element cond = doc.createElement("Condition");
		top.appendChild(cond);
		s.getCondition().accept(this, cond);
		//-- body
		Element bodyElement = doc.createElement("Body");
		top.appendChild(bodyElement);
		s.getBody().accept(this, bodyElement);
		return null;
	}
	
/******************************************************************************
 * EntityExpr (Network)
 */
	public void print(EntityExpr entity){
		entity.accept(this, null);
	}
	public Void visitEntityInstanceExpr(EntityInstanceExpr e, Void p) {
		return null;
	}
	public Void visitEntityIfExpr(EntityIfExpr e, Void p) {
		return null;
	}
	public Void visitEntityListExpr(EntityListExpr e, Void p) {
		return null;
	}
/******************************************************************************
 * Structure Statement (Network)
 */
	public Void visitStructureConnectionStmt(StructureConnectionStmt stmt, Void p) {
		return null;
	}
	public Void visitStructureIfStmt(StructureIfStmt stmt, Void p) {
		return null;
	}
	public Void visitStructureForeachStmt(StructureForeachStmt stmt, Void p) {
		return null;
	}
}
