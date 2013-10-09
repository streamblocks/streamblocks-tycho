package net.opendf.ir.net.ast.evaluate;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.opendf.interp.BasicActorMachineSimulator;
import net.opendf.interp.BasicEnvironment;
import net.opendf.interp.BasicMemory;
import net.opendf.interp.Channel;
import net.opendf.interp.Environment;
import net.opendf.interp.GeneratorFilterHelper;
import net.opendf.interp.Interpreter;
import net.opendf.interp.Memory;
import net.opendf.interp.TypeConverter;
import net.opendf.interp.exception.CALCompiletimeException;
import net.opendf.interp.values.ExprValue;
import net.opendf.interp.values.RefView;
import net.opendf.ir.IRNode.Identifier;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.DeclType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprLiteral;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.GeneratorFilter;
import net.opendf.ir.common.ParDeclType;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.Port;
import net.opendf.ir.common.PortContainer;
import net.opendf.ir.net.Connection;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;
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
import net.opendf.ir.util.DeclLoader;

public class NetDefEvaluator implements EntityExprVisitor<EntityExpr, Environment>, StructureStmtVisitor<StructureStatement, Environment>{
	NetworkDefinition srcNetwork;
	private final Interpreter interpreter;
	private final TypeConverter converter;
	private final GeneratorFilterHelper gen;
	private DeclLoader entityLoader;

	private HashMap<String, EntityExpr> entities;
	private ArrayList<StructureStatement> structure;
	private Memory mem;

	public NetDefEvaluator(NetworkDefinition network, Interpreter interpreter, DeclLoader entityLoader){
		this.srcNetwork = network;
		this.interpreter = interpreter;
		this.converter = TypeConverter.getInstance();
		this.gen = new GeneratorFilterHelper(interpreter);
		this.entityLoader = entityLoader;
	}


	public NetworkDefinition getNetworkDefinition(){
		ImmutableList<ParDeclType> typePars = ImmutableList.empty();
		ImmutableList<ParDeclValue> valuePars = ImmutableList.empty();
		ImmutableList<DeclType> typeDecls = ImmutableList.empty();
		ImmutableList<DeclVar> varDecls = ImmutableList.empty();
		return srcNetwork.copy(srcNetwork.getName(), srcNetwork.getNamespaceDecl(), 
				typePars, valuePars, typeDecls, varDecls, 
				srcNetwork.getInputPorts(), srcNetwork.getOutputPorts(), 
				ImmutableList.copyOf(entities.entrySet()), ImmutableList.copyOf(structure), srcNetwork.getToolAttributes());
	}

	public Network getNetwork(){
		NetworkNodeBuilder nb = new NetworkNodeBuilder();
		for(Entry<String, EntityExpr> entry : entities.entrySet()){
			entry.getValue().accept(nb, entry.getKey());
		}
		for(StructureStatement stmt : structure){
			stmt.accept(nb,  "");
		}
		return new Network(nb.getNodes(), nb.getConnections(), srcNetwork.getInputPorts(), srcNetwork.getOutputPorts());
	}

	private class NetworkNodeBuilder implements EntityExprVisitor<Void, String>, StructureStmtVisitor<Void, String>{
		HashMap<String, Node> nodes = new HashMap<String, Node>();
		ImmutableList.Builder<Connection> connections = new ImmutableList.Builder<Connection>();

		public ImmutableList<Node> getNodes() {	return ImmutableList.copyOf(nodes.values()); }
		public ImmutableList<Connection> getConnections(){ return connections.build(); }

		@Override
		public Void visitEntityInstanceExpr(EntityInstanceExpr e, String p) {
			Decl decl = null;
			try{
				String entityName = e.getEntityName() + ":" + p;
				// TODO if a parameter has the value of a lambda/procedure expression with free variables we need to store the environment to.
				//e.getParameterAssignments();
				PortContainer payload = null; 
				decl = entityLoader.getDecl(e.getEntityName());
				//TODO handle networks
				payload = BasicActorMachineSimulator.prepareActor((Actor)decl);
				nodes.put(p, new Node(entityName, payload, e.getToolAttributes()));
			} catch (ClassCastException exception){
				throw new CALCompiletimeException("An entity instance expression in a network must refer to an actor or network. "
			                                      + e.getEntityName() + " instantiates " + decl.getName() + " of type " + decl.getClass().getCanonicalName());
			}
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
		@Override
		public Void visitStructureConnectionStmt(StructureConnectionStmt stmt, String p) {
			PortReference src = stmt.getSrc();
			PortReference dst = stmt.getDst();
			Node srcNode = nodes.get(makeEntityName(src));
			Node dstNode = nodes.get(makeEntityName(dst));
			Identifier srcId = null;
			if(srcNode != null){
				srcId = srcNode.getIdentifier();
			} else {
				error("can not find entity " + makeEntityName(src));
			}
			Identifier dstId = null;
			if(dstNode != null){
				dstId = dstNode.getIdentifier();
			} else {
				error("can not find entity " + makeEntityName(dst));
			}
			Connection con =  new Connection(srcId, new Port(src.getPortName()), dstId, new Port(dst.getPortName()), stmt.getToolAttributes());
			connections.add(con);
			return null;
		}
		private void error(String msg) {
			System.err.println(msg);
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

		mem = new BasicMemory(srcNetwork.getVarDecls().size() + srcNetwork.getValueParameters().size());
		Environment env = new BasicEnvironment(new Channel.InputEnd[0], new Channel.OutputEnd[0], mem);    // no expressions will read from the ports while instantiating/flattening this network
		// declarations
		ImmutableList<DeclVar> declList = srcNetwork.getVarDecls();
		int scopeIndex=0;
		for(scopeIndex=0; scopeIndex<declList.size(); scopeIndex++){
			DeclVar decl = declList.get(scopeIndex);
			RefView value = interpreter.evaluate(decl.getInitialValue(), env);
			value.assignTo(mem.declare(0, scopeIndex));
		}
		// value parameters
		ImmutableList<ParDeclValue> parList = srcNetwork.getValueParameters();
		for(int parIndex=0; parIndex<parameterAssignments.size(); parIndex++){
			ParDeclValue par = parList.get(parIndex);
			Entry<String, Expression> assignment = parameterAssignments.get(parIndex);
			//TODO here we assume that parameters are declared in the same order as they are assigned
			assert assignment.getKey().equals(par.getName());
			RefView value = interpreter.evaluate(assignment.getValue(), env);			
			value.assignTo(mem.declare(0, scopeIndex++));
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
	}

	@Override
	public EntityExpr visitEntityInstanceExpr(EntityInstanceExpr e, Environment env) {
		ImmutableList.Builder<Map.Entry<String,Expression>> builder = new ImmutableList.Builder<Map.Entry<String,Expression>>();
		for(Entry<String, Expression> pa : e.getParameterAssignments()){
			Expression expr = pa.getValue();
			RefView value = interpreter.evaluate(expr, env);
			//TODO support for values that can not be represented as strings, i.e. ExprLambda
			builder.add(new AbstractMap.SimpleEntry<String, Expression>(pa.getKey(), new ExprValue(pa.getValue(), ExprLiteral.Kind.Integer, value.toString(), value)));
		}
		return e.copy(e.getEntityName(), builder.build(), e.getToolAttributes());
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
			ImmutableList<DeclVar> vars = srcNetwork.getVarDecls();
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
