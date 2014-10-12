package se.lth.cs.tycho.instance.am;

import java.util.Objects;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

public class Scope extends AbstractIRNode {
	private final ImmutableList<LocalVarDecl> declarations;
	private final NamespaceDecl location;

	public Scope(ImmutableList<LocalVarDecl> declarations, NamespaceDecl origin) {
		this(null, declarations, origin);
	}

	private Scope(IRNode original, ImmutableList<LocalVarDecl> declarations, NamespaceDecl origin) {
		super(original);
		this.declarations = ImmutableList.copyOf(declarations);
		this.location = origin;
	}
	
	public Scope copy(ImmutableList<LocalVarDecl> declarations, NamespaceDecl origin) {
		if (Lists.equals(this.declarations, declarations) && Objects.equals(this.location, origin)) {
			return this;
		}
		return new Scope(this, declarations, origin);
	}

	public ImmutableList<LocalVarDecl> getDeclarations() {
		return declarations;
	}
	
	public NamespaceDecl getLocation() {
		return location;
	}

}
