package se.lth.cs.tycho.ir.decl;

import java.util.Objects;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.expr.Expression;

public class GlobalVarDecl extends VarDecl implements GlobalDecl {
	private final Expression value;
	private final Availability availability;

	public Expression getValue() {
		return value;
	}

	@Override
	public Availability getAvailability() {
		return availability;
	}

	public GlobalVarDecl(TypeExpr type, String name, Expression value, Availability availability) {
		this(null, type, name, value, availability);
	}

	public GlobalVarDecl(IRNode original, TypeExpr type, String name, Expression value, Availability availability) {

		super(original, type, name);

		this.value = value;
		this.availability = availability;
	}

	public GlobalVarDecl copy(TypeExpr type, String name, Expression value, Availability availability) {
		if (Objects.equals(getType(), type) && Objects.equals(getName(), name)
				&& Objects.equals(this.value, value) && getAvailability() == availability) {
			return this;
		}
		return new GlobalVarDecl(this, type, name, value, availability);
	}

	@Override
	public <R, P> R accept(GlobalDeclVisitor<R, P> visitor, P param) {
		return visitor.visitGlobalVarDecl(this, param);
	}
}
