package se.lth.cs.tycho.ir.expr.pattern;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.PatternVarDecl;

public abstract class PatternDeclaration extends Pattern {

	protected PatternVarDecl declaration;

	public PatternDeclaration(IRNode original, PatternVarDecl declaration) {
		super(original);
		this.declaration = declaration;
	}

	public PatternVarDecl getDeclaration() {
		return declaration;
	}
}
