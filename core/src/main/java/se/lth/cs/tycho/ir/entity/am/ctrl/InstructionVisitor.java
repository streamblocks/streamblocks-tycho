package se.lth.cs.tycho.ir.entity.am.ctrl;

public interface InstructionVisitor<R, P> {
	R visitExec(Exec t, P p);

	R visitTest(Test t, P p);

	R visitWait(Wait t, P p);
}
