package se.lth.cs.tycho.instance.am;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.function.Consumer;

public class Scope extends AbstractIRNode {
	private final ImmutableList<VarDecl> declarations;
	private final boolean isPersistent;

	public enum Lifespan { PERSISTENT, TRANSIENT }

	public Scope(ImmutableList<VarDecl> declarations, boolean isPersistent) {
		this(null, declarations, isPersistent);
	}

	private Scope(IRNode original, ImmutableList<VarDecl> declarations, boolean isPersistent) {
		super(original);
		this.declarations = ImmutableList.from(declarations);
		this.isPersistent = isPersistent;
	}
	
	public Scope copy(ImmutableList<VarDecl> declarations, boolean isPersistent) {
		if (Lists.equals(this.declarations, declarations)) {
			return this;
		}
		return new Scope(this, declarations, isPersistent);
	}

	public ImmutableList<VarDecl> getDeclarations() {
		return declarations;
	}
	
	public NamespaceDecl getLocation() {
		return null; // TODO remove this method
	}

	public boolean isPersistent() {
		return isPersistent;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		declarations.forEach(action);
	}
}
