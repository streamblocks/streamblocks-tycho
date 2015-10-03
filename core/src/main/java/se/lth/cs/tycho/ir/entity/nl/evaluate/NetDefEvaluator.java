package se.lth.cs.tycho.ir.entity.nl.evaluate;

import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.instance.net.Node.Identifier;
import se.lth.cs.tycho.interp.*;
import se.lth.cs.tycho.interp.exception.CALCompiletimeException;
import se.lth.cs.tycho.interp.preprocess.VariableOffsetTransformer;
import se.lth.cs.tycho.interp.values.ExprValue;
import se.lth.cs.tycho.interp.values.RefView;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortContainer;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.*;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.loader.AmbiguityException;
import se.lth.cs.tycho.loader.DeclarationLoader;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class NetDefEvaluator implements EntityExprVisitor<EntityExpr, Environment>, StructureStmtVisitor<StructureStatement, Environment>{
	NlNetwork srcNetwork;
	private final Interpreter interpreter;
	private final TypeConverter converter;
	private final GeneratorFilterHelper gen;
	private DeclarationLoader declLoader;
	private boolean evaluated;

	private HashMap<String, EntityExpr> entities;
	private ArrayList<StructureStatement> structure;
	private Memory mem;

	public NetDefEvaluator(NlNetwork network, Interpreter interpreter, DeclarationLoader entityLoader){
		this.srcNetwork = network;
		this.interpreter = interpreter;
		this.converter = TypeConverter.getInstance();
		this.gen = new GeneratorFilterHelper(interpreter);
		this.declLoader = entityLoader;
		this.evaluated = false;
	}


	public NlNetwork getNetworkDefinition(){
		assert evaluated;
		ImmutableList<TypeDecl> typePars = ImmutableList.empty();
		ImmutableList<VarDecl> valuePars = ImmutableList.empty();
		ImmutableList<TypeDecl> typeDecls = ImmutableList.empty();
		ImmutableList<VarDecl> varDecls = ImmutableList.empty();
		return srcNetwork.copy(
				typePars, valuePars, typeDecls, varDecls, 
				srcNetwork.getInputPorts(), srcNetwork.getOutputPorts(), 
				ImmutableList.from(entities.entrySet()), ImmutableList.from(structure), srcNetwork.getToolAttributes());
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
		throw new CALCompiletimeException(msg);
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

		public ImmutableList<Node> getNodes() {	return ImmutableList.from(nodes.values()); }
		public ImmutableList<Connection> getConnections(){ return connections.build(); }

		@Override
		public Void visitEntityInstanceExpr(EntityInstanceExpr e, String p) {
			Entity decl = null;
			String entityName = e.getEntityName() + ":" + p;
			// TODO if a parameter has the value of a lambda/procedure expression with free variables we need to store the environment to.
			//e.getParameterAssignments();
			PortContainer payload = null;
			try {
				decl = declLoader.loadEntity(QID.of(e.getEntityName()), null).getEntity();
			} catch (AmbiguityException e1) {
				throw new RuntimeException(e1);
			}
			if(decl instanceof CalActor){
				payload = BasicActorMachineSimulator.prepareActor((CalActor)decl);
			} else if(decl instanceof NlNetwork){
				payload = BasicNetworkSimulator.prepareNetworkDefinition((NlNetwork)decl, e.getParameterAssignments(), declLoader);
			} else {
				throw new UnsupportedOperationException("DeclLoader returned an unexpected type during network evaluation." + entityName + "is instance of class" + decl.getClass().getCanonicalName());
			}
			//TODO parameters for actors
			nodes.put(p, new Node(entityName, payload));
			return null;
		}

		@Override
		public Void visitEntityIfExpr(EntityIfExpr e, String p) {
			throw new RuntimeException("The NlNetwork is not evalated befor the Network is created.");
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
			throw new RuntimeException("The NlNetwork is not evalated befor the Network is created.");
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
		ImmutableList<VarDecl> declList = srcNetwork.getVarDecls();
		int scopeOffset=0;
		for(scopeOffset=0; scopeOffset<declList.size(); scopeOffset++){
			VarDecl decl = declList.get(scopeOffset);
			RefView value = interpreter.evaluate(decl.getValue(), env);
			value.assignTo(mem.declare(VariableOffsetTransformer.NetworkGlobalScopeId, scopeOffset));
		}
		// value parameters, compute values and store in paramScopeId memory
		ImmutableList<VarDecl> parList = srcNetwork.getValueParameters();
		for(Entry<String, Expression> assignment : parameterAssignments){
			String name = assignment.getKey();
			scopeOffset = -1;
			for(int i=0; i<parList.size(); i++){
				if(name.equals(parList.get(i).getName())){
					scopeOffset = i;
				}
			}
			if(scopeOffset<0) {
				throw new CALCompiletimeException("unknown parameter name: " + name);
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
			return e.copy(e.getEntityName(), builder.build());
		} catch(se.lth.cs.tycho.interp.exception.CALIndexOutOfBoundsException error){
			String msg = error.getMessage();
			throw new se.lth.cs.tycho.interp.exception.CALIndexOutOfBoundsException(msg);
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
		return new StructureForeachStmt(ImmutableList.<GeneratorFilter>empty(), builder.build());
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
			ImmutableList<VarDecl> vars = srcNetwork.getVarDecls();
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
