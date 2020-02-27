package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.TypeExpr;

import java.util.Objects;
import java.util.function.Consumer;

public class PatternVarDecl extends VarDecl {

	public PatternVarDecl(String name) {
		this(null, null, name);
	}

	private PatternVarDecl(VarDecl original, TypeExpr type, String name) {
		super(original, type, name, null, true, false);
	}

	public PatternVarDecl copy(String name) {
		if (Objects.equals(getName(), name)) {
			return this;
		} else {
			return new PatternVarDecl(name);
		}
	}

	@Override
	public VarDecl withName(String name) {
		return copy(name);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {

	}

	@Override
	public VarDecl transformChildren(Transformation transformation) {
		return this;
	}
}
