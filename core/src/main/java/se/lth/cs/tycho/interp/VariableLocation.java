package se.lth.cs.tycho.interp;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;

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

	public String toString() {
		if (isScopeVariable()) {
			return "ScopeVariable(" + getName() + ", " + getScopeId() + ", " + offset + ")";
		} else {
			return "Variable(" + getName() + ", " + offset + ")";
		}
	}
}
