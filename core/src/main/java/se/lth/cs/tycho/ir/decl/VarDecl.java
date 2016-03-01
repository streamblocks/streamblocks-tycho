package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Objects;
import java.util.function.Consumer;

public class VarDecl extends Decl {

	private final TypeExpr type;
	private final Expression value;
	private final boolean constant;

	private VarDecl(VarDecl original, LocationKind locationKind, Availability availability, TypeExpr type, String name,
			boolean constant, Expression value, QID qualifiedIdentifier) {
		super(original, locationKind, availability, DeclKind.VAR, name, qualifiedIdentifier);
		this.type = type;
		this.value = value;
		this.constant = constant;
	}

	public static VarDecl global(Availability availability, TypeExpr type, String name, Expression value) {
		return new VarDecl(null, LocationKind.GLOBAL, availability, type, name, true, value, null);
	}

	public static VarDecl local(TypeExpr type, String name, boolean constant, Expression value) {
		return new VarDecl(null, LocationKind.LOCAL, Availability.LOCAL, type, name, constant, value, null);
	}

	public static VarDecl parameter(TypeExpr type, String name) {
		return new VarDecl(null, LocationKind.PARAMETER, Availability.LOCAL, type, name, true, null, null);
	}

	public static VarDecl importDecl(QID qid, String name) {
		if (name == null) {
			name = qid.getLast().toString();
		}
		return new VarDecl(null, LocationKind.GLOBAL, Availability.LOCAL, null, name, true, null, qid);
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
					value,
					getQualifiedIdentifier());
		}
	}

	public VarDecl withLocationKind(LocationKind locationKind) {
		if (getLocationKind() == locationKind) {
			return this;
		} else {
			return new VarDecl(this, locationKind, getAvailability(), type, getName(), constant, value, getQualifiedIdentifier());
		}
	}

	public VarDecl withAvailability(Availability availability) {
		if (getAvailability() == availability) {
			return this;
		} else {
			return new VarDecl(this, getLocationKind(), availability, type, getName(), constant, value, getQualifiedIdentifier());
		}
	}

	public VarDecl withValue(Expression value) {
		if (this.value == value) {
			return this;
		} else {
			return new VarDecl(this, getLocationKind(), getAvailability(), type, getName(), constant, value, getQualifiedIdentifier());
		}
	}

	public VarDecl withName(String name) {
		if (this.getName().equals(name)) {
			return this;
		} else {
			return new VarDecl(this, getLocationKind(), getAvailability(), type, name, constant, value, getQualifiedIdentifier());
		}
	}

	public VarDecl withQualifiedIdentifier(QID qid) {
		if (this.getQualifiedIdentifier().equals(qid)) {
			return this;
		} else {
			return new VarDecl(this, getLocationKind(), getAvailability(), type, getName(), constant, value, qid);
		}
	}

	public VarDecl clone() {
		return (VarDecl) super.clone();
	}

	public VarDecl deepClone() {
		return (VarDecl) super.deepClone();
	}
}