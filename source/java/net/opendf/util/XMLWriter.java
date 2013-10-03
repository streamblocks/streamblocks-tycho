package net.opendf.util;

/**
 *  copyright (c) 2013, Per Andersson
 *  
 *  This code translates a cal IR to an XML document (org.w3c.dom.Document)
 *  The XML document is built top down. 
 *  The parameter to all visit methods is the parent XML node.
 **/

import java.io.ByteArrayOutputStream;
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

import net.opendf.interp.VariableLocation;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Condition;
import net.opendf.ir.am.ICall;
import net.opendf.ir.am.ITest;
import net.opendf.ir.am.IWait;
import net.opendf.ir.am.Instruction;
import net.opendf.ir.am.InstructionVisitor;
import net.opendf.ir.am.PortCondition;
import net.opendf.ir.am.PredicateCondition;
import net.opendf.ir.am.Scope;
import net.opendf.ir.am.State;
import net.opendf.ir.am.Transition;
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
import net.opendf.ir.util.ImmutableList;

public class XMLWriter implements ExpressionVisitor<Void,Element>, 
                                  StatementVisitor<Void, Element>, 
                                  EntityExprVisitor<Void, Element>, 
                                  StructureStmtVisitor<Void, Element>, 
                                  LValueVisitor<Void, Element>, 
                                  InstructionVisitor<Void, Element>{
	private java.io.PrintStream out = System.out;
	Document doc;

	public Document getDocument(){ return doc; }
	public void print(){
        try {
        	OutputFormat format = new OutputFormat(doc);
        	format.setLineWidth(80);
        	format.setIndenting(true);
        	format.setIndent(2);
        	XMLSerializer serializer = new XMLSerializer(out, format);
        	serializer.serialize(doc);
        } catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String toString(){
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
        	OutputFormat format = new OutputFormat(doc);
        	format.setLineWidth(80);
        	format.setIndenting(true);
        	format.setIndent(2);
        	XMLSerializer serializer = new XMLSerializer(out, format);
        	serializer.serialize(doc);
        } catch (IOException e) {
			e.printStackTrace();
		}
        return out.toString();
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
			generateXMLForDeclEntity(network, networkElement);
			generateXMLForEntityExprList(network.getEntities(), networkElement);
			generateXMLForStructureStmtList(network.getStructure(), networkElement);
			generateXMLForToolAttributeList(network.getToolAttributes(), networkElement);
			
		}catch(ParserConfigurationException pce){
			pce.printStackTrace();
		}
	}
	public void generateXMLForToolAttributeList(
			ImmutableList<ToolAttribute> toolAttributes, Element p) {
		// TODO Auto-generated method stub
		
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
			generateXMLForDeclEntity(actor, actorElement);
			generateXMLForActions(actor.getInitializers(), actorElement);
			generateXMLForActions(actor.getActions(), actorElement);
			generateXMLForSchedule(actor.getScheduleFSM(), actorElement);
			generateXMLForPriorityList(actor.getPriorities(), actorElement);
			generateXMLForExpressionList(actor.getInvariants(), actorElement, "InvariantList");
		}catch(ParserConfigurationException pce){
			pce.printStackTrace();
		}
	}
	private void generateXMLForSchedule(ScheduleFSM scheduleFSM, Element p) {
		if(scheduleFSM == null) return;
		Element top = doc.createElement("ScheduleFSM");
		p.appendChild(top);
		top.setAttribute("initialState", scheduleFSM.getInitialState());
		generateXMLForFSMTransitionList(scheduleFSM.getTransitions(), top);
	}
	private void generateXMLForFSMTransitionList(ImmutableList<net.opendf.ir.cal.Transition> list, Element p) {
		Element top = doc.createElement("TransitionListFSM");
		p.appendChild(top);
		for(net.opendf.ir.cal.Transition t : list){
			generateXMLForFSMTransition(t, top);
		}
	}
	private void generateXMLForFSMTransition(net.opendf.ir.cal.Transition t, Element p) {
		Element top = doc.createElement("TransitionFSM");
		p.appendChild(top);
		top.setAttribute("sourceState", t.getSourceState());
		top.setAttribute("destinationState", t.getDestinationState());
		Element actions = doc.createElement("ActionList");
		top.appendChild(actions);
		for(QID id:t.getActionTags()){
			generateXMLForQID(id, actions);
		}
	}
	private void generateXMLForPriorityList(
			ImmutableList<ImmutableList<QID>> list, Element p) {
		if(list==null || list.size()==0){ return; }
		Element top = doc.createElement("PriorityList");
		p.appendChild(top);
		for(ImmutableList<QID> pri : list){
			generateXMLForPriority(pri, top);
		}
	}
	private void generateXMLForPriority(ImmutableList<QID> pri, Element p) {
		Element top = doc.createElement("Priority");
		p.appendChild(top);
		for(QID id : pri){
			generateXMLForQID(id, top);
		}
	}
	private void generateXMLForActions(ImmutableList<Action> actions, Element parent) {
		for(Action a : actions){
			generateXMLForAction(a, parent);
		}
	}
	private void generateXMLForAction(Action action, Element parent) {
		Element top = doc.createElement("Action");
		parent.appendChild(top);
		if(action.getInputPatterns()==null || action.getInputPatterns().size()==0){
			top.setAttribute("Initializer", "true");
		}
		generateXMLForQID(action.getTag(), top);
		generateXMLForInputPatterns(action.getInputPatterns(), top);
		generateXMLForDeclTypeList(action.getTypeDecls(), top);
		generateXMLForDeclVarList(action.getVarDecls(), top);
		generateXMLForExpressionList(action.getGuards(), top, "GuardList");
		generateXMLForStatementList(action.getBody(), top);
		generateXMLForOutputExpressionList(action.getOutputExpressions(), top);
		if(action.getDelay() != null){
			action.getDelay().accept(this, top);
		}
		generateXMLForExpressionList(action.getPreconditions(), top, "PreConditionList");
		generateXMLForExpressionList(action.getPostconditions(), top, "PostConditionList");		
	}
	private void generateXMLForOutputExpressionList(ImmutableList<OutputExpression> outputExpressions, Element p) {
		Element top = doc.createElement("OutputExpressionList");
		p.appendChild(top);
		for(OutputExpression o : outputExpressions){
			generateXMLForOutputExpression(o, top);
		}
	}
	private void generateXMLForOutputExpression(OutputExpression output, Element p) {
		Element top = doc.createElement("OutputExpression");
		p.appendChild(top);
		generateXMLForPort(output.getPort(), top);
		generateXMLForExpressionList(output.getExpressions(), top, "OutputExpressions");
		Element rep = doc.createElement("Repeat");
		top.appendChild(rep);
		if(output.getRepeatExpr() != null){
			output.getRepeatExpr().accept(this, rep);
		}
	}
	private void generateXMLForInputPatterns(
			ImmutableList<InputPattern> inputPatterns, Element p) {
		Element top = doc.createElement("InputtPatternList");
		p.appendChild(top);
		for(InputPattern i : inputPatterns){
			generateXMLForInputPattern(i, top);
		}
	}
	private void generateXMLForInputPattern(InputPattern input, Element p) {
		Element top = doc.createElement("InputPattern");
		p.appendChild(top);
		generateXMLForPort(input.getPort(), p);
		generateXMLForDeclVarList(input.getVariables(), p);
		Element rep = doc.createElement("Repeat");
		top.appendChild(rep);
		if(input.getRepeatExpr() != null){
			input.getRepeatExpr().accept(this, rep);
		}
	}
	private void generateXMLForQID(QID tag, Element parent) {
		if(tag != null){
			Element qid = doc.createElement("QID");
			parent.appendChild(qid);
			qid.setAttribute("name", tag.toString());
		}
	}
	//-- type/value parameters, in/out ports, type/value declarations
	public void generateXMLForDeclEntity(DeclEntity entity, Element top){
		//-- type parameters 
		//TODO
		//-- value parameters 
		generateXMLForParDeclValueList(entity.getValueParameters(), top);
		//-- input ports 
		generateXMLForPortDeclList(entity.getInputPorts(), top, "InputPortList");
		//-- output ports 
		generateXMLForPortDeclList(entity.getOutputPorts(), top, "OutputPortList");
		//--- type declaration
		generateXMLForDeclTypeList(entity.getTypeDecls(), top);
		//--- variable declaration
		generateXMLForDeclVarList(entity.getVarDecls(), top);
	}
	/******************************************************************************
	 * Actor Machine
	 */
	public XMLWriter(ActorMachine am){
		try{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();

			Element amElement = doc.createElement("ActorMachine");
			doc.appendChild(amElement);
			generateXMLForConditionList(am.getConditions(), amElement);
			generateXMLForAMController(am.getController(), amElement);
			generateXMLForPortDeclList(am.getInputPorts(), amElement, "InputPortList");
			generateXMLForPortDeclList(am.getOutputPorts(), amElement, "OutputPortList");
			generateXMLForAMScopeList(am.getScopes(), amElement);
			generateXMLForAMTransitionList(am.getTransitions(), amElement);
		}catch(ParserConfigurationException pce){
			pce.printStackTrace();
		}
	}
	private void generateXMLForAMTransitionList(ImmutableList<Transition> transitions, Element p) {
		if(transitions != null && !transitions.isEmpty()){
			Element top = doc.createElement("TransitionList");
			p.appendChild(top);
			int index = 0;
			for(Transition t : transitions){
				Element transElem = doc.createElement("Transition");
				top.appendChild(transElem);
				transElem.setAttribute("index", Integer.toString(index));
				t.getBody().accept(this, transElem);
				if(!t.getInputRates().isEmpty()){
					Element rates = doc.createElement("InputRateList");
					transElem.appendChild(rates);
					for(Map.Entry<Port,Integer> r : t.getInputRates().entrySet()){
						Element rateElem = doc.createElement("InputRate");
						rates.appendChild(rateElem);
						rateElem.setAttribute("rate", r.getValue().toString());
						generateXMLForPort(r.getKey(), rateElem);
					}
				} // end input rates
				Element killListElem = doc.createElement("ScopesToKill");
				transElem.appendChild(killListElem);
				for(Integer scopeIndex : t.getScopesToKill()){
					Element scopeElem = doc.createElement("Scope");
					killListElem.appendChild(scopeElem);
					scopeElem.setAttribute("index", scopeIndex.toString());
				} // end scopes to kill
				if(!t.getOutputRates().isEmpty()){
					Element rates = doc.createElement("OutputRateList");
					transElem.appendChild(rates);
					for(Map.Entry<Port,Integer> r : t.getOutputRates().entrySet()){
						Element rateElem = doc.createElement("OutputRate");
						rates.appendChild(rateElem);
						rateElem.setAttribute("rate", r.getValue().toString());
						generateXMLForPort(r.getKey(), rateElem);
					}
				} // output rates
			index++;
			}
		}
	}
	private void generateXMLForAMScopeList(ImmutableList<Scope> scopes, Element p) {
		if(scopes != null && !scopes.isEmpty()){
			Element top = doc.createElement("ScopeList");
			p.appendChild(top);
			int scopeId = 0;
			for(Scope scope : scopes){
				Element list = generateXMLForDeclVarList(scope.getDeclarations(), top, "Scope", true);
				if(list != null){
					list.setAttribute("scopeID", Integer.toString(scopeId++));
				}
			}
		}		
	}
	private void generateXMLForAMController(ImmutableList<State> controller, Element parent) {
		if(controller != null && !controller.isEmpty()){
			Element top = doc.createElement("Controller");
			parent.appendChild(top);
			int stateNbr = 0;
			for(State state : controller){
				Element sElem = doc.createElement("ControllerState");
				top.appendChild(sElem);
				sElem.setAttribute("index", Integer.toString(stateNbr));
				for(Instruction i : state.getInstructions()){
					i.accept(this, sElem);
				}
				stateNbr++;
			}
		}
	}
	@Override
	public Void visitWait(IWait i, Element p) {
		Element inst = doc.createElement("IWait");
		p.appendChild(inst);
		inst.setAttribute("nextState", Integer.toString(i.S()));
		return null;
	}
	@Override
	public Void visitTest(ITest i, Element p) {
		Element inst = doc.createElement("ITest");
		p.appendChild(inst);
		inst.setAttribute("cond", Integer.toString(i.C()));
		inst.setAttribute("trueState", Integer.toString(i.S1()));
		inst.setAttribute("falseState", Integer.toString(i.S0()));
		return null;
	}
	@Override
	public Void visitCall(ICall i, Element p) {
		Element inst = doc.createElement("ICal");
		p.appendChild(inst);
		inst.setAttribute("nextState", Integer.toString(i.S()));
		inst.setAttribute("transition", Integer.toString(i.T()));
		return null;
	}

	public void generateXMLForConditionList(ImmutableList<Condition> cl, Element parent){
		if(cl != null && !cl.isEmpty()){
			int index = 0;
			Element top = doc.createElement("ConditionList");
			parent.appendChild(top);
			for(Condition cond : cl){
				switch (cond.kind()){
				case input:
				case output:{
					PortCondition c = (PortCondition)cond;
					Element e = doc.createElement("PortCondition");
					top.appendChild(e);
					e.setAttribute("isInputCondition", Boolean.toString(c.isInputCondition()));
					e.setAttribute("n", Integer.toString(c.N()));
					e.setAttribute("index", Integer.toString(index));
					generateXMLForPort(c.getPortName(), e);
					break;
					}
				case predicate:{
					Element e = doc.createElement("PredicateCondition");
					top.appendChild(e);
					e.setAttribute("index", Integer.toString(index));
					PredicateCondition c = (PredicateCondition) cond;
					((Expression)c.getExpression()).accept(this, e);
					break;
					}
				}
				index++;
			}
			
		}
	}
	/******************************************************************************
	 * Declaration
	 */
	public void generateXMLForPortDeclList(List<PortDecl> ports, Element parent, String kind){
		if(ports != null && !ports.isEmpty()){
			Element top = doc.createElement(kind);
			parent.appendChild(top);
			for(PortDecl port : ports){
				Element elem = doc.createElement("PortDecl");
				top.appendChild(elem);
				elem.setAttribute("name", port.getName());
				if(port.getType() != null){
					generateXMLForTypeExpr(port.getType(), elem);
				}
			}
		}
	}
	public void generateXMLForDeclTypeList(List<DeclType> immutableList, Element parent){
		if(immutableList != null && !immutableList.isEmpty()){
			Element declTypeList = doc.createElement("DeclTypeList");
			for(DeclType typeDecl : immutableList){
				Element typeDeclElem = doc.createElement("DeclType");
				typeDeclElem.setAttribute("name", typeDecl.getName());
				declTypeList.appendChild(typeDeclElem);
			}
			parent.appendChild(declTypeList);
		}
	}
	public Element generateXMLForDeclVarList(List<DeclVar> varDecls, Element parent){
		return generateXMLForDeclVarList(varDecls, parent, "DeclVarList", false);
	}
	public Element generateXMLForDeclVarList(List<DeclVar> varDecls, Element parent, String topName, boolean printEmptyList){
		Element declVarList = null;
		if(varDecls != null && (printEmptyList || !varDecls.isEmpty())){
			declVarList = doc.createElement(topName);
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
		return declVarList;
	}
	
	private void generateXMLForPort(Port port, Element p) {
		if(port != null){
			Element e = doc.createElement("Port");
			p.appendChild(e);
			e.setAttribute("name", port.getName());
			if(port.hasLocation()){
				e.setAttribute("offset", Integer.toString(port.getOffset()));
			}
		}
	}
	private void generateXMLForParDeclValueList(List<ParDeclValue> valueParameters, Element p) {
		if(valueParameters != null && !valueParameters.isEmpty()){
			Element top = doc.createElement("ValueParameterList");
			p.appendChild(top);
			for(ParDeclValue param : valueParameters){
				generateXMLForParDeclValue(param, top);
			}
		}
	}
	private void generateXMLForParDeclValue(ParDeclValue param, Element p) {
		Element top = doc.createElement("ParDeclValue");
		p.appendChild(top);
		top.setAttribute("name", param.getName());
		generateXMLForTypeExpr(param.getType(), top);
	}
	public void generateXMLForTypeExpr(TypeExpr type, Element parent){
		if(type != null){
			Element top = doc.createElement("TypeExpr");
			parent.appendChild(top);
			top.setAttribute("TODO", "Types are not supported by XML writer");
			//TODO
		}
	}
	private void generateXMLForGeneratorFilterList(List<GeneratorFilter> generators, Element p) {
		if(generators != null && !generators.isEmpty()){
			Element top = doc.createElement("GeneratorFilterList");
			p.appendChild(top);
			for(GeneratorFilter gen : generators){
				generateXMLForGeneratorFilter(gen, top);
			}
		}
	}

	private void generateXMLForGeneratorFilter(GeneratorFilter gen, Element p) {
		Element top = doc.createElement("GenertatorFilter");
		p.appendChild(top);
		generateXMLForDeclVarList(gen.getVariables(), top);
		Element collExpr = doc.createElement("CollectionExpression");
		top.appendChild(collExpr);
		gen.getCollectionExpr().accept(this, collExpr);
		generateXMLForExpressionList(gen.getFilters(), top, "Filters");
	}
	private void generateXMLForVariable(Variable var, Element p){
		Element varElem = doc.createElement("Variable");
		p.appendChild(varElem);
		varElem.setAttribute("name", var.getName());
		if(var.isScopeVariable()){
			varElem.setAttribute("isScopeVariable", "true");
			varElem.setAttribute("scopeId", Integer.toString(var.getScopeId()));
		} else {
			varElem.setAttribute("isScopeVariable", "false");
		}
		if(var instanceof VariableLocation){
			varElem.setAttribute("offset", Integer.toString(((VariableLocation)var).getOffset()));
		}
	}
	private void generateXMLForField(Field f, Element p){
		Element fieldElem = doc.createElement("Field");
		p.appendChild(fieldElem);
		fieldElem.setAttribute("name", f.getName());
		if(f.hasOffset()){
			fieldElem.setAttribute("offset", Integer.toString(f.getOffset()));
		}
	}
	
	private void generateXMLForFreeVariablelist(
			ImmutableList<Variable> freeVariables, Element p) {
		if(freeVariables != null){
			Element top = doc.createElement("FreeVariables");
			p.appendChild(top);
			for(Variable v : freeVariables){
				generateXMLForVariable(v, top);
			}
		}
	}
/******************************************************************************
 * Expression
 */
	public void generateXMLForExpressionList(List<Expression> exprVector, Element p, String lablel){
		if(exprVector != null && !exprVector.isEmpty()){
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
		generateXMLForExpressionList(e.getArgs(), appl, "ArgumentList");
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
		generateXMLForField(e.getField(), elem);
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
		generateXMLForPort(e.getPort(), top);
		return null;
	}
	@Override
	public Void visitExprLambda(ExprLambda e, Element p) {
		//TODO out.append("const ");
		Element top = doc.createElement("ExprLambda");
		p.appendChild(top);
		generateXMLForParDeclValueList(e.getValueParameters(), top);
		//TODO type parameters
		if(e.getReturnType() != null){
			Element rt = doc.createElement("ReturnType");
			top.appendChild(rt);
			generateXMLForTypeExpr(e.getReturnType(), rt);
		}
		Element body = doc.createElement("BodyExpr");
		top.appendChild(body);
		e.getBody().accept(this, body);
		if(e.isFreeVariablesComputed()){
			generateXMLForFreeVariablelist(e.getFreeVariables(), top);
		}
		return null;
	}
	@Override
	public Void visitExprLet(ExprLet e, Element p) {
		Element top = doc.createElement("ExprLet");
		p.appendChild(top);
		generateXMLForDeclVarList(e.getVarDecls(), top);
		//TODO type declarations
		Element body = doc.createElement("BodyExpr");
		top.appendChild(body);
		e.getBody().accept(this, body);
		return null;
	}
	@Override
	public Void visitExprList(ExprList e, Element p) {
		Element top = doc.createElement("ExprList");
		p.appendChild(top);
		generateXMLForExpressionList(e.getElements(), top, "ElementList");
		generateXMLForGeneratorFilterList(e.getGenerators(), top);
		//TODO tail
		return null;
	}
	@Override
	public Void visitExprLiteral(ExprLiteral e, Element p) {
		Element litteralElement = doc.createElement("ExprLiteral");
		if(e instanceof net.opendf.interp.values.ExprValue){
			litteralElement = doc.createElement("ExprValue");
		} else {
			litteralElement = doc.createElement("ExprLiteral");
		}
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
		generateXMLForGeneratorFilterList(e.getGenerators(), top);
		return null;
	}
	@Override
	public Void visitExprProc(ExprProc e, Element p) {
		Element top = doc.createElement("ExprProc");
		p.appendChild(top);
		generateXMLForParDeclValueList(e.getValueParameters(), top);
		//TODO type parameters
		generateXMLForStatement(e.getBody(), top, "BodyStmt");
		if(e.isFreeVariablesComputed()){
			generateXMLForFreeVariablelist(e.getFreeVariables(), top);
		}
		return null;
	}
	@Override
	public Void visitExprSet(ExprSet e, Element p) {
		Element top = doc.createElement("ExprSet");
		p.appendChild(top);
		generateXMLForExpressionList(e.getElements(), top, "ElementList");
		generateXMLForGeneratorFilterList(e.getGenerators(), top);
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
		p.appendChild(var);
		generateXMLForVariable(e.getVariable(), var);
		return null;
	}
	
/******************************************************************************
 * LValue
 */
	@Override
	public Void visitLValueVariable(LValueVariable lvalue, Element p) {
		Element var = doc.createElement("LValueVariable");
		p.appendChild(var);
		generateXMLForVariable(lvalue.getVariable(), var);
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
		generateXMLForField(lvalue.getField(), top);
		return null;
	}
/******************************************************************************
 * Statement
 */
	private void generateXMLForStatementList(ImmutableList<Statement> list, Element p) {
		Element stmtElement = doc.createElement("StatementList");
		p.appendChild(stmtElement);
		for(Statement stmt : list){
			stmt.accept(this, stmtElement);
		}
	}
	void generateXMLForStatement(Statement s, Element p, String label) {
		if(s != null){
			Element top = doc.createElement(label);
			p.appendChild(top);
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
		generateXMLForDeclTypeList(s.getTypeDecls(), top);
		generateXMLForDeclVarList(s.getVarDecls(), top);
		generateXMLForStatementList(s.getStatements(), top);
		return null;
	}
	@Override
	public Void visitStmtCall(StmtCall s, Element p) {
		Element top = doc.createElement("StmtCall");
		p.appendChild(top);
		Element proc = doc.createElement("Procedure");
		top.appendChild(proc);
		s.getProcedure().accept(this, proc);
		generateXMLForExpressionList(s.getArgs(), top, "ArgumentList");
		return null;
	}
	@Override
	public Void visitStmtConsume(StmtConsume s, Element p) {
		Element top = doc.createElement("StmtConsume");
		p.appendChild(top);
		top.setAttribute("numberOfTokens", Integer.toString(s.getNumberOfTokens()));
		generateXMLForPort(s.getPort(), top);
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
		generateXMLForGeneratorFilterList(s.getGenerators(), top);
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
		generateXMLForPort(s.getPort(), top);
		generateXMLForExpressionList(s.getValues(), top, "ValueList");
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
	public void generateXMLForEntityExprList(ImmutableList<Entry<String, EntityExpr>> entities, Element p) {
		Element top = doc.createElement("EntityExprList");
		p.appendChild(top);
		for(Entry<String, EntityExpr> e : entities){
			Element decl = doc.createElement("EntityDecl");
			top.appendChild(decl);
			decl.setAttribute("name", e.getKey());
			e.getValue().accept(this, decl);
		}
	}
	public Void visitEntityInstanceExpr(EntityInstanceExpr entity, Element p) {
		Element top = doc.createElement("EntityInstance");
		p.appendChild(top);
		top.setAttribute("name", entity.getEntityName());
		generateXMLForParameterAssignmentList(entity.getParameterAssignments(), top);
		generateXMLForToolAttributeList(entity.getToolAttributes(), top);
		return null;
	}
	public Void visitEntityIfExpr(EntityIfExpr e, Element p) {
		return null;
	}
	public Void visitEntityListExpr(EntityListExpr e, Element p) {
		return null;
	}
	public void generateXMLForParameterAssignmentList(ImmutableList<Entry<String, Expression>> parameterAssignments,
			Element p) {
		Element top = doc.createElement("ParameterAssignementList");
		p.appendChild(top);
		for(Entry<String, Expression> node : parameterAssignments){
			generateXMLForParameterAssignment(node, top);
		}
	}
	public void generateXMLForParameterAssignment(Entry<String, Expression> node, Element p) {
		Element top = doc.createElement("ParameterAssignment");
		p.appendChild(top);
		top.setAttribute("name", node.getKey());
		node.getValue().accept(this, top);
	}
/******************************************************************************
 * Structure Statement (Network)
 */
	public void generateXMLForStructureStmtList(ImmutableList<StructureStatement> structure, Element p) {
		Element top = doc.createElement("StructureStmtList");
		p.appendChild(top);
		for(StructureStatement s : structure){
			s.accept(this, p);
		}
	}
	public Void visitStructureConnectionStmt(StructureConnectionStmt stmt, Element p) {
		Element top = doc.createElement("StructureStmtConnection");
		p.appendChild(top);
		generateXMLForPortReference(stmt.getSrc(), "SrcPort", top);
		generateXMLForPortReference(stmt.getDst(), "DstPort", top);
		generateXMLForToolAttributeList(stmt.getToolAttributes(), top);
		return null;
	}
	private void generateXMLForPortReference(PortReference port, String label,
			Element p) {
		Element top = doc.createElement(label);
		p.appendChild(top);
		if(port.getEntityName() != null){
			top.setAttribute("EntityName", port.getEntityName());
		}
		top.setAttribute("portName", port.getPortName());
		generateXMLForExpressionList(port.getEntityIndex(), top, "EntityIndexList");
	}
	public Void visitStructureIfStmt(StructureIfStmt stmt, Element p) {
		Element top = doc.createElement("StructureIfStmt");
		p.appendChild(top);
		//--- condition
		Element cond = doc.createElement("Condition");
		top.appendChild(cond);
		stmt.getCondition().accept(this, cond);
		//--- true branch
		Element trueExpr = doc.createElement("TrueExpr");
		top.appendChild(trueExpr);
		generateXMLForStructureStmtList(stmt.getTrueStmt(), trueExpr);
		//--- else expr
		if(stmt.getFalseStmt() != null){
			Element elseExpr = doc.createElement("ElseExpr");
			top.appendChild(elseExpr);
			generateXMLForStructureStmtList(stmt.getFalseStmt(), elseExpr);
		}
		return null;
	}
	public Void visitStructureForeachStmt(StructureForeachStmt stmt, Element p) {
		Element top = doc.createElement("StructureStmtForeach");
		p.appendChild(top);
		//-- body
		Element bodyElement = doc.createElement("Body");
		top.appendChild(bodyElement);
		generateXMLForStructureStmtList(stmt.getStatements(), bodyElement);
		generateXMLForGeneratorFilterList(stmt.getGenerators(), top);
		return null;
	}
}
