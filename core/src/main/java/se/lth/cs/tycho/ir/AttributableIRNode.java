package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AttributableIRNode implements IRNode, Attributable {
	private final ImmutableList<ToolAttribute> attributes;

	public AttributableIRNode(ImmutableList<ToolAttribute> attributes) {
		this.attributes = attributes == null ? ImmutableList.<ToolAttribute>empty() : attributes;
	}

	@Override
	public ToolAttribute getToolAttribute(String name) {
		return attributes.stream()
				.filter(attribute -> attribute.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	@Override
	public ImmutableList<ToolAttribute> getToolAttributes() {
		return attributes;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		throw new UnsupportedOperationException(); // TODO implement
	}

	@Override
	public AttributableIRNode transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		//return this;
		throw new UnsupportedOperationException("Transformation not implemented for " + getClass().getCanonicalName());
	}

	@Override
	public IRNode clone() {
		try {
			return (IRNode) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error(e);
		}
	}
}
