package se.lth.cs.tycho.types;

import java.util.List;
import java.util.stream.Collectors;

public class ProcType extends CallableType {
	public ProcType(List<Type> parameterTypes) {
		super(parameterTypes, UnitType.INSTANCE);
	}
	@Override
	public String toString() {
		String params = getParameterTypes().stream().map(Type::toString).collect(Collectors.joining(", "));
		return "[" + params + " --> ]";
	}
}
