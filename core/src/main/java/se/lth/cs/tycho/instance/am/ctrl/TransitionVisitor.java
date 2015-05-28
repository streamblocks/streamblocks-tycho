package se.lth.cs.tycho.instance.am.ctrl;

public interface TransitionVisitor<R, P> {
	R visitExec(Exec t, P p);

	R visitTest(Test t, P p);

	R visitWait(Wait t, P p);
}
