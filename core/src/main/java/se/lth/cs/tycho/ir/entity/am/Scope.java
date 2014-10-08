package se.lth.cs.tycho.ir.entity.am;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

public class Scope extends AbstractIRNode {
	private final ImmutableList<LocalVarDecl> declarations;

	public Scope(ImmutableList<LocalVarDecl> declarations) {
		this(null, declarations);
	}

	private Scope(IRNode original, ImmutableList<LocalVarDecl> declarations) {
		super(original);
		this.declarations = ImmutableList.copyOf(declarations);
	}
	
	public Scope copy(ImmutableList<LocalVarDecl> declarations) {
		if (Lists.equals(this.declarations, declarations)) {
			return this;
		}
		return new Scope(this, declarations);
	}

	public ImmutableList<LocalVarDecl> getDeclarations() {
		return declarations;
	}

}
