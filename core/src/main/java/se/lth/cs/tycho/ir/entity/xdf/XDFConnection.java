package se.lth.cs.tycho.ir.entity.xdf;

import java.util.List;

import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.util.ImmutableList;

public class XDFConnection extends AbstractIRNode {
	private final String sourceInstance;
	private final Port sourcePort;
	private final String destinaitonInstance;
	private final Port destinationPort;
	
	public XDFConnection(String sourceInstance, Port sourcePort, String destinaitonInstance,
			Port destinationPort) {
		this(null, sourceInstance, sourcePort, destinaitonInstance, destinationPort, ImmutableList.empty());
	}
	
	public XDFConnection(IRNode original, String sourceInstance, Port sourcePort, String destinaitonInstance,
			Port destinationPort, List<ToolAttribute> attributes) {
		super(original, ImmutableList.copyOf(attributes));
		this.sourceInstance = sourceInstance;
		this.sourcePort = sourcePort;
		this.destinaitonInstance = destinaitonInstance;
		this.destinationPort = destinationPort;
	}

	public String getSourceInstance() {
		return sourceInstance;
	}

	public Port getSourcePort() {
		return sourcePort;
	}

	public String getDestinaitonInstance() {
		return destinaitonInstance;
	}

	public Port getDestinationPort() {
		return destinationPort;
	}
	

}
