package se.lth.cs.tycho.ir.entity.am.ctrl;

import java.util.function.Consumer;
import java.util.function.Function;

public class Test extends Instruction {
	private final int condition;
	private final State targetTrue;
	private final State targetFalse;

	public Test(int condition, State targetTrue, State targetFalse) {
		this.condition = condition;
		this.targetTrue = targetTrue;
		this.targetFalse = targetFalse;
	}

	@Override
	public <R, P> R accept(InstructionVisitor<R, P> v, P p) {
		return v.visitTest(this, p);
	}

	@Override
	public <R> R accept(Function<Exec, R> ifExec, Function<Test, R> ifTest, Function<Wait, R> ifWait) {
		return ifTest.apply(this);
	}

	@Override
	public InstructionKind getKind() {
		return InstructionKind.TEST;
	}

	public int condition() {
		return condition;
	}

	public State targetTrue() {
		return targetTrue;
	}

	public State targetFalse() {
		return targetFalse;
	}

	@Override
	public void forEachTarget(Consumer<State> action) {
		action.accept(targetTrue());
		action.accept(targetFalse());
	}
}
