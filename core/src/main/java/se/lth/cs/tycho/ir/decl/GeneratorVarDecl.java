package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Objects;
import java.util.function.Consumer;

public class GeneratorVarDecl extends VarDecl {

	public GeneratorVarDecl(String name) {
		this(null, name);
	}
	private GeneratorVarDecl(VarDecl original, String name) {

		this(original, name, null);
	}


	private GeneratorVarDecl(VarDecl original, String name, TypeExpr type) {
		super(original, ImmutableList.empty(), type, name, null, true, false);
	}

	private GeneratorVarDecl copy(String name, TypeExpr type) {
		if (Objects.equals(getName(), name) && type == getType()) {
			return this;
		} else {
			return new GeneratorVarDecl(this, name, type);
		}
	}
	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		if (getType() != null)
			action.accept(getType());
	}


	@Override
	public GeneratorVarDecl withName(String name) {
		return copy(name, getType());
	}

	@Override
	public GeneratorVarDecl withType(TypeExpr type) {
		return copy(getName(), type);
	}

	@Override
	public GeneratorVarDecl transformChildren(Transformation transformation) {
		return copy(
				getName(),
				getType() == null ? null : transformation.applyChecked(TypeExpr.class, getType())
		);
	}

}
