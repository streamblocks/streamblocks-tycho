package se.lth.cs.tycho.ir.decl;


public interface ParDecl extends Decl {
	public <R, P> R accept(ParDeclVisitor<R, P> visitor, P param);
}