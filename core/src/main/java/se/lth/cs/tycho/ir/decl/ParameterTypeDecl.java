package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;

import java.util.Objects;
import java.util.function.Consumer;

public class ParameterTypeDecl extends TypeDecl {
	private ParameterTypeDecl(ParameterTypeDecl original, String name) {
		super(original, name);
	}

	public ParameterTypeDecl copy(String name) {
		if (Objects.equals(this.getName(), name)) {
			return this;
		} else {
			return new ParameterTypeDecl(this, name);
		}
	}

	@Override
	public ParameterTypeDecl withName(String name) {
		return copy(name);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
	}

	@Override
	public Decl transformChildren(Transformation transformation) {
		return this;
	}
}
