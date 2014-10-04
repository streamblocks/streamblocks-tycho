package net.opendf.ir.common.decl;


public interface GlobalDecl extends Decl {
	public enum Visibility {
		PUBLIC, PRIVATE, LOCAL;
	}
	
	public Visibility getVisibility();
	
	public <R, P> R accept(GlobalDeclVisitor<R, P> visitor, P param);
}
