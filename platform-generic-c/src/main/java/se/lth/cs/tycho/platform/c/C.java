package se.lth.cs.tycho.platform.c;

import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.CBackendPhase;
import se.lth.cs.tycho.phase.Phase;
import se.lth.cs.tycho.compiler.Compiler;
import se.lth.cs.tycho.phase.RemoveUnusedEntityDeclsPhase;
import se.lth.cs.tycho.platform.Platform;

import java.util.List;

public class C implements Platform {
	@Override
	public String name() {
		return "generic-c";
	}

	@Override
	public String description() {
		return "A backend for sequential C code.";
	}

	private static final List<Phase> phases = ImmutableList.<Phase> builder()
			.addAll(Compiler.frontendPhases())
			.addAll(Compiler.templatePhases())
			.addAll(Compiler.portEnumerationPhases())
			.addAll(Compiler.networkElaborationPhases())
			.addAll(Compiler.nameAndTypeAnalysis())
			.addAll(Compiler.actorMachinePhases())
			.add(new RemoveUnusedEntityDeclsPhase())
			.add(new CBackendPhase())
			.build();

	@Override
	public List<Phase> phases() {
		return phases;
	}
}
