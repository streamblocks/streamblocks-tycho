package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;

public abstract class AttributableIRNode extends AbstractIRNode implements Attributable {
	private ImmutableList<ToolAttribute> attributes;

	public AttributableIRNode(AttributableIRNode original) {
		super(original);
		this.attributes = original == null ? ImmutableList.empty() : original.attributes;
	}

	@Override
	public ImmutableList<ToolAttribute> getAttributes() {
		return attributes;
	}

	@Override
	public AttributableIRNode withAttributes(List<ToolAttribute> attributes) {
		if (Lists.sameElements(this.attributes, attributes)) {
			return this;
		} else {
			AttributableIRNode clone = clone();
			clone.attributes = ImmutableList.from(attributes);
			return clone;
		}
	}

	@Override
	public AttributableIRNode clone() {
		return (AttributableIRNode) super.clone();
	}
}
