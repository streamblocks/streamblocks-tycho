package se.lth.cs.tycho.instance.am.ctrl;

public interface Wait extends Transition {
	static Wait of(State target) {
		return new Wait() {
			public State target() {
				return target;
			}
		};
	}

	@Override
	default <R, P> R accept(TransitionVisitor<R, P> v, P p) {
		return v.visitWait(this, p);
	}

	default TransitionKind getKind() {
		return TransitionKind.WAIT;
	}

	State target();
}
