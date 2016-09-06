package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.function.Consumer;

public class VarDecl extends Decl {

	private final TypeExpr type;
	private final Expression value;
	private final boolean constant;

	private VarDecl(VarDecl original, LocationKind locationKind, Availability availability, TypeExpr type, String name,
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

	public static VarDecl importDecl(QID qid, String name) {
		if (name == null) {
			name = qid.getLast().toString();
		}
		return new VarDecl(null, LocationKind.GLOBAL, Availability.LOCAL, null, name, true, new ExprGlobalVariable(qid));
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

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		if (type != null) action.accept(type);
		if (value != null) action.accept(value);
	}

	@Override
	public VarDecl transformChildren(Transformation transformation) {
		TypeExpr type = this.type == null ? null : (TypeExpr) transformation.apply(this.type);
		Expression value = this.value == null ? null : (Expression) transformation.apply(this.value);
		if (type == this.type && value == this.value) {
			return this;
		} else {
			return new VarDecl(this,
					getLocationKind(),
					getAvailability(),
					type,
					getName(),
					constant,
					value);
		}
	}

	public VarDecl withAvailability(Availability availability) {
		if (getAvailability() == availability) {
			return this;
		} else {
			return new VarDecl(this, getLocationKind(), availability, type, getName(), constant, value);
		}
	}

	public VarDecl withValue(Expression value) {
		if (this.value == value) {
			return this;
		} else {
			return new VarDecl(this, getLocationKind(), getAvailability(), type, getName(), constant, value);
		}
	}

	public VarDecl withName(String name) {
		if (this.getName().equals(name)) {
			return this;
		} else {
			return new VarDecl(this, getLocationKind(), getAvailability(), type, name, constant, value);
		}
	}

	public VarDecl clone() {
		return (VarDecl) super.clone();
	}

	public VarDecl deepClone() {
		return (VarDecl) super.deepClone();
	}
}