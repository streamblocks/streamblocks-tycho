package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;

/**
 * This abstract class provides a base implementation for {@link Attributable}.
 */
public abstract class AttributableIRNode extends AbstractIRNode implements Attributable {
	private ImmutableList<ToolAttribute> attributes;

	/**
	 * Constructs a node with the attributes of original.
	 *
	 * @param original the node to take the attributes from
	 */
	public AttributableIRNode(AttributableIRNode original) {
		super(original);
		this.attributes = original == null ? ImmutableList.empty() : original.attributes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImmutableList<ToolAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttributableIRNode clone() {
		return (AttributableIRNode) super.clone();
	}
}
