package se.lth.cs.tycho.ir.expr;

import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.Field;
import se.lth.cs.tycho.ir.IRNode;

public class ExprField extends Expression {
	private Expression structure;
	private Field field;

	public ExprField(Expression structure, Field field) {
		this(null, structure, field);
	}

	private ExprField(ExprField original, Expression structure, Field field) {
		super(original);
		this.structure = structure;
		this.field = field;
	}

	public ExprField copy(Expression structure, Field field) {
		if (Objects.equals(this.structure, structure) && Objects.equals(this.field, field)) {
			return this;
		}
		return new ExprField(this, structure, field);
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

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(structure);
		action.accept(field);
	}

	@Override
	public ExprField transformChildren(Transformation transformation) {
		return copy((Expression) transformation.apply(structure), (Field) transformation.apply(field));
	}
}
