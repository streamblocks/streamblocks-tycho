package net.opendf.ir.am;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.IRNode;
import net.opendf.ir.common.decl.LocalVarDecl;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

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
