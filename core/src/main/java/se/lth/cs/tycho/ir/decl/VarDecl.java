package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.expr.Expression;

public abstract class VarDecl extends AbstractDecl {

	private final TypeExpr type;
	private final Expression value;
	private final boolean constant;
	private final boolean external;

	protected VarDecl(VarDecl original, TypeExpr type, String name, Expression value, boolean constant, boolean external) {
		super(original, name);
		this.type = type;
		this.value = value;
		this.constant = constant;
		this.external = external;
	}

	public static GlobalVarDecl global(Availability availability, TypeExpr type, String name, Expression value) {
		return new GlobalVarDecl(availability, type, name, value);
	}

	public static LocalVarDecl local(TypeExpr type, String name, Expression value, boolean constant) {
		return new LocalVarDecl(type, name, value, constant);
	}

	public static ParameterVarDecl parameter(TypeExpr type, String name, Expression defaultValue) {
		return new ParameterVarDecl(type, name, defaultValue);
	}

	public static InputVarDecl input(String name) {
		return new InputVarDecl(name);
	}

	public static GeneratorVarDecl generator(String name) {
		return new GeneratorVarDecl(name);
	}

	@Override
	public abstract VarDecl withName(String name);

	public TypeExpr getType() {
		return type;
	}

	public Expression getValue() {
		return value;
	}

	public boolean isConstant() {
		return constant;
	}

	public boolean isExternal() {
		return external;
	}

	@Override
	public abstract VarDecl transformChildren(Transformation transformation);

}