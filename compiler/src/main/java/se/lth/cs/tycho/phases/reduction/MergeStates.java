package se.lth.cs.tycho.phases.reduction;

import se.lth.cs.tycho.ir.entity.am.ctrl.Exec;
import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.InstructionVisitor;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;
import se.lth.cs.tycho.ir.entity.am.ctrl.Test;
import se.lth.cs.tycho.ir.entity.am.ctrl.Wait;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class MergeStates implements Function<State, State> {
	private final Map<I, State> cache;

	public MergeStates() {
		cache = new HashMap<>();
	}

	@Override
	public State apply(State state) {
		if (state.getInstructions().size() == 1) {
			Instruction i = state.getInstructions().get(0);
			return cache.computeIfAbsent(i.accept(wrap), x -> state);
		}
		return state;
	}

	private static final InstructionVisitor<I, Void> wrap = new InstructionVisitor<I, Void>() {
		@Override
		public I visitExec(Exec t, Void aVoid) {
			return new E(t);
		}

		@Override
		public I visitTest(Test t, Void aVoid) {
			return new T(t);
		}

		@Override
		public I visitWait(Wait t, Void aVoid) {
			return new W(t);
		}
	};

	private static class I {}
	private static final class W extends I {
		private final State target;
		private W(Wait w) {
			this.target = w.target();
		}
		@Override
		public boolean equals(Object o) {
			return !(o instanceof W) || target == ((W) o).target;
		}
		@Override
		public int hashCode() {
			return target.hashCode();
		}
	}
	private static final class E extends I {
		private final int transition;
		private final State target;
		private E(Exec e) {
			this.transition = e.transition();
			this.target = e.target();
		}
		@Override
		public boolean equals(Object o) {
			if (o instanceof E) {
				E e = (E) o;
				return transition == e.transition && target == e.target;
			} else {
				return false;
			}
		}
		@Override
		public int hashCode() {
			return Objects.hash(transition, target);
		}
	}
	private static final class T extends I {
		private final int condition;
		private final State targetTrue;
		private final State targetFalse;
		private T(Test t) {
			this.condition = t.condition();
			this.targetTrue = t.targetTrue();
			this.targetFalse = t.targetFalse();
		}
		@Override
		public boolean equals(Object o) {
			if (o instanceof T) {
				T t = (T) o;
				return condition == t.condition && targetTrue == t.targetTrue && targetFalse == t.targetFalse;
			} else {
				return false;
			}
		}
		@Override
		public int hashCode() {
			return Objects.hash(condition, targetTrue, targetFalse);
		}
	}
}
