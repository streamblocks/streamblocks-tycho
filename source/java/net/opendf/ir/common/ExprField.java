package net.opendf.ir.common;

public class ExprField extends Expression {
	private Expression structure;
	private Field field;

	public ExprField(Expression structure, Field field) {
		this.structure = structure;
		this.field = field;
	}

	public Expression getStructure() {
		return structure;
	}

	public Field getField() {
		return field;
	}

	@Override
	public <R, P> R accept(ExpressionVisitor<R, P> v, P p) {
		return v.visitExprField(this, p);
	}

}
