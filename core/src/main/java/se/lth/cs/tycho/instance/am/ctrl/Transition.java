package se.lth.cs.tycho.instance.am.ctrl;

public interface Transition {
	<R, P> R accept(TransitionVisitor<R, P> v, P p);

	TransitionKind getKind();
}
