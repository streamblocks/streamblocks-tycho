package se.lth.cs.tycho.ir.entity.nl.evaluate;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.interp.BasicActorMachineSimulator;
import se.lth.cs.tycho.interp.BasicEnvironment;
import se.lth.cs.tycho.interp.BasicMemory;
import se.lth.cs.tycho.interp.BasicNetworkSimulator;
import se.lth.cs.tycho.interp.Channel;
import se.lth.cs.tycho.interp.Environment;
import se.lth.cs.tycho.interp.GeneratorFilterHelper;
import se.lth.cs.tycho.interp.Interpreter;
import se.lth.cs.tycho.interp.Memory;
import se.lth.cs.tycho.interp.TypeConverter;
import se.lth.cs.tycho.interp.exception.CALCompiletimeException;
import se.lth.cs.tycho.interp.preprocess.VariableOffsetTransformer;
import se.lth.cs.tycho.interp.values.ExprValue;
import se.lth.cs.tycho.interp.values.RefView;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.IRNode.Identifier;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.LocalTypeDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParDeclType;
import se.lth.cs.tycho.ir.decl.ParDeclValue;
import se.lth.cs.tycho.ir.entity.PortContainer;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.Actor;
import se.lth.cs.tycho.ir.entity.nl.EntityExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityExprVisitor;
import se.lth.cs.tycho.ir.entity.nl.EntityIfExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityListExpr;
import se.lth.cs.tycho.ir.entity.nl.NetworkDefinition;
import se.lth.cs.tycho.ir.entity.nl.PortReference;
import se.lth.cs.tycho.ir.entity.nl.StructureConnectionStmt;
import se.lth.cs.tycho.ir.entity.nl.StructureForeachStmt;
import se.lth.cs.tycho.ir.entity.nl.StructureIfStmt;
import se.lth.cs.tycho.ir.entity.nl.StructureStatement;
import se.lth.cs.tycho.ir.entity.nl.StructureStmtVisitor;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.DeclLoader;
import se.lth.cs.tycho.ir.util.ImmutableList;

public class NetDefEvaluator implements EntityExprVisitor<EntityExpr, Environment>, StructureStmtVisitor<StructureStatement, Environment>{
	NetworkDefinition srcNetwork;
	private final Interpreter interpreter;
	private final TypeConverter converter;
	private final GeneratorFilterHelper gen;
	private DeclLoader declLoader;
	private boolean evaluated;

	private HashMap<String, EntityExpr> entities;
	private ArrayList<StructureStatement> structure;
	private Memory mem;

	public NetDefEvaluator(NetworkDefinition network, Interpreter interpreter, DeclLoader entityLoader){
		this.srcNetwork = network;
		this.interpreter = interpreter;
		this.converter = TypeConverter.getInstance();
		this.gen = new GeneratorFilterHelper(interpreter);
		this.declLoader = entityLoader;
		this.evaluated = false;
	}


	public NetworkDefinition getNetworkDefinition(){
		assert evaluated;
		ImmutableList<ParDeclType> typePars = ImmutableList.empty();
		ImmutableList<ParDeclValue> valuePars = ImmutableList.empty();
		ImmutableList<LocalTypeDecl> typeDecls = ImmutableList.empty();
		ImmutableList<LocalVarDecl> varDecls = ImmutableList.empty();
		return srcNetwork.copy(
				typePars, valuePars, typeDecls, varDecls, 
				srcNetwork.getInputPorts(), srcNetwork.getOutputPorts(), 
				ImmutableList.copyOf(entities.entrySet()), ImmutableList.copyOf(structure), srcNetwork.getToolAttributes());
	}

	public Network getNetwork(){
		assert evaluated;
		NetworkNodeBuilder nb = new NetworkNodeBuilder();
		for(Entry<String, EntityExpr> entry : entities.entrySet()){
			entry.getValue().accept(nb, entry.getKey());
		}
		for(StructureStatement stmt : structure){
			stmt.accept(nb,  "");
		}
		return new Network(nb.getNodes(), nb.getConnections(), srcNetwork.getInputPorts(), srcNetwork.getOutputPorts());
	}

	private void error(String msg) {
		throw new CALCompiletimeException(msg, null);
	}

	private int findPortIndex(String portName, ImmutableList<PortDecl> portList) {
		for(int i=0; i<portList.size(); i++){
			if(portList.get(i).getName().equals(portName)){
				return i;
			}
		}
		return -1;
	}

	private class NetworkNodeBuilder implements EntityExprVisitor<Void, String>, StructureStmtVisitor<Void, String>{
		HashMap<String, Node> nodes = new HashMap<String, Node>();
		ImmutableList.Builder<Connection> connections = new ImmutableList.Builder<Connection>();

		public ImmutableList<Node> getNodes() {	return ImmutableList.copyOf(nodes.values()); }
		public ImmutableList<Connection> getConnections(){ return connections.build(); }

		@Override
		public Void visitEntityInstanceExpr(EntityInstanceExpr e, String p) {
			Decl decl = null;
			String entityName = e.getEntityName() + ":" + p;
			// TODO if a parameter has the value of a lambda/procedure expression with free variables we need to store the environment to.
			//e.getParameterAssignments();
			PortContainer payload = null; 
			decl = declLoader.getDecl(e.getEntityName());
			if(decl instanceof Actor){
				payload = BasicActorMachineSimulator.prepareActor((Actor)decl, declLoader);
			} else if(decl instanceof NetworkDefinition){
				payload = BasicNetworkSimulator.prepareNetworkDefinition((NetworkDefinition)decl, e.getParameterAssignments(), declLoader);
			} else {
				throw new UnsupportedOperationException("DeclLoader returned an unexpected type during network evaluation." + entityName + "is instance of class" + decl.getClass().getCanonicalName());
			}
			//TODO parameters for actors
			nodes.put(p, new Node(entityName, payload, e.getToolAttributes()));
			return null;
		}

		@Override
		public Void visitEntityIfExpr(EntityIfExpr e, String p) {
			throw new RuntimeException("The NetworkDefinition is not evalated befor the Network is created.");
		}

		@Override
		public Void visitEntityListExpr(EntityListExpr e, String p) {
			assert e.getGenerators().isEmpty();
			ImmutableList<EntityExpr> list = e.getEntityList();
			for(int i=0; i<list.size(); i++){
				list.get(i).accept(this, p + "[" + i + "]");
			}
			return null;
		}
		private String makeEntityName(PortReference port){
			StringBuffer name = new StringBuffer(port.getEntityName());
			for(Expression index : port.getEntityIndex()){
				name.append("[");
				name.append(((ExprLiteral)index).getText());
				name.append("]");
			}
			return name.toString();
		}

		/**
		 * Validates entity and port names
		 */
		@Override
		public Void visitStructureConnectionStmt(StructureConnectionStmt stmt, String p) {
			PortReference src = stmt.getSrc();
			PortReference dst = stmt.getDst();
			Identifier srcId = null;
			Identifier dstId = null;
			int srcPortIndex = -1;
			int dstPortIndex = -1;
			ImmutableList<PortDecl> srcPortList, dstPortList;
			if(src.getEntityName() != null){
				// internal node
				Node srcNode = nodes.get(makeEntityName(src));
				if(srcNode == null){
					error("can not find entity " + makeEntityName(src));
				}
				srcId = srcNode.getIdentifier();
				srcPortList = srcNode.getContent().getOutputPorts();
			} else {
				// external port, nodeId == null
				srcPortList = srcNetwork.getInputPorts();
			}

			if(dst.getEntityName() != null){
				Node dstNode = nodes.get(makeEntityName(dst));
				if(dstNode == null){
					error("can not find entity " + makeEntityName(dst));
				}
				dstId = dstNode.getIdentifier();
				dstPortList = dstNode.getContent().getInputPorts();
			} else {
				// external port, nodeId == null
				dstPortList = srcNetwork.getOutputPorts();
			}
			// verify that the ports exists
			srcPortIndex = findPortIndex(src.getPortName(), srcPortList);
			if(srcPortIndex<0){ error("can not find port " + src.getPortName()); }
			dstPortIndex = findPortIndex(dst.getPortName(), dstPortList);
			if(dstPortIndex<0){ error("can not find port " + dst.getPortName()); }
			Connection con =  new Connection(srcId, new Port(src.getPortName(), srcPortIndex), dstId, new Port(dst.getPortName(), dstPortIndex), stmt.getToolAttributes());
			connections.add(con);
			return null;
		}
		@Override
		public Void visitStructureIfStmt(StructureIfStmt stmt, String p) {
			throw new RuntimeException("The NetworkDefinition is not evalated befor the Network is created.");
		}
		@Override
		public Void visitStructureForeachStmt(StructureForeachStmt stmt, String p) {
			assert stmt.getGenerators().isEmpty();
			for(StructureStatement s : stmt.getStatements()){
				s.accept(this, "");
			}
			return null;
		}
	}

	public void evaluate(ImmutableList<Map.Entry<String,Expression>> parameterAssignments){
		entities = new HashMap<String, EntityExpr>();
		structure = new ArrayList<StructureStatement>();

		int[] config = new int[Math.max(VariableOffsetTransformer.NetworkGlobalScopeId, VariableOffsetTransformer.NetworkParamScopeId)+1];
		config[VariableOffsetTransformer.NetworkGlobalScopeId] = srcNetwork.getVarDecls().size();
		config[VariableOffsetTransformer.NetworkParamScopeId] = srcNetwork.getValueParameters().size();
		mem = new BasicMemory(config);
		Environment env = new BasicEnvironment(new Channel.InputEnd[0], new Channel.OutputEnd[0], mem);    // no expressions will read from the ports while instantiating/flattening this network
		// declarations, compute initial values
		ImmutableList<LocalVarDecl> declList = srcNetwork.getVarDecls();
		int scopeOffset=0;
		for(scopeOffset=0; scopeOffset<declList.size(); scopeOffset++){
			LocalVarDecl decl = declList.get(scopeOffset);
			RefView value = interpreter.evaluate(decl.getInitialValue(), env);
			value.assignTo(mem.declare(VariableOffsetTransformer.NetworkGlobalScopeId, scopeOffset));
		}
		// value parameters, compute values and store in paramScopeId memory
		ImmutableList<ParDeclValue> parList = srcNetwork.getValueParameters();
		for(Entry<String, Expression> assignment : parameterAssignments){
			String name = assignment.getKey();
			scopeOffset = -1;
			for(int i=0; i<parList.size(); i++){
				if(name.equals(parList.get(i).getName())){
					scopeOffset = i;
				}
			}
			if(scopeOffset<0) {
				throw new CALCompiletimeException("unknown parameter name: " + name, null);
			}
			RefView value = interpreter.evaluate(assignment.getValue(), env);
			value.assignTo(mem.declare(VariableOffsetTransformer.NetworkParamScopeId, scopeOffset));
		}
		// EntityExpr
		for(Entry<String, EntityExpr> decl : srcNetwork.getEntities()){
			EntityExpr unrolled = decl.getValue().accept(this, env);
			entities.put(decl.getKey(), unrolled);
		}
		// structure statements
		for(StructureStatement s : srcNetwork.getStructure()){
			structure.add(s.accept(this, env));
		}
		evaluated = true;
	}

	@Override
	public EntityExpr visitEntityInstanceExpr(EntityInstanceExpr e, Environment env) {
		try{
			ImmutableList.Builder<Map.Entry<String,Expression>> builder = new ImmutableList.Builder<Map.Entry<String,Expression>>();
			for(Entry<String, Expression> pa : e.getParameterAssignments()){
				Expression expr = pa.getValue();
				RefView value = interpreter.evaluate(expr, env);
				//TODO support for values that can not be represented as strings, i.e. ExprLambda
				builder.add(new AbstractMap.SimpleEntry<String, Expression>(pa.getKey(), new ExprValue(pa.getValue(), ExprLiteral.Kind.Integer, value.toString(), value)));
			}
			return e.copy(e.getEntityName(), builder.build(), e.getToolAttributes());
		} catch(se.lth.cs.tycho.interp.exception.CALIndexOutOfBoundsException error){
			String msg = error.getMessage();
			throw new se.lth.cs.tycho.interp.exception.CALIndexOutOfBoundsException(msg + " at " + declLoader.getSrcLocations(e.getIdentifier()));
		}
	}

	@Override
	public EntityExpr visitEntityIfExpr(EntityIfExpr e, Environment env) {
		RefView condRef = interpreter.evaluate(e.getCondition(), env);
		boolean cond = converter.getBoolean(condRef);
		if (cond) {
			return e.getTrueEntity().accept(this, env);
		} else {
			return e.getFalseEntity().accept(this, env);
		}
	}

	@Override
	public EntityExpr visitEntityListExpr(final EntityListExpr e, final Environment env) {
		final ImmutableList.Builder<EntityExpr> builder = new ImmutableList.Builder<EntityExpr>();
		final NetDefEvaluator exprEvaluateor = this;

		Runnable execStmt = new Runnable() {
			public void run() {
				for(EntityExpr element : e.getEntityList()){
					builder.add(element.accept(exprEvaluateor, env));
				}
			}
		};
		gen.interpret(e.getGenerators(), execStmt, env);

		return e.copy(builder.build(), ImmutableList.<GeneratorFilter>empty());
	}

	/******************************************************************************
	 * Statements
	 */
	@Override
	public StructureStatement visitStructureConnectionStmt(StructureConnectionStmt stmt, Environment env) {
		PortReference src = stmt.getSrc();
		ImmutableList.Builder<Expression> srcBuilder = new ImmutableList.Builder<Expression>();
		for(Expression expr : src.getEntityIndex()){
			RefView value = interpreter.evaluate(expr, env);
			srcBuilder.add(new ExprValue(expr, ExprLiteral.Kind.Integer, value.toString(), value));
		}
		PortReference newSrc = src.copy(src.getEntityName(), srcBuilder.build(), src.getPortName());
		// dst
		PortReference dst = stmt.getDst();
		ImmutableList.Builder<Expression> dstBuilder = new ImmutableList.Builder<Expression>();
		for(Expression expr : dst.getEntityIndex()){
			RefView value = interpreter.evaluate(expr, env);
			dstBuilder.add(new ExprValue(expr, ExprLiteral.Kind.Integer, value.toString(), value));
		}
		PortReference newDst = dst.copy(dst.getEntityName(), dstBuilder.build(), dst.getPortName());
		return stmt.copy(newSrc, newDst, stmt.getToolAttributes());
	}

	@Override
	public StructureStatement visitStructureIfStmt(StructureIfStmt stmt, Environment env) {
		ImmutableList.Builder<StructureStatement> builder = new ImmutableList.Builder<StructureStatement>();
		RefView condRef = interpreter.evaluate(stmt.getCondition(), env);
		boolean cond = converter.getBoolean(condRef);
		if (cond) {
			for(StructureStatement s : stmt.getTrueStmt()){
				builder.add(s.accept(this,env));
			}
		} else {
			for(StructureStatement s : stmt.getFalseStmt()){
				builder.add(s.accept(this,env));
			}
		}
		return new StructureForeachStmt(stmt, ImmutableList.<GeneratorFilter>empty(), builder.build());
	}

	@Override
	public StructureStatement visitStructureForeachStmt(final StructureForeachStmt stmt, final Environment env) {
		final ImmutableList.Builder<StructureStatement> builder = new ImmutableList.Builder<StructureStatement>();
		final NetDefEvaluator stmtEvaluateor = this;

		Runnable execStmt = new Runnable() {
			public void run() {
				for(StructureStatement element : stmt.getStatements()){
					builder.add(element.accept(stmtEvaluateor, env));
				}
			}
		};
		gen.interpret(stmt.getGenerators(), execStmt, env);

		return stmt.copy(ImmutableList.<GeneratorFilter>empty(), builder.build());
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		if(srcNetwork != null){
			sb.append("vars\n");
			ImmutableList<LocalVarDecl> vars = srcNetwork.getVarDecls();
			for(int i = 0; i<vars.size(); i++){
				sb.append("  ");
				sb.append(vars.get(i).getName());
				sb.append(" = ");
				if(mem != null) {
					sb.append(mem.declare(0, i));
				}
				sb.append("\n");
			}
			sb.append("entities\n");
			if(entities != null){
				for(Entry<String, EntityExpr> e : entities.entrySet()){
					sb.append("  ");
					sb.append(e.getKey());
					sb.append(" = ");
					sb.append(e.getValue());
					sb.append("\n");
				}
			}
		}
		return sb.toString();
	}

}
