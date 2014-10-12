package se.lth.cs.tycho.instance.net;

import java.util.Objects;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

/**
 * A Connection links two ports. A port either belongs to the content of the 
 * {@link Node} or is an external port of the {@link Network}.
 * 
 * {@link Connection}s identifies the ports it connects to by linking to the 
 * {@link se.lth.cs.tycho.ir.IRNode.Identifier} of the encapsulating {@link Node} 
 * and a {@link se.lth.cs.tycho.ir.Port} object. 
 * 
 * For external ports, i.e. ports belonging to the {@link Network}, 
 * srcNodeId/dstNodeId is <code>null</code>.
 */

public class Connection extends AbstractIRNode {
	
	public Port getSrcPort() { return srcPort; }

	public Identifier getSrcNodeId() { return srcNodeId; }
	
	public Port getDstPort() { return dstPort; }
	
	public Identifier getDstNodeId(){ return dstNodeId; }
	//
	//  Ctor
	// 
	
	public Connection(Identifier srcNodeId, Port srcPort, Identifier dstNodeId, Port dstPort, ImmutableList<ToolAttribute> ta) {
		this(null, srcNodeId, srcPort, dstNodeId, dstPort, ta);
	}
	
	protected Connection(IRNode original, Identifier srcNodeId, Port srcPort, Identifier dstNodeId, Port dstPort, ImmutableList<ToolAttribute> ta) {
		super(original, ta);
		assert srcPort != null;
		assert dstPort != null;
		this.srcNodeId = srcNodeId;
		this.srcPort = srcPort;
		this.dstNodeId = dstNodeId;
		this.dstPort = dstPort;
	}
	
	public Connection copy(Identifier srcNodeId, Port srcPort, Identifier dstNodeId, Port dstPort, ImmutableList<ToolAttribute> ta){
		if(Objects.equals(this.srcNodeId, srcNodeId) && Objects.equals(this.srcPort, srcPort) && Objects.equals(this.dstNodeId,  dstNodeId) 
				&& Objects.equals(this.dstPort,  dstPort) && Lists.equals(getToolAttributes(), ta)){
			return this;
		}
		return new Connection(this, srcNodeId, srcPort, dstNodeId, dstPort, ta);
	}
	
	private Port srcPort, dstPort;
	private Identifier srcNodeId, dstNodeId;  // null for external ports in the network.
}
