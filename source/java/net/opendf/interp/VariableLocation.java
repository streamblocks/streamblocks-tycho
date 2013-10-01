package net.opendf.interp;

import net.opendf.ir.IRNode;
import net.opendf.ir.common.Variable;

public class VariableLocation extends Variable {

	private int offset;
	
	public static VariableLocation scopeVariable(IRNode original, String name, int scopeId, int offset){
		return new VariableLocation(original, name, scopeId, offset, true);
	}
	public static VariableLocation stackVariable(IRNode original, String name, int offset){
		return new VariableLocation(original, name, -1, offset, false);
	}

	private VariableLocation(IRNode original, String name, int scope, int offset, boolean isStatic){
		super(original, name, scope, isStatic);
		this.offset = offset;
	}
	
	VariableLocation copy(String name, int scope, int offset, boolean isStatic){
		return new 	VariableLocation(this, name, scope, offset, isStatic);
	}
	
	public int getOffset(){
		return offset;
	}
}
