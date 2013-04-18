package net.opendf.ir.net.ast;

import java.util.Objects;

import net.opendf.ir.common.Expression;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public class EntityIfExpr extends EntityExpr {

	public EntityIfExpr(Expression condition, EntityExpr trueEntity, EntityExpr falseEntity) {
		this(null, condition, trueEntity, falseEntity);
	}

	private EntityIfExpr(EntityIfExpr original, Expression condition, EntityExpr trueEntity, EntityExpr falseEntity) {
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
}
