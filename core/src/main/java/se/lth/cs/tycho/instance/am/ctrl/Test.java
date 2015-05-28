package se.lth.cs.tycho.instance.am.ctrl;

import se.lth.cs.tycho.instance.am.Condition;

public interface Test extends Transition {
	static Test of(Condition cond, State targetTrue, State targetFalse) {
		return new Test() {
			public Condition condition() {
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

	Condition condition();

	State targetTrue();

	State targetFalse();

}
