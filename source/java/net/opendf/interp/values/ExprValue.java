package net.opendf.interp.values;

import java.util.HashMap;
import java.util.Map;

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
					tmp.setString(original.getText());
				}
			}
		}
	}
	
	public ExprValue(IRNode original, ExprLiteral.Kind kind, String text, RefView value) {
		super(original, kind, text);
		BasicRef tmp = new BasicRef();
		value.assignTo(tmp);
		this.value = tmp;
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
