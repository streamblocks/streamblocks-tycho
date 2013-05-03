package net.opendf.ir.common;

import java.util.Objects;

/**
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 * 
 */

public class ParDeclValue extends ParDecl {

	@Override
	public ParameterKind parameterKind() {
		return ParameterKind.value;
	}

	public TypeExpr getType() {
		return type;
	}

	public ParDeclValue(String name, TypeExpr type) {
		this(null, name, type);
	}

	private ParDeclValue(ParDeclValue original, String name, TypeExpr type) {
		super(original, name);
		this.type = type;
	}
	
	public ParDeclValue copy(String name, TypeExpr type) {
		if (Objects.equals(getName(), name) && Objects.equals(this.type, type)) {
			return this;
		}
		return new ParDeclValue(this, name, type);
	}

	private TypeExpr type;

}
