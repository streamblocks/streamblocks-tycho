package net.opendf.backend.c.att;

import java.util.Set;

import javarag.Collected;
import javarag.Inherited;
import javarag.Module;
import javarag.coll.Builder;
import javarag.coll.Builders;
import javarag.coll.Collector;
import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.IRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.ICall;
import net.opendf.ir.am.ITest;
import net.opendf.ir.am.IWait;
import net.opendf.ir.am.Instruction;
import net.opendf.ir.am.State;

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
