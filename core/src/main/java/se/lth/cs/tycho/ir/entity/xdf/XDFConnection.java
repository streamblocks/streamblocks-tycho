package se.lth.cs.tycho.ir.entity.xdf;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.AttributableIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

public class XDFConnection extends AttributableIRNode {
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
		super(ImmutableList.copyOf(attributes));
		this.sourceInstance = sourceInstance;
		this.sourcePort = sourcePort;
		this.destinaitonInstance = destinaitonInstance;
		this.destinationPort = destinationPort;
	}

	public XDFConnection copy(String sourceInstance, Port sourcePort, String destinaitonInstance,
							  Port destinationPort, List<ToolAttribute> attributes) {
		if (Objects.equals(this.sourceInstance, sourceInstance) && this.sourcePort == sourcePort
				&& Objects.equals(this.destinaitonInstance, destinaitonInstance) && this.destinationPort == destinationPort
				&& Lists.elementIdentityEquals(getToolAttributes(), attributes)) {
			return this;
		} else {
			return new XDFConnection(this, sourceInstance, sourcePort, destinaitonInstance, destinationPort, attributes);
		}
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


	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(sourcePort);
		action.accept(destinationPort);
	}

	@Override
	public XDFConnection transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return copy(
				sourceInstance,
				(Port) transformation.apply(sourcePort),
				destinaitonInstance,
				(Port) transformation.apply(destinationPort),
				(ImmutableList) getToolAttributes().map(transformation)
		);
	}
}
