package se.lth.cs.tycho.backend.c.att;

import java.util.Set;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.ICall;
import se.lth.cs.tycho.instance.am.ITest;
import se.lth.cs.tycho.instance.am.IWait;
import se.lth.cs.tycho.instance.am.Instruction;
import se.lth.cs.tycho.instance.am.State;
import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import javarag.Collected;
import javarag.Inherited;
import javarag.Module;
import javarag.coll.Builder;
import javarag.coll.Builders;
import javarag.coll.Collector;

public class ControllerGraph extends Module<ControllerGraph.Decls> {

	public interface Decls {
		@Inherited
		State predecessor(Instruction i);

		@Collected
		Set<Instruction> predecessors(State s);

		ActorMachine actorMachine(IRNode n);
	}

	public State predecessor(State s) {
		return s;
	}

	public State predecessor(AbstractIRNode n) {
		return null;
	}

	public Builder<Set<State>, State> predecessors(State s) {
		return Builders.setBuilder();
	}

	public void predecessors(ITest t, Collector<Instruction> coll) {
		ActorMachine am = e().actorMachine(t);
		coll.add(am.getController().get(t.S0()), t);
		coll.add(am.getController().get(t.S1()), t);
	}

	public void predecessors(ICall c, Collector<Instruction> coll) {
		ActorMachine am = e().actorMachine(c);
		coll.add(am.getController().get(c.S()), c);
	}

	public void predecessors(IWait w, Collector<Instruction> coll) {
		ActorMachine am = e().actorMachine(w);
		coll.add(am.getController().get(w.S()), w);
	}

}
