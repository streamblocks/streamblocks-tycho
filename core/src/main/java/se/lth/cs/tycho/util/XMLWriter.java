package se.lth.cs.tycho.util;

/**
 *  copyright (c) 2013, Per Andersson
 *  
 *  This code translates a cal IR to an XML document (org.w3c.dom.Document)
 *  The XML document is built top down. 
 *  The parameter to all visit methods is the parent XML node.
 **/

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.ICall;
import se.lth.cs.tycho.instance.am.ITest;
import se.lth.cs.tycho.instance.am.IWait;
import se.lth.cs.tycho.instance.am.Instruction;
import se.lth.cs.tycho.instance.am.InstructionVisitor;
import se.lth.cs.tycho.instance.am.PortCondition;
import se.lth.cs.tycho.instance.am.PredicateCondition;
import se.lth.cs.tycho.instance.am.Scope;
import se.lth.cs.tycho.instance.am.State;
import se.lth.cs.tycho.instance.am.Transition;
import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.interp.VariableLocation;
import se.lth.cs.tycho.ir.Field;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.entity.cal.ScheduleFSM;
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

public class XMLWriter implements ExpressionVisitor<Void,Element>, 
StatementVisitor<Void, Element>, 
EntityExprVisitor<Void, Element>, 
StructureStmtVisitor<Void, Element>, 
LValueVisitor<Void, Element>, 
InstructionVisitor<Void, Element>{
	Document doc;
	
	public Document getDocument(){ return doc; }

	public void print() {
		print(System.out);
	}
	
	public void print(OutputStream out){
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			
			transformer.transform(new DOMSource(doc), 
			         new StreamResult(new OutputStreamWriter(out, "UTF-8")));
		} catch (IOException | TransformerException e) {
			e.printStackTrace();
		}
	}
	public String toString(){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		print(out);
		return out.toString();
	}

	public XMLWriter(CalActor calActor){
		try{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();
			Element top = doc.createElement("wrapper");
			generateXMLForActor(calActor, top);
		}catch(ParserConfigurationException pce){
			pce.printStackTrace();
		}
	}
	
	public XMLWriter(Network net){
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();

			Element top = doc.createElement("wrapper");
			doc.appendChild(top);
			generateXMLForNetwork(net, top);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	private void generateXMLForNetwork(Network net, Element p) {
		Element top = doc.createElement("Network");
		p.appendChild(top);
		// ports
		generateXMLForPortDeclList(net.getInputPorts(), top, "InputPortList");
		generateXMLForPortDeclList(net.getOutputPorts(), top, "OutputPortList");
		//nodes
		generateXMLForNodeList(net.getNodes(), top);
		//connections
		generateXMLForConnectionList(net.getConnections(), top);
	}

	private void generateXMLForConnectionList(ImmutableList<Connection> connections, Element p) {
		Element top = doc.createElement("ConnectionList");
		p.appendChild(top);
		for(Connection c : connections){
			generateXMLForConnection(c, top);
		}
	}
	private void generateXMLForConnection(Connection c, Element p) {
		Element top = doc.createElement("Connection");
		p.appendChild(top);
		Element src = doc.createElement("Source");
		top.appendChild(src);
		if(c.getSrcNodeId() != null){
			src.setAttribute("nodeId", c.getSrcNodeId().toString());
		}
		generateXMLForPort(c.getSrcPort(), src);
		Element dst = doc.createElement("Destination");
		top.appendChild(dst);
		if(c.getDstNodeId() != null){
			dst.setAttribute("nodeId",  c.getDstNodeId().toString());
		}
		generateXMLForPort(c.getDstPort(), dst);
	}
	private void generateXMLForNodeList(ImmutableList<Node> nodes, Element p) {
		Element top = doc.createElement("NodeList");
		p.appendChild(top);
		for(Node node : nodes){
			GenerateXMLForNode(node, top);
		}
	}
	private void GenerateXMLForNode(Node node, Element p) {
		Element top = doc.createElement("Node");
		p.appendChild(top);
		top.setAttribute("name", node.getName());
		top.setAttribute("id", node.getIdentifier().toString());
		Object content = node.getContent();
		if(content instanceof CalActor){
			generateXMLForActor((CalActor)content, top);
		} else if(content instanceof ActorMachine){
			generateXMLForActorMachine((ActorMachine)content, top);
		} else if(content instanceof NlNetwork){
			generateXMLForNetworkDefinition((NlNetwork)content, top);
		} else if(content instanceof Network){
			generateXMLForNetwork((Network)content, top);
		}
	}
	
	public XMLWriter(NlNetwork network){
		try{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();
			
			Element wrapper = doc.createElement("wrapper");
			doc.appendChild(wrapper);
			generateXMLForNetworkDefinition(network, wrapper);
		}catch(ParserConfigurationException pce){
			pce.printStackTrace();
		}
	}
	private void generateXMLForNetworkDefinition(NlNetwork network, Element p) {
		Element networkElement = doc.createElement("NlNetwork");
		p.appendChild(networkElement);
		networkElement.setAttribute("name", null); //FIXME
		//-- type/value parameters, in/out ports, type/value declarations
		generateXMLForEntityDefinition(network, networkElement);
		generateXMLForDeclTypeList(network.getTypeDecls(), networkElement);
		generateXMLForDeclVarList(network.getVarDecls(), networkElement);
		generateXMLForEntityExprList(network.getEntities(), networkElement);
		generateXMLForStructureStmtList(network.getStructure(), networkElement);
		generateXMLForToolAttributeList(network.getToolAttributes(), networkElement);
	}

	public void generateXMLForToolAttributeList(ImmutableList<ToolAttribute> toolAttributes, Element p) {
		if(toolAttributes!=null && !toolAttributes.isEmpty()){
			Element top = doc.createElement("ToolAttributeList");
			p.appendChild(top);
			top.setAttribute("WARNING", "tool attributes is not supported by XML writer");
		}
	}
	
	private void generateXMLForActor(CalActor calActor, Element p) {
		Element actorElement = doc.createElement("CalActor");
		p.appendChild(actorElement);
		doc.appendChild(actorElement);
		actorElement.setAttribute("name", null); // FIXME
		//-- type/value parameters, in/out ports, type/value declarations
		generateXMLForEntityDefinition(calActor, actorElement);
		generateXMLForDeclTypeList(calActor.getTypeDecls(), actorElement);
		generateXMLForDeclVarList(calActor.getVarDecls(), actorElement);
		generateXMLForActions(calActor.getInitializers(), actorElement);
		generateXMLForActions(calActor.getActions(), actorElement);
		generateXMLForSchedule(calActor.getScheduleFSM(), actorElement);
		generateXMLForPriorityList(calActor.getPriorities(), actorElement);
		generateXMLForExpressionList(calActor.getInvariants(), actorElement, "InvariantList");		
	}
	private void generateXMLForSchedule(ScheduleFSM scheduleFSM, Element p) {
		if(scheduleFSM == null) return;
		Element top = doc.createElement("ScheduleFSM");
		p.appendChild(top);
		top.setAttribute("initialState", scheduleFSM.getInitialState());
		generateXMLForFSMTransitionList(scheduleFSM.getTransitions(), top);
	}
	private void generateXMLForFSMTransitionList(ImmutableList<se.lth.cs.tycho.ir.entity.cal.Transition> list, Element p) {
		Element top = doc.createElement("TransitionListFSM");
		p.appendChild(top);
		for(se.lth.cs.tycho.ir.entity.cal.Transition t : list){
			generateXMLForFSMTransition(t, top);
		}
	}
	private void generateXMLForFSMTransition(se.lth.cs.tycho.ir.entity.cal.Transition t, Element p) {
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
			//addSourceCodePosition(tag, qid);
			qid.setAttribute("name", tag.toString());
		}
	}
	//-- type/value parameters, in/out ports, type/value declarations
	public void generateXMLForEntityDefinition(Entity entity, Element top){
		//-- type parameters 
		//TODO
		//-- value parameters 
		generateXMLForVarDeclList(entity.getValueParameters(), top);
		//-- input ports 
		generateXMLForPortDeclList(entity.getInputPorts(), top, "InputPortList");
		//-- output ports 
		generateXMLForPortDeclList(entity.getOutputPorts(), top, "OutputPortList");
	}
	/******************************************************************************
	 * CalActor Machine
	 */
	public XMLWriter(ActorMachine am){
		try{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();

			Element wrapper = doc.createElement("wrapper");
			doc.appendChild(wrapper);
			generateXMLForActorMachine(am, wrapper);
		}catch(ParserConfigurationException pce){
			pce.printStackTrace();
		}
	}
	private void generateXMLForActorMachine(ActorMachine am, Element p) {
		Element amElement = doc.createElement("ActorMachine");
		p.appendChild(amElement);
		generateXMLForConditionList(am.getConditions(), amElement);
		generateXMLForAMController(am.getController(), amElement);
		generateXMLForPortDeclList(am.getInputPorts(), amElement, "InputPortList");
		generateXMLForPortDeclList(am.getOutputPorts(), amElement, "OutputPortList");
		generateXMLForAMScopeList(am.getScopes(), amElement);
		generateXMLForAMTransitionList(am.getTransitions(), amElement);
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
	public void generateXMLForDeclTypeList(List<TypeDecl> immutableList, Element parent){
		if(immutableList != null && !immutableList.isEmpty()){
			Element declTypeList = doc.createElement("DeclTypeList");
			for(TypeDecl typeDecl : immutableList){
				Element typeDeclElem = doc.createElement("DeclType");
				typeDeclElem.setAttribute("name", typeDecl.getName());
				declTypeList.appendChild(typeDeclElem);
			}
			parent.appendChild(declTypeList);
		}
	}
	public Element generateXMLForDeclVarList(List<VarDecl> varDecls, Element parent){
		return generateXMLForDeclVarList(varDecls, parent, "DeclVarList", false);
	}
	public Element generateXMLForDeclVarList(List<VarDecl> varDecls, Element parent, String topName, boolean printEmptyList){
		Element declVarList = null;
		if(varDecls != null && (printEmptyList || !varDecls.isEmpty())){
			declVarList = doc.createElement(topName);
			for(VarDecl v : varDecls){
				Element varDeclElem = doc.createElement("DeclVar");
				varDeclElem.setAttribute("name", v.getName());
				if(v.getValue() != null){
					Element initElem = doc.createElement("InitialValue");
					v.getValue().accept(this, initElem);
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
	private void generateXMLForVarDeclList(List<VarDecl> valueParameters, Element p) {
		if(valueParameters != null && !valueParameters.isEmpty()){
			Element top = doc.createElement("ValueParameterList");
			p.appendChild(top);
			for(VarDecl param : valueParameters){
				generateXMLForVarDecl(param, top);
			}
		}
	}
	private void generateXMLForVarDecl(VarDecl param, Element p) {
		Element top = doc.createElement("VarDecl");
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
		Element top = doc.createElement("ExprIf");
		p.appendChild(top);
		//--- condition
		Element cond = doc.createElement("Condition");
		top.appendChild(cond);
		e.getCondition().accept(this, cond);
		//--- true branch
		Element trueExpr = doc.createElement("TrueExpr");
		top.appendChild(trueExpr);
		e.getThenExpr().accept(this, trueExpr);
		//--- else expr
		Element elseExpr = doc.createElement("ElseExpr");
		top.appendChild(elseExpr);
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
		generateXMLForVarDeclList(e.getValueParameters(), top);
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
		Element litteralElement;
		if(e instanceof se.lth.cs.tycho.interp.values.ExprValue){
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
		generateXMLForVarDeclList(e.getValueParameters(), top);
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
		p.appendChild(op);
		op.setAttribute("operation", e.getOperation());
		e.getOperand().accept(this, op);
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
		return null;
	}
	public Void visitEntityIfExpr(EntityIfExpr e, Element p) {
		Element top = doc.createElement("EntityIfExpr");
		p.appendChild(top);
		//-- condition
		Element cond = doc.createElement("Condition");
		top.appendChild(cond);
		e.getCondition().accept(this, cond);
		//-- then branch
		Element thenElement = doc.createElement("TrueEntity");
		top.appendChild(thenElement);
		e.getTrueEntity().accept(this, thenElement);
		//-- else branch
		Element elseElement = doc.createElement("FalseEntity");
		top.appendChild(elseElement);
		e.getFalseEntity().accept(this, elseElement);
		return null;
	}
	public Void visitEntityListExpr(EntityListExpr e, Element p) {
		Element top = doc.createElement("EntityListExpr");
		p.appendChild(top);
		Element list = doc.createElement("EntityList");
		top.appendChild(list);
		for(EntityExpr elem : e.getEntityList()){
			elem.accept(this, list);
		}
		generateXMLForGeneratorFilterList(e.getGenerators(), top);
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
