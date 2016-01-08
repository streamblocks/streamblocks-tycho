package se.lth.cs.tycho.transform.util;

import java.util.List;
import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.Transition;

import se.lth.cs.tycho.ir.QID;

public interface Controller<S> {
	public List<GenInstruction<S>> instructions(S state);
	public S initialState();
	public QID instanceId();
        public Condition getCondition(int c);
        public Transition getTransition(int t);
}
