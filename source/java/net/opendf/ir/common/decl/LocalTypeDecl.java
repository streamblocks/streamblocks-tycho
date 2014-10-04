package net.opendf.ir.common.decl;

import java.util.Objects;

public class LocalTypeDecl extends TypeDecl implements LocalDecl {
	

	public LocalTypeDecl(String name) {
		this(null, name);
	}
	
	private LocalTypeDecl(LocalTypeDecl original, String name) {
		super(original, name);
	}
	
	public LocalTypeDecl copy(String name) {
		if (Objects.equals(getName(), name)) {
			return this;
		}
		return new LocalTypeDecl(this, name);
	}

	@Override
	public <R, P> R accept(LocalDeclVisitor<R, P> visitor, P param) {
		return visitor.visitLocalTypeDecl(this, param);
	}
}
