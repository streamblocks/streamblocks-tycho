package se.lth.cs.tycho.meta.ir.entity.nl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.nl.EntityExprVisitor;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.util.Lists;
import se.lth.cs.tycho.meta.core.MetaArgument;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class MetaEntityInstanceExpr extends MetaEntityExpr {

	private final EntityInstanceExpr entityInstanceExpr;

	public MetaEntityInstanceExpr(List<MetaArgument> arguments, EntityInstanceExpr entityInstanceExpr) {
		super(arguments);
		this.entityInstanceExpr = entityInstanceExpr;
	}

	public EntityInstanceExpr getEntityInstanceExpr() {
		return entityInstanceExpr;
	}

	public MetaEntityInstanceExpr copy(List<MetaArgument> arguments, EntityInstanceExpr entityInstanceExpr) {
		if (Lists.sameElements(getArguments(), arguments) && Objects.equals(getEntityInstanceExpr(), entityInstanceExpr)) {
			return this;
		} else {
			return new MetaEntityInstanceExpr(arguments, entityInstanceExpr);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		getArguments().forEach(action);
		action.accept(getEntityInstanceExpr());
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(transformation.mapChecked(MetaArgument.class, getArguments()), transformation.applyChecked(EntityInstanceExpr.class, getEntityInstanceExpr()));
	}

	@Override
	public IRNode clone() {
		return super.clone();
	}

	@Override
	public <R, P> R accept(EntityExprVisitor<R, P> v, P p) {
		return null;
	}
}
