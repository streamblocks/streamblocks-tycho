package se.lth.cs.tycho.ir.decl;


public interface GlobalDecl extends Decl {
	public enum Availability {
		PUBLIC, PRIVATE, LOCAL;
	}
	
	public Availability getAvailability();
	
	public <R, P> R accept(GlobalDeclVisitor<R, P> visitor, P param);
}
