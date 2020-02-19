package se.lth.cs.tycho.type;

import java.util.List;
import java.util.Objects;

public abstract class CallableType implements Type {
	private final List<Type> parameterTypes;
	private final Type returnType;

	public CallableType(List<Type> parameterTypes, Type returnType) {
		this.parameterTypes = parameterTypes;
		this.returnType = returnType;
	}

	public Type getReturnType() {
		return returnType;
	}

	public List<Type> getParameterTypes() {
		return parameterTypes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CallableType)) return false;
		CallableType that = (CallableType) o;
		return Objects.equals(parameterTypes, that.parameterTypes) &&
				Objects.equals(returnType, that.returnType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(parameterTypes, returnType);
	}
}
