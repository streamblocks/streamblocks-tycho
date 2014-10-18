package se.lth.cs.tycho.ir.decl;

import java.util.Objects;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.expr.Expression;

public class VarDecl extends Decl {

	private final TypeExpr type;
	private final Expression value;
	private final boolean constant;

	private VarDecl(IRNode original, LocationKind locationKind, Availability availability, TypeExpr type, String name,
			boolean constant, Expression value) {
		super(original, locationKind, availability, DeclKind.VAR, name);
		this.type = type;
		this.value = value;
		this.constant = constant;
	}

	public static VarDecl global(Availability availability, TypeExpr type, String name, Expression value) {
		return new VarDecl(null, LocationKind.GLOBAL, availability, type, name, true, value);
	}

	public static VarDecl local(TypeExpr type, String name, boolean constant, Expression value) {
		return new VarDecl(null, LocationKind.LOCAL, Availability.LOCAL, type, name, constant, value);
	}

	public static VarDecl parameter(TypeExpr type, String name) {
		return new VarDecl(null, LocationKind.PARAMETER, Availability.LOCAL, type, name, true, null);
	}

	public VarDecl copyAsGlobal(Availability availability, TypeExpr type, String name, Expression value) {
		return getIfDifferent(global(availability, type, name, value));
	}
	
	public VarDecl copyAsLocal(TypeExpr type, String name, boolean constant, Expression value) {
		return getIfDifferent(local(type, name, constant, value));
	}
	
	public VarDecl copyAsParameter(TypeExpr type, String name) {
		return getIfDifferent(parameter(type, name));
	}

	private VarDecl getIfDifferent(VarDecl that) {
		if (this.getLocationKind() == that.getLocationKind() && this.getAvailability() == that.getAvailability()
				&& Objects.equals(this.type, that.type) && Objects.equals(this.getName(), that.getName())
				&& this.constant == that.constant && Objects.equals(this.value, that.value)) {
			return this;
		} else {
			return that;
		}
	}

	public TypeExpr getType() {
		return type;
	}

	public Expression getValue() {
		return value;
	}

	public boolean isConstant() {
		return constant;
	}

}