package se.lth.cs.tycho.types;

import java.util.List;
import java.util.stream.Collectors;

public class LambdaType extends CallableType {
	public LambdaType(List<Type> parameterTypes, Type returnType) {
		super(parameterTypes, returnType);
	}

	@Override
	public String toString() {
		String params = getParameterTypes().stream().map(Type::toString).collect(Collectors.joining(", "));
		return "[" + params + " --> " + getReturnType() + "]";
	}
}
