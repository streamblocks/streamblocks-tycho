package net.opendf.backend.c.att;

import java.util.HashSet;
import java.util.Set;

import javarag.CollectionBuilder;
import javarag.CollectionContribution;
import javarag.Inherited;
import javarag.Module;
import javarag.coll.Builder;
import javarag.coll.CollectionWrapper;
import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.IRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.ICall;
import net.opendf.ir.am.ITest;
import net.opendf.ir.am.IWait;
import net.opendf.ir.am.Instruction;
import net.opendf.ir.am.State;

public class ControllerGraph extends Module<ControllerGraph.Required> {

	@Inherited
	public State predecessor(State s) {
		return s;
	}

	@Inherited
	public State predecessor(AbstractIRNode n) {
		return null;
	}

	@CollectionBuilder("predecessors")
	public Builder predecessorsBuilder(State s) {
		return new CollectionWrapper(new HashSet<>());
	}

	@CollectionContribution
	public void predecessors(ITest t) {
		ActorMachine am = get().actorMachine(t);
		contribute(am.getController().get(t.S0()), t);
		contribute(am.getController().get(t.S1()), t);
	}

	@CollectionContribution
	public void predecessors(ICall c) {
		ActorMachine am = get().actorMachine(c);
		contribute(am.getController().get(c.S()), c);
	}

	@CollectionContribution
	public void predecessors(IWait w) {
		ActorMachine am = get().actorMachine(w);
		contribute(am.getController().get(w.S()), w);
	}

	public interface Required {
		ActorMachine actorMachine(IRNode n);
	}

	public interface Provided {
		State predecessor(Instruction i);

		Set<Instruction> predecessors(State s);
	}

}
