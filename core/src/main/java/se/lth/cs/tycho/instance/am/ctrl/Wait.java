package se.lth.cs.tycho.instance.am.ctrl;

import se.lth.cs.tycho.util.BitSets;

import java.util.BitSet;
import java.util.function.Consumer;
import java.util.function.Function;

public class Wait extends Instruction {
	private final State target;
	private final BitSet waitsFor;

	public Wait(State target, BitSet waitsFor) {
		this.target = target;
		this.waitsFor = BitSets.copyOf(waitsFor);
	}

	@Override
	public <R, P> R accept(InstructionVisitor<R, P> v, P p) {
		return v.visitWait(this, p);
	}

	@Override
	public <R> R accept(Function<Exec, R> ifExec, Function<Test, R> ifTest, Function<Wait, R> ifWait) {
		return ifWait.apply(this);
	}

	@Override
	public InstructionKind getKind() {
		return InstructionKind.WAIT;
	}

	public State target() {
		return target;
	}

	public BitSet waitsFor() {
		return BitSets.copyOf(waitsFor);
	}

	@Override
	public void forEachTarget(Consumer<State> action) {
		action.accept(target());
	}
}
