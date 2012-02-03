package net.opendf.ir.common;


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
	
	public TypeExpr  getType() { return type; }


	public ParDeclValue(String name, TypeExpr type) {
		super(name);
		
		this.type = type;
	}
	
	private TypeExpr type;

}
