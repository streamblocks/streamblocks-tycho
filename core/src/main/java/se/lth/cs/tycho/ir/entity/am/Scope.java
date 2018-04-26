package se.lth.cs.tycho.ir.entity.am;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.function.Consumer;

public class Scope extends AbstractIRNode {
	private final ImmutableList<LocalVarDecl> declarations;
	private final boolean isPersistent;

	public Scope(ImmutableList<LocalVarDecl> declarations, boolean isPersistent) {
		this(null, declarations, isPersistent);
	}

	private Scope(IRNode original, ImmutableList<LocalVarDecl> declarations, boolean isPersistent) {
		super(original);
		this.declarations = ImmutableList.from(declarations);
		this.isPersistent = isPersistent;
	}
	
	public Scope copy(ImmutableList<LocalVarDecl> declarations, boolean isPersistent) {
		if (Lists.equals(this.declarations, declarations)) {
			return this;
		}
		return new Scope(this, declarations, isPersistent);
	}

	public ImmutableList<LocalVarDecl> getDeclarations() {
		return declarations;
	}

	public boolean isPersistent() {
		return isPersistent;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		declarations.forEach(action);
	}

	@Override
	public Scope transformChildren(Transformation transformation) {
		return copy((ImmutableList) declarations.map(transformation), isPersistent);
	}
}
