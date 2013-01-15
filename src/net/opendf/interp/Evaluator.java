package net.opendf.interp;

import net.opendf.interp.values.RefView;
import net.opendf.ir.common.Expression;

public interface Evaluator {
	
	public RefView evaluate(Expression expr, Environment mem);

}
