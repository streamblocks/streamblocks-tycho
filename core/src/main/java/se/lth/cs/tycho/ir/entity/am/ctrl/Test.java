package se.lth.cs.tycho.ir.entity.am.ctrl;

import java.util.function.Consumer;
import java.util.function.Function;

public class Test extends Instruction {
	private final int condition;
	private final State targetTrue;
	private final State targetFalse;

	// When considering a MultiInstructionState and making use of the OrderedConditionChecking (OCC) reducer, a unique
	// order number is assigned to each Test instruction. The OCC transforms the MultiInstructionState to a
	// SingleInstructionState by selecting the Test instruction with the lowest order number.
	private final int orderNumber;

	public Test(int condition, State targetTrue, State targetFalse, int orderNumber) {
		this.condition = condition;
		this.targetTrue = targetTrue;
		this.targetFalse = targetFalse;
		this.orderNumber = orderNumber;
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

	public int getOrderNumber() {
		return orderNumber;
	}
}
