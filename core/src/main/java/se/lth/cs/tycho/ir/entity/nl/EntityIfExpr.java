package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Per Andersson
 * 
 */

public class EntityIfExpr extends AbstractIRNode implements EntityExpr {

	public EntityIfExpr(Expression condition, EntityExpr trueEntity, EntityExpr falseEntity) {
		this(null, condition, trueEntity, falseEntity);
	}
	public EntityIfExpr(EntityExpr original, Expression condition, EntityExpr trueEntity, EntityExpr falseEntity) {
		super(original);
		this.condition = condition;
		this.trueEntity = trueEntity;
		this.falseEntity = falseEntity;
	}

	public EntityIfExpr copy(Expression condition, EntityExpr trueEntity, EntityExpr falseEntity) {
		if (Objects.equals(this.condition, condition) && Objects.equals(this.trueEntity, trueEntity)
				&& Objects.equals(this.falseEntity, falseEntity)) {
			return this;
		}
		return new EntityIfExpr(this, condition, trueEntity, falseEntity);
	}

	public EntityIfExpr clone() {
		return (EntityIfExpr) super.clone();
	}

	public Expression getCondition() {
		return condition;
	}

	public EntityExpr getTrueEntity() {
		return trueEntity;
	}

	public EntityExpr getFalseEntity() {
		return falseEntity;
	}

	private EntityExpr trueEntity, falseEntity;
	private Expression condition;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(condition);
		action.accept(trueEntity);
		action.accept(falseEntity);
	}

	@Override
	@SuppressWarnings("unchecked")
	public IRNode transformChildren(Transformation transformation) {
		return copy(
				(Expression) transformation.apply(condition),
				(EntityExpr) transformation.apply(trueEntity),
				(EntityExpr) transformation.apply(falseEntity)
		);
	}
}
