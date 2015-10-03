package se.lth.cs.tycho.instance.am.ctrl;

import java.util.function.Consumer;
import java.util.function.Function;

public class Exec extends Instruction {
	private final int transition;
	private final State target;

	public Exec(int transition, State target) {
		this.transition = transition;
		this.target = target;
	}

	@Override
	public <R, P> R accept(InstructionVisitor<R, P> v, P p) {
		return v.visitExec(this, p);
	}

	@Override
	public <R> R accept(Function<Exec, R> ifExec, Function<Test, R> ifTest, Function<Wait, R> ifWait) {
		return ifExec.apply(this);
	}

	@Override
	public InstructionKind getKind() {
		return InstructionKind.EXEC;
	}

	public int transition() {
		return transition;
	}

	public State target() {
		return target;
	}

	@Override
	public void forEachTarget(Consumer<State> action) {
		action.accept(target());
	}
}
