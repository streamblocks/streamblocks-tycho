package net.opendf.ir.decl;

import java.util.Objects;

public class GlobalTypeDecl extends TypeDecl implements GlobalDecl {

	private final Visibility visibility;

	public GlobalTypeDecl(String name, Visibility visibility) {
		this(null, name, visibility);
	}

	private GlobalTypeDecl(GlobalTypeDecl original, String name, Visibility visibility) {
		super(original, name);
		this.visibility = visibility;
	}

	public GlobalTypeDecl copy(String name, Visibility visibility) {
		if (Objects.equals(getName(), name)) {
			return this;
		}
		return new GlobalTypeDecl(this, name, visibility);
	}

	@Override
	public Visibility getVisibility() {
		return visibility;
	}

	@Override
	public <R, P> R accept(GlobalDeclVisitor<R, P> visitor, P param) {
		return visitor.visitGlobalTypeDecl(this, param);
	}
}
