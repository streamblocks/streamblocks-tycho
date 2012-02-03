package net.opendf.ir.common;


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
		super(name);
	}


}
