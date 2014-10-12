package se.lth.cs.tycho.transform.util;

import java.util.Map;

import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.nl.EntityExpr;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.entity.nl.PortReference;
import se.lth.cs.tycho.ir.entity.nl.StructureStatement;
import se.lth.cs.tycho.ir.util.ImmutableList;

public interface NetworkDefinitionTransformer<P> extends BasicTransformer<P> {

	public NlNetwork transformNetworkDefinition(NlNetwork net, P p);

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
