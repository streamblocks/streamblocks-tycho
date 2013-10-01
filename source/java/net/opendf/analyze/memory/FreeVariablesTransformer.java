package net.opendf.analyze.memory;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.opendf.interp.exception.CALCompiletimeException;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.common.DeclType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprLambda;
import net.opendf.ir.common.ExprLet;
import net.opendf.ir.common.ExprProc;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.ParDeclType;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.common.Statement;
import net.opendf.ir.common.StmtBlock;
import net.opendf.ir.common.Variable;
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
import net.opendf.transform.util.AbstractActorTransformer;
import net.opendf.transform.util.NetworkDefinitionTransformer;

/**
 * This class computes the set of free variables for any expression.
 * The set of free variables are cached in the ExprLambda and ExprProc classes.
 * 
 * It also orders variable declarations to a valid initialization order.
 * Cyclic dependencies in variable initialization are detected.
 * This is done by transformVarDecls(ImmutableList<DeclVar> varDecls, Set<String> c).
 */

public class FreeVariablesTransformer extends AbstractActorTransformer<Set<String>> implements NetworkDefinitionTransformer<Set<String>>,
                                                                                               EntityExprVisitor<EntityExpr, Set<String>>,
                                                                                               StructureStmtVisitor<StructureStatement, Set<String>>{

	public static Actor transformActor(Actor actor){
		FreeVariablesTransformer t = new FreeVariablesTransformer();
		Set<String> c = new TreeSet<String>();  //sort the free variables in alphabetic order
		Actor a = t.transformActor(actor, c);
		return a;
	}

	public static NetworkDefinition transformNetworkDefinition(NetworkDefinition net){
		FreeVariablesTransformer t = new FreeVariablesTransformer();
		Set<String> c = new TreeSet<String>();  //sort the free variables in alphabetic order
		return t.transformNetworkDefinition(net, c);
	}

	public NetworkDefinition transformNetworkDefinition(NetworkDefinition net, Set<String> c){
		ImmutableList<ParDeclType> typePars = transformTypeParameters(net.getTypeParameters(), c);
		ImmutableList<ParDeclValue> valuePars = transformValueParameters(net.getValueParameters(), c);
		ImmutableList<DeclType> typeDecls = transformTypeDecls(net.getTypeDecls(), c);
		ImmutableList<DeclVar> varDecls = transformVarDecls(net.getVarDecls(), c);
		ImmutableList<PortDecl> inputPorts = transformInputPorts(net.getInputPorts(), c);
		ImmutableList<PortDecl> outputPorts = transformOutputPorts(net.getOutputPorts(), c);
		ImmutableList<Map.Entry<String,EntityExpr>> entities = transformEntities(net.getEntities(), c);
		ImmutableList<StructureStatement> structure = transformStructureStmts(net.getStructure(), c);
		ImmutableList<ToolAttribute> toolAttributes = transformToolAttributes(net.getToolAttributes(), c);
		return net.copy(net.getName(), net.getNamespaceDecl(), typePars, valuePars, typeDecls, varDecls,
				inputPorts, outputPorts, entities, structure, toolAttributes); 
	}

	@Override
	public ImmutableList<Entry<String, EntityExpr>> transformEntities(
			ImmutableList<Entry<String, EntityExpr>> entities, Set<String> c) {
		ImmutableList.Builder<Entry<String, EntityExpr>> builder = new ImmutableList.Builder<Entry<String, EntityExpr>>();
		for(Entry<String, EntityExpr> entry : entities){
			EntityExpr newExpr = transformEntityExpr(entry.getValue(), c);
			builder.add(new AbstractMap.SimpleEntry<String, EntityExpr>(entry.getKey(), newExpr));
		}
		return builder.build();
	}

	@Override
	public EntityExpr transformEntityExpr(EntityExpr expr, Set<String> c) {
		return expr.accept(this, c);
	}


	@Override
	public EntityExpr visitEntityInstanceExpr(EntityInstanceExpr e,	Set<String> c) {
		TreeMap<String, Expression> parAssignments = new TreeMap<String, Expression>();
		for(Entry<String, Expression> entry : e.getParameterAssignments()){
			parAssignments.put(entry.getKey(), transformExpression(entry.getValue(), c));
		}
		return e.copy(e.getEntityName(), ImmutableList.copyOf(parAssignments.entrySet()), 
				      transformToolAttributes(e.getToolAttributes(), c));
	}

	@Override
	public EntityExpr visitEntityIfExpr(EntityIfExpr e, Set<String> p) {
		return e.copy(transformExpression(e.getCondition(), p), 
				      e.getTrueEntity().accept(this, p),
				      e.getFalseEntity().accept(this, p));
	}

	@Override
	public EntityExpr visitEntityListExpr(EntityListExpr e, Set<String> p) {
		ImmutableList.Builder<EntityExpr> builder = new ImmutableList.Builder<EntityExpr>();
		for(EntityExpr expr : e.getEntityList()){
			builder.add(expr.accept(this, p));
		}
		return e.copy(builder.build(), transformGenerators(e.getGenerators(), p));
	}

	@Override
	public ImmutableList<StructureStatement> transformStructureStmts(ImmutableList<StructureStatement> structs, Set<String> p) {
		ImmutableList.Builder<StructureStatement> builder = new ImmutableList.Builder<StructureStatement>();
		for(StructureStatement s : structs){
			builder.add(s.accept(this, p));
		}
		return builder.build();
	}

	@Override
	public StructureStatement visitStructureConnectionStmt(StructureConnectionStmt stmt, Set<String> p) {
		return stmt.copy(transformPortReference(stmt.getSrc(), p), transformPortReference(stmt.getDst(), p), transformToolAttributes(stmt.getToolAttributes(), p));
	}

	@Override
	public PortReference transformPortReference(PortReference port, Set<String> p) {
		return port.copy(port.getEntityName(), transformExpressions(port.getEntityIndex(), p), port.getPortName());
	}

	@Override
	public StructureStatement visitStructureIfStmt(StructureIfStmt stmt, Set<String> p) {
		return stmt.copy(transformExpression(stmt.getCondition(), p), transformStructureStmts(stmt.getTrueStmt(), p),
				transformStructureStmts(stmt.getFalseStmt(), p));
	}

	@Override
	public StructureStatement visitStructureForeachStmt(StructureForeachStmt stmt, Set<String> p) {
		return stmt.copy(transformGenerators(stmt.getGenerators() , p), transformStructureStmts(stmt.getStatements(), p));
	}

	@Override
	public ImmutableList<ToolAttribute> transformToolAttributes(ImmutableList<ToolAttribute> ta, Set<String> p) {
		// TODO transform tool attributes
		return ta;
	}
	
	/**************************************************************************************************************
	 * computing the free variables
	 */

	@Override
	public Variable transformVariable(Variable var, Set<String> c) {
		c.add(var.getName());
		return var;
	}

	@Override
	public Expression visitExprLambda(ExprLambda lambda, Set<String> c) {
		try {
			Set<String> freeVars = c.getClass().newInstance();
			Expression body = lambda.getBody().accept(this, freeVars);
			for(ParDeclValue v :lambda.getValueParameters()){
				freeVars.remove(v.getName());
			}
			ImmutableList.Builder<Variable> builder = new ImmutableList.Builder<Variable>();
			for(String name : freeVars){
				builder.add(Variable.variable(name));
			}
			c.addAll(freeVars);
			return lambda.copy(lambda.getTypeParameters(), lambda.getValueParameters(), body, lambda.getReturnType(), 
					builder.build(), true);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Expression visitExprLet(ExprLet let, Set<String> c) {
		try {
			Set<String> freeVars = c.getClass().newInstance();
			ImmutableList<DeclVar> newDecls = transformVarDecls(let.getVarDecls(), freeVars);

			Expression body = let.getBody().accept(this, freeVars);
			// remove the locally declared names
			for(DeclVar v :let.getVarDecls()){
				freeVars.remove(v.getName());
			}
			c.addAll(freeVars);
			return let.copy(let.getTypeDecls(), newDecls, body);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Statement visitStmtBlock(StmtBlock block, Set<String> c) {
		try {
			Set<String> freeVars = c.getClass().newInstance();
			ImmutableList<DeclVar> newDecls = transformVarDecls(block.getVarDecls(), freeVars);

			ImmutableList.Builder<Statement> bodyBuilder = new ImmutableList.Builder<Statement>();
			for(Statement stmt : block.getStatements()){
				bodyBuilder.add(stmt.accept(this, freeVars));
			}
			c.addAll(freeVars);
			return block.copy(block.getTypeDecls(), newDecls, bodyBuilder.build());
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Expression visitExprProc(ExprProc proc, Set<String> c) {
		try {
			Set<String> freeVars = c.getClass().newInstance();
			Statement body = transformStatement(proc.getBody(), freeVars);
			for(ParDeclValue v : proc.getValueParameters()){
				freeVars.remove(v.getName());
			}
			ImmutableList.Builder<Variable> builder = new ImmutableList.Builder<Variable>();
			for(String name : freeVars){
				builder.add(Variable.variable(name));
			}
			c.addAll(freeVars);
			return proc.copy(proc.getTypeParameters(), proc.getValueParameters(), body, builder.build(), true);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public ImmutableList<DeclVar> transformVarDecls(ImmutableList<DeclVar> varDecls, Set<String> c){
		assert varDecls != null;
		Set<String> allFreeVars;
		try {
			ImmutableList.Builder<DeclVar> builder = new ImmutableList.Builder<>();
			allFreeVars = c.getClass().newInstance();
			int size = varDecls.size();
			HashMap<String, Set<String>> freeVarsMap = new HashMap<String, Set<String>>();
			DeclVar[] newDecls = new DeclVar[size];
			ScheduleStatus[] status = new ScheduleStatus[size];
			// compute the free variables for each declaration
			for(int i=0; i<size; i++){
				Set<String> freeVars = c.getClass().newInstance();
				newDecls[i] = transformVarDecl(varDecls.get(i), freeVars);
				status[i] = ScheduleStatus.nop;
				freeVarsMap.put(varDecls.get(i).getName(), freeVars);				
				allFreeVars.addAll(freeVars);
			}
			
			for(int i=0; i<newDecls.length; i++){
				scheduleDecls(i, newDecls, status, freeVarsMap, builder);
			}
			
			for(DeclVar v : varDecls){
				allFreeVars.remove(v.getName());
			}
			c.addAll(allFreeVars);
			return builder.build();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private enum ScheduleStatus{nop, Visited, Scheduled}

	public void scheduleDecls(int candidateIndex, DeclVar[] decls, ScheduleStatus[] status, Map<String, Set<String>> freeVarsMap, ImmutableList.Builder<DeclVar> builder) {
		switch(status[candidateIndex]){
		case Visited:
			// A variable is depending on itself. Find the variables involved in the dependency cycle
			StringBuffer sb = new StringBuffer();
			String sep = "";
			for(int i=0; i<status.length; i++){
				if(status[i]==ScheduleStatus.Visited){
					sb.append(sep);
					sb.append(decls[i].getName());
					sep = ", ";
				}
			}
			throw new CALCompiletimeException("Cyclic dependency when initializing variables. Dependent variables: " + sb);
		case Scheduled:
			return;
		case nop :
			DeclVar candidate = decls[candidateIndex];
			status[candidateIndex] = ScheduleStatus.Visited;
			for(String freeVar : freeVarsMap.get(candidate.getName())){
				for(int i=0; i<decls.length; i++){
					DeclVar decl = decls[i];
					if(status[i] != ScheduleStatus.Scheduled && decl.getName().equals(freeVar)){
						// the candidate is depending on an uninitialized variable. Initialize it first
						scheduleDecls(i, decls, status, freeVarsMap, builder);
					}
				}
			}
			status[candidateIndex] = ScheduleStatus.Scheduled;
			builder.add(candidate);
			break;
		}
	}

}
