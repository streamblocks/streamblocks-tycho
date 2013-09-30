package net.opendf.ir.net.ast.evaluate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.opendf.interp.BasicEnvironment;
import net.opendf.interp.BasicMemory;
import net.opendf.interp.Channel;
import net.opendf.interp.Environment;
import net.opendf.interp.Interpreter;
import net.opendf.interp.Memory;
import net.opendf.interp.TypeConverter;
import net.opendf.interp.values.RefView;
import net.opendf.ir.IRNode;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprLiteral;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.GeneratorFilter;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.ExprLiteral.Kind;
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

public class NetDefEvaluator implements EntityExprVisitor<EntityExpr, Environment>, StructureStmtVisitor<StructureStatement, Environment>{
	NetworkDefinition srcNetwork;
	private final Interpreter interpreter;
	private final TypeConverter converter;

/*
 	private boolean	isImport;
	private String	name;
	private NamespaceDecl	namespaceDecl;
	private String[]	qid 	private ImmutableList.Builder<PortDecl>	inputPorts;
	private ImmutableList.Builder<PortDecl>	outputPorts;
	private ImmutableList.Builder<DeclType>	typeDecls;
	private ImmutableList.Builder<ParDeclType>	typePars;
	private ImmutableList.Builder<ParDeclValue>	valuePars;
	private ImmutableList.Builder<DeclVar>	varDecls;
*/
	private HashMap<String, EntityExpr> entities;
//TODO	private ImmutableList.Builder<ToolAttribute> toolAttributes;
	private ArrayList<StructureStatement> structure;
	
	
	NetDefEvaluator(NetworkDefinition network, Interpreter interpreter){
		assert network.getToolAttributes().isEmpty();  // not supported
		this.srcNetwork = network;
		this.interpreter = interpreter;
		this.converter = TypeConverter.getInstance();
	}

	public void evaluate(ImmutableList<Map.Entry<String,Expression>> parameterAssignments){
		//TODO order variable initializations
		//TODO set scopeId and offset for all ExprVariable objects
		Memory mem = new BasicMemory(srcNetwork.getVarDecls().size() + srcNetwork.getValueParameters().size());
		Environment env = new BasicEnvironment(new Channel.InputEnd[0], new Channel.OutputEnd[0], mem);    // no expressions will read from the ports while instantiating/flattening this network
		ImmutableList<DeclVar> declList = srcNetwork.getVarDecls();
		int scopeIndex=0;
		for(scopeIndex=0; scopeIndex<declList.size(); scopeIndex++){
			DeclVar decl = declList.get(scopeIndex);
			RefView value = interpreter.evaluate(decl.getInitialValue(), env);
			value.assignTo(mem.declare(0, scopeIndex));
		}
		ImmutableList<ParDeclValue> parList = srcNetwork.getValueParameters();
		for(int parIndex=0; parIndex<parList.size(); parIndex++){
			ParDeclValue par = parList.get(parIndex);
			Entry<String, Expression> assignment = parameterAssignments.get(parIndex);
			//TODO here we assume that parameters are declared in the same order as they are assigned
			assert assignment.getKey().equals(par.getName());
			RefView value = interpreter.evaluate(assignment.getValue(), env);			
			value.assignTo(mem.declare(0, scopeIndex++));
		}
		// EntityExpr
		for(Entry<String, EntityExpr> decl : srcNetwork.getEntities()){
			EntityExpr unrolled = decl.getValue().accept(this, null);
			entities.put(decl.getKey(), unrolled);
		}
	}

	@Override
	public EntityExpr visitEntityInstanceExpr(EntityInstanceExpr e, Environment env) {
		// TODO evaluate parameters
		return e;
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
	public EntityExpr visitEntityListExpr(EntityListExpr e, Environment env) {
		//TODO generators
		assert e.getGenerators().isEmpty();
		
		ImmutableList.Builder<EntityExpr> builder = new ImmutableList.Builder<>();
		for(EntityExpr element : e.getEntityList()){
			builder.add(element.accept(this, env));
		}
		return e.copy(builder.build(), ImmutableList.<GeneratorFilter>empty());
	}

	/******************************************************************************
	 * Statements
	 */
	@Override
	public StructureStatement visitStructureConnectionStmt(StructureConnectionStmt stmt, Environment env) {
		//TODO tool attributes
		assert stmt.getToolAttributes().isEmpty();
		
		PortReference src = stmt.getSrc();
		ImmutableList.Builder<Expression> srcBuilder = new ImmutableList.Builder<Expression>();
		for(Expression expr : src.getEntityIndex()){
			RefView value = interpreter.evaluate(expr, env);
			srcBuilder.add(new ExprLiteral(expr, ExprLiteral.Kind.Integer, value.toString()));
		}
		PortReference newSrc = src.copy(src.getEntityName(), srcBuilder.build(), src.getPortName());
		// dst
		PortReference dst = stmt.getDst();
		ImmutableList.Builder<Expression> dstBuilder = new ImmutableList.Builder<Expression>();
		for(Expression expr : dst.getEntityIndex()){
			RefView value = interpreter.evaluate(expr, env);
			dstBuilder.add(new ExprLiteral(expr, ExprLiteral.Kind.Integer, value.toString()));
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
	public StructureStatement visitStructureForeachStmt(
			StructureForeachStmt stmt, Environment p) {
		// TODO Auto-generated method stub
		return null;
	}

}
