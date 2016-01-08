package se.lth.cs.tycho.ir.entity.am.ctrl;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Instruction {
	public abstract <R, P> R accept(InstructionVisitor<R, P> v, P p);

	public <R> R accept(InstructionVisitor<R, Void> v) {
		return accept(v, null);
	}

	public abstract <R> R accept(Function<Exec, R> ifExec, Function<Test, R> ifTest, Function<Wait, R> ifWait);

	public abstract InstructionKind getKind();

	public abstract void forEachTarget(Consumer<State> action);

}
