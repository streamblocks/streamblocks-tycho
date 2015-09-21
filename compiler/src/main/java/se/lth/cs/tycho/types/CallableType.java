package se.lth.cs.tycho.types;

import java.util.List;

public abstract class CallableType implements Type {
	private final List<Type> parameterTypes;
	private final Type returnType;

	CallableType(List<Type> parameterTypes, Type returnType) {
		this.parameterTypes = parameterTypes;
		this.returnType = returnType;
	}

	public Type getReturnType() {
		return returnType;
	}

	public List<Type> getParameterTypes() {
		return parameterTypes;
	}


}
