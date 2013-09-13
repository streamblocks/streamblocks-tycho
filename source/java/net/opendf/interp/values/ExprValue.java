package net.opendf.interp.values;

import net.opendf.ir.IRNode;
import net.opendf.ir.common.ExprLiteral;

public class ExprValue extends ExprLiteral {
	RefView value;
	public ExprValue(ExprLiteral original) {
		super(original, original.getKind(), original.getText());
		BasicRef tmp = new BasicRef();
		value = tmp;
		//FIXME, type LONG is assumed
		tmp.setLong(Long.parseLong(original.getText()));
	}
	
	public ExprValue(ExprLiteral original, RefView value) {
		super(original, original.getKind(), original.getText());
		this.value = value;
	}

	public ExprValue(IRNode original, ExprLiteral.Kind kind, String text, RefView value) {
		super(original, kind, text);
		this.value = value;
	}

	public RefView getValue(){
		return value;
	}

}
