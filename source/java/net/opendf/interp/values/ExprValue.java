package net.opendf.interp.values;

import java.util.HashMap;
import java.util.Map;

import net.opendf.interp.exception.CALNumberFormatException;
import net.opendf.ir.IRNode;
import net.opendf.ir.common.ExprLiteral;

public class ExprValue extends ExprLiteral {
	RefView value;
	public ExprValue(ExprLiteral original) {
		super(original, original.getKind(), original.getText());
		// look in the table of constants
		value = constants.get(original.getText());
		if(value == null){
			BasicRef tmp = new BasicRef();
			value = tmp;
			try{
				tmp.setLong(Long.parseLong(original.getText()));
			} catch (NumberFormatException eL){
				try{
					tmp.setDouble(Double.parseDouble(original.getText()));
				} catch (NumberFormatException eD){
					throw new CALNumberFormatException(original.getText());
				}
			}
		}
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

	private static Map<String, RefView> constants = new HashMap<String, RefView>();
	static {
		constants.put("true", ConstRef.of(0));
		constants.put("false", ConstRef.of(1));
	}

}
