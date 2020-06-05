package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Objects;
import java.util.function.Consumer;

public class FieldDecl extends VarDecl {

	public FieldDecl(TypeExpr type, String name) {
		this(null, type, name);
	}

	private FieldDecl(VarDecl original, TypeExpr type, String name) {
		super(original, ImmutableList.empty(), type, name, null, false, false);
	}

	@Override
	public FieldDecl withName(String name) {
		return copy(getType(), name);
	}

	private FieldDecl copy(TypeExpr type, String name) {
		if (getType() == type && Objects.equals(getName(), name)) {
			return this;
		} else {
			return new FieldDecl(this, type, name);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		if (getType() != null) action.accept(getType());
	}

	@Override
	public VarDecl transformChildren(Transformation transformation) {
		return copy(
				getType() == null ? null : transformation.applyChecked(TypeExpr.class, getType()),
				getName());
	}
}