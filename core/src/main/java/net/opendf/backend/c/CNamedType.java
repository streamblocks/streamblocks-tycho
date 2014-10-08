package net.opendf.backend.c;

public class CNamedType extends CType {
	private final String name;

	public CNamedType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String plainType() {
		return name;
	}

	public String variableType(String variable) {
		return name + " " + variable;
	}

	@Override
	public String toString() {
		return "CNamedType [name=" + name + "]";
	}

}
