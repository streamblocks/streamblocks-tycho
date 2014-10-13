package se.lth.cs.tycho.backend.c;

public class CArrayType extends CType {
	private final CType elementType;
	private final String dimension;

	public CArrayType(CType elementType, String dimension) {
		this.elementType = elementType;
		this.dimension = dimension;
	}

	public CType getElementType() {
		return elementType;
	}

	public String getDimension() {
		return dimension;
	}

	public String plainType() {
		return "*" + elementType.plainType();
	}

	public String variableType(String variable) {
		return elementType.variableType(variable) + "[" + dimension + "]";
	}

	@Override
	public String toString() {
		return "CArrayType [elementType=" + elementType + ", dimension=" + dimension + "]";
	}

}
