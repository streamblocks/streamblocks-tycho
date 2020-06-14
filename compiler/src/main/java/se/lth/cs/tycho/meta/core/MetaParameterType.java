package se.lth.cs.tycho.meta.core;

import se.lth.cs.tycho.ir.IRNode;

import java.util.function.Consumer;

public class MetaParameterType extends MetaParameter {

	public MetaParameterType(String name) {
		this(null, name);
	}

	public MetaParameterType(IRNode original, String name) {
		super(original, name);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {

	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return this;
	}
}
