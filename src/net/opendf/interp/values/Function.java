package net.opendf.interp.values;

import net.opendf.interp.ProceduralExecutor;

public interface Function extends Value {
	public RefView apply(ProceduralExecutor exec);
}
