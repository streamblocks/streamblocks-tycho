package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.type.TypeExpr;

import java.util.Objects;
import java.util.function.Consumer;

public class PatternVarDecl extends VarDecl {

	public PatternVarDecl(String name) {
		this(null, new NominalTypeExpr("<transient>"), name);
	}

	private PatternVarDecl(VarDecl original, TypeExpr type, String name) {
		super(original, type, name, null, true, false);
	}

	public PatternVarDecl copy(TypeExpr type, String name) {
		if (Objects.equals(getName(), name) && Objects.equals(getType(), type)) {
			return this;
		} else {
			return new PatternVarDecl(this, type, name);
		}
	}

	@Override
	public VarDecl withName(String name) {
		return copy(getType(), name);
	}

	public VarDecl withType(TypeExpr type) {
		return copy(type, getName());
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getType());
	}

	@Override
	public VarDecl transformChildren(Transformation transformation) {
		return copy(getType() == null ? null : transformation.applyChecked(TypeExpr.class, getType()), getName());
	}
}
