package net.opendf.interp.values;

import net.opendf.interp.ProceduralExecutor;

public interface Procedure extends Value {
	public void exec(ProceduralExecutor exec);
}
