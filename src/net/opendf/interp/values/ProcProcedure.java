package net.opendf.interp.values;

import net.opendf.interp.Environment;
import net.opendf.interp.Executor;
import net.opendf.interp.Simulator;
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
	public void exec(int args, Simulator sim) {
		if (args != proc.getValueParameters().length) {
			throw new IllegalArgumentException("Wrong number of arguments");
		}
		Executor exec = sim.executor();
		for (Statement s : proc.getBody()) {
			exec.execute(s, closure);
		}
	}

}
