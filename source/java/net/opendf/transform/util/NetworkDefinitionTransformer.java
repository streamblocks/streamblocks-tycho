package net.opendf.transform.util;

import java.util.Map;

import net.opendf.ir.common.PortDecl;
import net.opendf.ir.net.ast.NetworkDefinition;
import net.opendf.ir.net.ast.EntityExpr;
import net.opendf.ir.net.ast.PortReference;
import net.opendf.ir.net.ast.StructureStatement;
import net.opendf.ir.net.ToolAttribute;
import net.opendf.ir.util.ImmutableList;

public interface NetworkDefinitionTransformer<P> extends BasicTransformer<P> {

	public NetworkDefinition transformNetworkDefinition(NetworkDefinition net, P p);

	public ImmutableList<PortDecl> transformInputPorts(ImmutableList<PortDecl> port, P param);
	public PortDecl transformInputPort(PortDecl port, P param);

	public ImmutableList<PortDecl> transformOutputPorts(ImmutableList<PortDecl> port, P param);
	public PortDecl transformOutputPort(PortDecl port, P param);

	public PortReference transformPortReference(PortReference port, P p);
	
	public ImmutableList<Map.Entry<String,EntityExpr>> transformEntitiyExprs(ImmutableList<Map.Entry<String, EntityExpr>> entities, P p);
	public EntityExpr transformEntityExpr(EntityExpr expr, P p);
	
	public ImmutableList<StructureStatement> transformStructureStmts(ImmutableList<StructureStatement> structs, P p);
    public StructureStatement transformStructureStmt(StructureStatement stmt, P p);

	public ImmutableList<ToolAttribute> transformToolAttributes(ImmutableList<ToolAttribute> tas, P p);
	public ToolAttribute transformToolAttribute(ToolAttribute ta, P p);

}
