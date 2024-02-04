package streamblocks.tycho.platform;

import se.lth.cs.tycho.compiler.Compiler;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.Phase;
import se.lth.cs.tycho.phase.RemoveUnusedEntityDeclsPhase;
import se.lth.cs.tycho.platform.Platform;
import streamblocks.tycho.phase.SimulatorPhase;

import java.util.List;

public class Simulator implements Platform {
    @Override
    public String name() {
        return "simulator";
    }

    @Override
    public String description() {
        return "CAL simulator based on AM";
    }

    private static final List<Phase> phases = ImmutableList.<Phase> builder()
            .addAll(Compiler.frontendPhases())
            .addAll(Compiler.templatePhases())
            .addAll(Compiler.portEnumerationPhases())
            .addAll(Compiler.networkElaborationPhases())
            .addAll(Compiler.nameAndTypeAnalysis())
            .addAll(Compiler.actorMachinePhases())
            .add(new RemoveUnusedEntityDeclsPhase())
            .add(new SimulatorPhase())
            .build();

    @Override
    public List<Phase> phases() {
        return phases;
    }
}
