package net.opendf.interp.values;

import net.opendf.interp.Environment;
import net.opendf.interp.Interpreter;
import net.opendf.ir.common.ExprProc;
import net.opendf.ir.common.Statement;

public class ProcProcedure implements Procedure {

	public final ExprProc proc;
	public final Environment closure;

	public ProcProcedure(ExprProc proc, Environment closure) {
		this.proc = proc;
		this.closure = closure;
	}

	@Override
	public Value copy() {
		return this;
	}

	@Override
	public void exec(Interpreter interpreter) {
		for (Statement s : proc.getBody()) {
			interpreter.execute(s, closure);
		}
	}

}
