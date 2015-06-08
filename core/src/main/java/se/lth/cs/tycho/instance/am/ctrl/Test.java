package se.lth.cs.tycho.instance.am.ctrl;

public interface Test extends Transition {
	static Test of(int cond, State targetTrue, State targetFalse) {
		return new Test() {
			public int condition() {
				return cond;
			}

			public State targetTrue() {
				return targetTrue;
			}

			public State targetFalse() {
				return targetFalse;
			}
		};
	}

	@Override
	default <R, P> R accept(TransitionVisitor<R, P> v, P p) {
		return v.visitTest(this, p);
	}

	default TransitionKind getKind() {
		return TransitionKind.TEST;
	}

	int condition();

	State targetTrue();

	State targetFalse();

	@Override
	default State[] targets() {
		return new State[] { targetTrue(), targetFalse() };
	}
}
