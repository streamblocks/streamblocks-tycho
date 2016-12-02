package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.type.TypeExpr;

import java.util.Objects;

public final class TypeParameter extends AbstractIRNode implements Parameter<TypeExpr, TypeParameter> {
	private final String name;
	private final TypeExpr value;

	private TypeParameter(IRNode original, String name, TypeExpr value) {
		super(original);
		this.name = name;
		this.value = value;
	}

	public TypeParameter(String name, TypeExpr value) {
		this(null, name, value);
	}

	@Override
	public TypeParameter copy(String name, TypeExpr value) {
		if (Objects.equals(this.name, name) && this.value == value) {
			return this;
		} else {
			return new TypeParameter(this, name, value);
		}
	}

	@Override
	public TypeParameter clone() {
		return (TypeParameter) super.clone();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public TypeExpr getValue() {
		return value;
	}
}
