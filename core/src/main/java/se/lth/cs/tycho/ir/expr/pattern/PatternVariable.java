package se.lth.cs.tycho.ir.expr.pattern;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.PatternVarDecl;

import java.util.Objects;
import java.util.function.Consumer;

public class PatternVariable extends Pattern {

	private PatternVarDecl declaration;

	public PatternVariable(PatternVarDecl declaration) {
		this(null, declaration);
	}

	public PatternVariable(IRNode original, PatternVarDecl declaration) {
		super(original);
		this.declaration = declaration;
	}

	public PatternVarDecl getDeclaration() {
		return declaration;
	}

	public PatternVariable copy(PatternVarDecl binding) {
		if (Objects.equals(getDeclaration(), binding)) {
			return this;
		} else {
			return new PatternVariable(this, binding);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getDeclaration());
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy((PatternVarDecl) transformation.apply(getDeclaration()));
	}
}
