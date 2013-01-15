package net.opendf.interp;

import net.opendf.ir.common.Statement;

public interface Executor {
	
	public void execute(Statement stmt, Environment env);

}
