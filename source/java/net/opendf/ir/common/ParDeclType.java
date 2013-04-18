package net.opendf.ir.common;

import java.util.Objects;

/**
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 * 
 */

public class ParDeclType extends ParDecl {

	@Override
	public ParameterKind parameterKind() {
		return ParameterKind.type;
	}

	public ParDeclType(String name) {
		this(null, name);
	}

	private ParDeclType(ParDeclType original, String name) {
		super(original, name);
	}

	public ParDeclType copy(String name) {
		if (Objects.equals(getName(), name)) {
			return this;
		}
		return new ParDeclType(this, name);
	}

}
