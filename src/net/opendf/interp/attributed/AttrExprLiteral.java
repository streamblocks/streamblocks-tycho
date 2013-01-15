package net.opendf.interp.attributed;

import net.opendf.interp.values.ConstRef;
import net.opendf.ir.common.ExprLiteral;

public class AttrExprLiteral extends ExprLiteral {
	public final ConstRef value;
	public AttrExprLiteral(ExprLiteral base, ConstRef value) {
		super(base.getKind(), base.getText());
		this.value = value;
	}
}
