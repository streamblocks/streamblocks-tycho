package se.lth.cs.tycho.transform.util;

import java.util.List;

import se.lth.cs.tycho.ir.QID;

public interface Controller<S> {
	public List<GenInstruction<S>> instructions(S state);
	public S initialState();
	public QID instanceId();
}
