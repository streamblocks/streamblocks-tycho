package se.lth.cs.tycho.meta.core;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.TypeExpr;

import java.util.Objects;
import java.util.function.Consumer;

public class MetaArgumentType extends MetaArgument {

	private final TypeExpr type;

	public MetaArgumentType(String name, TypeExpr type) {
		this(null, name, type);
	}

	public MetaArgumentType(IRNode original, String name, TypeExpr type) {
		super(original, name);
		this.type = type;
	}

	public MetaArgumentType copy(String name, TypeExpr type) {
		if (Objects.equals(getName(), name) && Objects.equals(getType(), type)) {
			return this;
		} else {
			return new MetaArgumentType(this, name, type);
		}
	}

	public TypeExpr getType() {
		return type;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getType());
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(getName(), transformation.applyChecked(TypeExpr.class, getType()));
	}

	@Override
	public String toString() {
		return getType().toString();
	}
}
