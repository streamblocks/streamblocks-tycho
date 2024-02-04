package streamblocks.tycho.phase;

import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.phase.Phase;
import se.lth.cs.tycho.reporting.CompilationException;

public class SimulatorPhase implements Phase {
    @Override
    public String getDescription() {
        return "Simulator phase";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        return null;
    }
}
