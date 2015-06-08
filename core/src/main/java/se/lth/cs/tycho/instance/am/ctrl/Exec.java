package se.lth.cs.tycho.instance.am.ctrl;

public interface Exec extends Transition {
	static Exec of(int transition, State target) {
		return new Exec() {
			public int transition() {
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

	int transition();

	State target();

	@Override
	default State[] targets() {
		return new State[] { target() };
	}
}
