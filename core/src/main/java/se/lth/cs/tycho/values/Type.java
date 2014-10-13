package se.lth.cs.tycho.values;

import java.util.List;
import java.util.Objects;

import se.lth.cs.tycho.ir.Parameter;

public class Type {
	public String name;
	public List<Parameter<Type>> typeParameters;
	public List<Parameter<Value>> valueParameters;

	public Type(String name, List<Parameter<Type>> typeParameters, List<Parameter<Value>> valueParameters) {
		this.name = name;
		this.typeParameters = typeParameters;
		this.valueParameters = valueParameters;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Parameter<Type>> getTypeParameters() {
		return typeParameters;
	}

	public void setTypeParameters(List<Parameter<Type>> typeParameters) {
		this.typeParameters = typeParameters;
	}

	public List<Parameter<Value>> getValueParameters() {
		return valueParameters;
	}

	public void setValueParameters(List<Parameter<Value>> valueParameters) {
		this.valueParameters = valueParameters;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, typeParameters, valueParameters);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Type) {
			Type that = (Type) obj;
			return Objects.equals(this.name, that.name) && Objects.equals(this.typeParameters, that.typeParameters)
					&& Objects.equals(this.valueParameters, that.valueParameters);
		} else {
			return false;
		}
	}

}
