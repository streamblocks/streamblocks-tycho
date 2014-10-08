package net.opendf.ir;

import java.util.Objects;

import net.opendf.ir.util.ImmutableEntry;
import net.opendf.ir.util.ImmutableList;

public class Annotation extends AbstractIRNode {
	private final String name;
	private final ImmutableList<ImmutableEntry<String, String>> parameters;

	public Annotation(String identifier, ImmutableList<ImmutableEntry<String, String>> parameters) {
		this(null, identifier, parameters);
	}

	private Annotation(Annotation original, String name, ImmutableList<ImmutableEntry<String, String>> parameters) {
		super(original);
		this.name = name;
		this.parameters = parameters;
	}

	public String getName() {
		return name;
	}

	public ImmutableList<ImmutableEntry<String, String>> getParameters() {
		return parameters;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Annotation) {
			Annotation that = (Annotation) obj;
			return this.name.equals(that.name) && this.parameters.equals(that.parameters);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, parameters);
	}
}
