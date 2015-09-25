package se.lth.cs.tycho.instance.am;

import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

public class Scope extends AbstractIRNode {
	private final ImmutableList<VarDecl> declarations;
	private final NamespaceDecl location;

	public Scope(ImmutableList<VarDecl> declarations, NamespaceDecl origin) {
		this(null, declarations, origin);
	}

	private Scope(IRNode original, ImmutableList<VarDecl> declarations, NamespaceDecl origin) {
		super(original);
		this.declarations = ImmutableList.from(declarations);
		this.location = origin;
	}
	
	public Scope copy(ImmutableList<VarDecl> declarations, NamespaceDecl origin) {
		if (Lists.equals(this.declarations, declarations) && Objects.equals(this.location, origin)) {
			return this;
		}
		return new Scope(this, declarations, origin);
	}

	public ImmutableList<VarDecl> getDeclarations() {
		return declarations;
	}
	
	public NamespaceDecl getLocation() {
		return location;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		declarations.forEach(action);
	}
}
