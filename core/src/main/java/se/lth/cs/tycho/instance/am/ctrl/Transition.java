package se.lth.cs.tycho.instance.am.ctrl;

public interface Transition {
	<R, P> R accept(TransitionVisitor<R, P> v, P p);
	default <R> R accept(TransitionVisitor<R, Void> v) {
		return accept(v, null);
	}

	TransitionKind getKind();

	State[] targets();
}
