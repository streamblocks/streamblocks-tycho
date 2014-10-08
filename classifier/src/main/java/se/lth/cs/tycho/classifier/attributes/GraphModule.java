package se.lth.cs.tycho.classifier.attributes;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.ICall;
import se.lth.cs.tycho.ir.entity.am.ITest;
import se.lth.cs.tycho.ir.entity.am.IWait;
import se.lth.cs.tycho.ir.entity.am.State;
import se.lth.cs.tycho.ir.entity.am.Transition;
import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;

public class GraphModule extends Module<GraphModule.Decls> {
	public interface Decls {
		@Synthesized
		public Condition condition(ITest t);

		@Synthesized
		public Transition transition(ICall c);

		@Inherited
		State lookupState(IRNode node, int index);

		@Inherited
		Condition lookupCondition(IRNode node, int index);

		@Inherited
		Transition lookupTransition(IRNode node, int index);

		@Synthesized
		State destination(IWait w);

		@Synthesized
		State destination(ICall c);

		@Synthesized
		State destinationTrue(ITest t);

		@Synthesized
		State destinationFalse(ITest t);
	}

	public State lookupState(ActorMachine actorMachine, int index) {
		return actorMachine.getController().get(index);
	}

	public Condition lookupCondition(ActorMachine actorMachine, int index) {
		return actorMachine.getCondition(index);
	}

	public Transition lookupTransition(ActorMachine actorMachine, int index) {
		return actorMachine.getTransition(index);
	}

	public State destination(ICall c) {
		return e().lookupState(c, c.S());
	}

	public State destination(IWait w) {
		return e().lookupState(w, w.S());
	}

	public State destinationTrue(ITest t) {
		return e().lookupState(t, t.S1());
	}

	public State destinationFalse(ITest t) {
		return e().lookupState(t, t.S0());
	}

	public Condition condition(ITest t) {
		return e().lookupCondition(t, t.C());
	}

	public Transition transition(ICall c) {
		return e().lookupTransition(c, c.T());
	}

}
