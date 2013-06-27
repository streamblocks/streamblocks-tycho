package net.opendf.interp.attr;

import net.opendf.interp.values.RefView;
import net.opendf.ir.common.ExprLiteral;

public aspect Literals {
	private RefView ExprLiteral.value;
	public void ExprLiteral.setValue(RefView v) {
		if (value != null) {
			throw new IllegalStateException("Value is already set");
		}
		value = v;
	}
	public RefView ExprLiteral.getValue() {
		return value;
	}

}
