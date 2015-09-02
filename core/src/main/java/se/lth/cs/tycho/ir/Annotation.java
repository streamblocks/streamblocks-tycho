package se.lth.cs.tycho.ir;

import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;

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

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {

	}
}
