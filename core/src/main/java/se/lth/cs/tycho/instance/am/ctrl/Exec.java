package se.lth.cs.tycho.instance.am.ctrl;

public interface Exec extends Transition {
	static Exec of(se.lth.cs.tycho.instance.am.Transition transition, State target) {
		return new Exec() {
			public se.lth.cs.tycho.instance.am.Transition transition() {
				return transition;
			}

			public State target() {
				return target;
			}
		};
	}

	@Override
	default <R, P> R accept(TransitionVisitor<R, P> v, P p) {
		return v.visitExec(this, p);
	}

	default TransitionKind getKind() {
		return TransitionKind.EXEC;
	}

	se.lth.cs.tycho.instance.am.Transition transition();

	State target();


}
