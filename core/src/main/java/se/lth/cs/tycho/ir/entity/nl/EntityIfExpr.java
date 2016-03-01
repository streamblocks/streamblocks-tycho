package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public class EntityIfExpr extends EntityExpr {

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

	public Expression getCondition() {
		return condition;
	}

	public EntityExpr getTrueEntity() {
		return trueEntity;
	}

	public EntityExpr getFalseEntity() {
		return falseEntity;
	}

	@Override
	public <R, P> R accept(EntityExprVisitor<R, P> v, P p) {
		return v.visitEntityIfExpr(this, p);
	}

	private EntityExpr trueEntity, falseEntity;
	private Expression condition;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(condition);
		action.accept(trueEntity);
		action.accept(falseEntity);
		getAttributes().forEach(action);
	}

	@Override
	@SuppressWarnings("unchecked")
	public IRNode transformChildren(Transformation transformation) {
		return copy(
				(Expression) transformation.apply(condition),
				(EntityExpr) transformation.apply(trueEntity),
				(EntityExpr) transformation.apply(falseEntity)
		).withAttributes((List) getAttributes().map(transformation));
	}
}
