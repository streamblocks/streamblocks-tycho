package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;

import java.util.Objects;
import java.util.function.Consumer;

public class InputVarDecl extends VarDecl {

	private static long count = 0;

	public InputVarDecl() {
		this(String.format("$input%d", count++));
	}

	public InputVarDecl(String name) {
		this(null, name);
	}

	private InputVarDecl(VarDecl original, String name) {
		super(original, null, name, null, true, false);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
	}

	@Override
	public InputVarDecl withName(String name) {
		return Objects.equals(getName(), name) ? this : new InputVarDecl(name);
	}

	@Override
	public InputVarDecl transformChildren(Transformation transformation) {
		return this;
	}

}
