package se.lth.cs.tycho.transform.util;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;

import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.decl.LocalTypeDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParDeclType;
import se.lth.cs.tycho.ir.decl.ParDeclValue;
import se.lth.cs.tycho.ir.entity.PortDecl;
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
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;

public class AbstractNetworkDefinitionTransformer<P> extends AbstractBasicTransformer<P> implements NetworkDefinitionTransformer<P>, 
                                                                                                    EntityExprVisitor<EntityExpr, P>,
                                                                                                    StructureStmtVisitor<StructureStatement, P>{

	public NetworkDefinition transformNetworkDefinition(NetworkDefinition net, P p){
		ImmutableList<ParDeclType> typePars = transformTypeParameters(net.getTypeParameters(), p);
		ImmutableList<ParDeclValue> valuePars = transformValueParameters(net.getValueParameters(), p);
		ImmutableList<LocalTypeDecl> typeDecls = transformTypeDecls(net.getTypeDecls(), p);
		ImmutableList<LocalVarDecl> varDecls = transformVarDecls(net.getVarDecls(), p);
		ImmutableList<PortDecl> inputPorts = transformInputPorts(net.getInputPorts(), p);
		ImmutableList<PortDecl> outputPorts = transformOutputPorts(net.getOutputPorts(), p);
		ImmutableList<Map.Entry<String,EntityExpr>> entities = transformEntitiyExprs(net.getEntities(), p);
		ImmutableList<StructureStatement> structure = transformStructureStmts(net.getStructure(), p);
		ImmutableList<ToolAttribute> toolAttributes = transformToolAttributes(net.getToolAttributes(), p);
		return net.copy(typePars, valuePars, typeDecls, varDecls,
				inputPorts, outputPorts, entities, structure, toolAttributes); 
	}
	
	@Override
	public PortReference transformPortReference(PortReference port, P p) {
		return port.copy(port.getEntityName(), transformExpressions(port.getEntityIndex(), p), port.getPortName());
	}

	@Override
	public PortDecl transformInputPort(PortDecl port, P p) {
		return port.copy(port.getName(), transformTypeExpr(port.getType(), p));
	}

	@Override
	public ImmutableList<PortDecl> transformInputPorts(ImmutableList<PortDecl> ports, P p) {
		ImmutableList.Builder<PortDecl> builder = new ImmutableList.Builder<PortDecl>();
		for(PortDecl port : ports){
			builder.add(transformInputPort(port, p));
		}
		return builder.build();
	}

	@Override
	public PortDecl transformOutputPort(PortDecl port, P param) {
		return port.copy(port.getName(), transformTypeExpr(port.getType(), param));
	}

	@Override
	public ImmutableList<PortDecl> transformOutputPorts(ImmutableList<PortDecl> ports, P p) {
		ImmutableList.Builder<PortDecl> builder = new ImmutableList.Builder<PortDecl>();
		for(PortDecl port : ports){
			builder.add(transformOutputPort(port, p));
		}
		return builder.build();
	}

	/*************************************************************************
	 * EntityExpressionVisitor
	 *************************************************************************/
	@Override
	public ImmutableList<Entry<String, EntityExpr>> transformEntitiyExprs(ImmutableList<Entry<String, EntityExpr>> entities, P p) {
		ImmutableList.Builder<Entry<String, EntityExpr>> builder = new ImmutableList.Builder<Entry<String, EntityExpr>>();
		for(Entry<String, EntityExpr> entry : entities){
			EntityExpr newExpr = transformEntityExpr(entry.getValue(), p);
			builder.add(new AbstractMap.SimpleEntry<String, EntityExpr>(entry.getKey(), newExpr));
		}
		return builder.build();
	}

	@Override
	public EntityExpr transformEntityExpr(EntityExpr expr, P p) {
		return expr.accept(this, p);
	}


	@Override
	public EntityExpr visitEntityInstanceExpr(EntityInstanceExpr e,	P p) {
		ImmutableList.Builder<Entry<String, Expression>> parAssignments = new ImmutableList.Builder<Entry<String, Expression>>();
		for(Entry<String, Expression> entry : e.getParameterAssignments()){
			parAssignments.add(new AbstractMap.SimpleEntry<String, Expression>(entry.getKey(), transformExpression(entry.getValue(), p)));
		}
		return e.copy(e.getEntityName(), parAssignments.build(), transformToolAttributes(e.getToolAttributes(), p));
	}

	@Override
	public EntityExpr visitEntityIfExpr(EntityIfExpr e, P p) {
		return e.copy(transformExpression(e.getCondition(), p), 
				      transformEntityExpr(e.getTrueEntity(), p),
				      transformEntityExpr(e.getFalseEntity(), p));
	}

	@Override
	public EntityExpr visitEntityListExpr(EntityListExpr e, P p) {
		ImmutableList.Builder<EntityExpr> builder = new ImmutableList.Builder<EntityExpr>();
		for(EntityExpr expr : e.getEntityList()){
			builder.add(expr.accept(this, p));
		}
		return e.copy(builder.build(), transformGenerators(e.getGenerators(), p));
	}

	/*************************************************************************
	 * StructureStatementVisitor
	 *************************************************************************/
	@Override
	public ImmutableList<StructureStatement> transformStructureStmts(ImmutableList<StructureStatement> structs, P p) {
		ImmutableList.Builder<StructureStatement> builder = new ImmutableList.Builder<StructureStatement>();
		for(StructureStatement s : structs){
			builder.add(transformStructureStmt(s, p));
		}
		return builder.build();
	}
	@Override
	public StructureStatement transformStructureStmt(StructureStatement stmt, P p) {
		return stmt.accept(this, p);
	}

	@Override
	public StructureStatement visitStructureConnectionStmt(StructureConnectionStmt stmt, P p) {
		return stmt.copy(transformPortReference(stmt.getSrc(), p), 
				         transformPortReference(stmt.getDst(), p), 
				         transformToolAttributes(stmt.getToolAttributes(), p));
	}

	@Override
	public StructureStatement visitStructureIfStmt(StructureIfStmt stmt, P p) {
		return stmt.copy(transformExpression(stmt.getCondition(), p), 
				         transformStructureStmts(stmt.getTrueStmt(), p),
				         transformStructureStmts(stmt.getFalseStmt(), p));
	}

	@Override
	public StructureStatement visitStructureForeachStmt(StructureForeachStmt stmt, P p) {
		return stmt.copy(transformGenerators(stmt.getGenerators() , p), 
				         transformStructureStmts(stmt.getStatements(), p));
	}

	/*************************************************************************
	 * ToolAttribute
	 *************************************************************************/
	@Override
	public ImmutableList<ToolAttribute> transformToolAttributes(ImmutableList<ToolAttribute> tas, P p) {
		ImmutableList.Builder<ToolAttribute> builder = new ImmutableList.Builder<ToolAttribute>();
		for(ToolAttribute ta : tas){
			builder.add(transformToolAttribute(ta, p));
		}
		return builder.build();
	}
	@Override
	public ToolAttribute transformToolAttribute(ToolAttribute ta, P p) {
		//TODO
		return ta;
	}
}
