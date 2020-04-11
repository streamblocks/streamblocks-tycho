package se.lth.cs.tycho.ir.expr.pattern;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.PatternVarDecl;

import java.util.Objects;
import java.util.function.Consumer;

public class PatternWildcard extends PatternDeclaration {

	public PatternWildcard(IRNode original) {
		this(original, new PatternVarDecl(""));
	}

	public PatternWildcard(IRNode original, PatternVarDecl declaration) {
		super(original, declaration);
	}

	public PatternWildcard copy(PatternVarDecl declaration) {
		if (Objects.equals(getDeclaration(), declaration)) {
			return this;
		} else {
			return new PatternWildcard(this, declaration);
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
