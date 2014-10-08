package se.lth.cs.tycho.ir.decl;

import java.util.Objects;

public class ParDeclType extends TypeDecl implements ParDecl {

	public ParDeclType(String name) {
		this(null, name);
	}

	private ParDeclType(ParDeclType original, String name) {
		super(original, name);
	}

	public ParDeclType copy(String name) {
		if (Objects.equals(getName(), name)) {
			return this;
		}
		return new ParDeclType(this, name);
	}

	@Override
	public <R, P> R accept(ParDeclVisitor<R, P> visitor, P param) {
		return visitor.visitParDeclType(this, param);
	}

}
