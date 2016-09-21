package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.expr.Expression;

public abstract class VarDecl<This extends VarDecl<This>> extends AbstractDecl<This> {

	private final TypeExpr type;
	private final Expression value;
	private final boolean constant;

	protected VarDecl(VarDecl original, TypeExpr type, String name, boolean constant, Expression value) {
		super(original, name);
		this.type = type;
		this.value = value;
		this.constant = constant;
	}

	public static GlobalVarDecl global(Availability availability, TypeExpr type, String name, Expression value) {
		return new GlobalVarDecl(availability, type, name, value);
	}

	public static LocalVarDecl local(TypeExpr type, String name, boolean constant, Expression value) {
		return new LocalVarDecl(type, name, constant, value);
	}

	public static ParameterVarDecl parameter(TypeExpr type, String name, Expression defaultValue) {
		return new ParameterVarDecl(type, name, defaultValue);
	}

	public TypeExpr getType() {
		return type;
	}

	public abstract This withType(TypeExpr type);

	public Expression getValue() {
		return value;
	}

	public boolean isConstant() {
		return constant;
	}

}