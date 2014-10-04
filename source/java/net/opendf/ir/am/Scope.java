package net.opendf.ir.am;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.IRNode;
import net.opendf.ir.common.decl.DeclVar;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

public class Scope extends AbstractIRNode {
	private final ImmutableList<DeclVar> declarations;

	public Scope(ImmutableList<DeclVar> declarations) {
		this(null, declarations);
	}

	private Scope(IRNode original, ImmutableList<DeclVar> declarations) {
		super(original);
		this.declarations = ImmutableList.copyOf(declarations);
	}
	
	public Scope copy(ImmutableList<DeclVar> declarations) {
		if (Lists.equals(this.declarations, declarations)) {
			return this;
		}
		return new Scope(this, declarations);
	}

	public ImmutableList<DeclVar> getDeclarations() {
		return declarations;
	}

}
