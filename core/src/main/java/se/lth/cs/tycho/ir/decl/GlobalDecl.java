package se.lth.cs.tycho.ir.decl;

public interface GlobalDecl<This extends GlobalDecl<This>> extends Decl<This> {
	Availability getAvailability();
	This withAvailability(Availability availability);
}
