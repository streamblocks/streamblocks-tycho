package se.lth.cs.tycho.ir.decl;

public interface GlobalDecl extends Decl {
	Availability getAvailability();
	GlobalDecl withAvailability(Availability availability);
}
