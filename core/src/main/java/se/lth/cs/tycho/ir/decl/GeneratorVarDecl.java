package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;

import java.util.Objects;
import java.util.function.Consumer;

public class GeneratorVarDecl extends VarDecl {

	public GeneratorVarDecl(String name) {
		this(null, name);
	}
	private GeneratorVarDecl(VarDecl original, String name) {
		super(original, null, name, null, true, false);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
	}

	@Override
	public GeneratorVarDecl withName(String name) {
		return Objects.equals(getName(), name) ? this : new GeneratorVarDecl(name);
	}

	@Override
	public GeneratorVarDecl transformChildren(Transformation transformation) {
		return this;
	}

}
