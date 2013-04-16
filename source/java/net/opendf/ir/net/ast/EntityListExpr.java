package net.opendf.ir.net.ast;

import net.opendf.ir.common.GeneratorFilter;
import net.opendf.ir.util.ImmutableList;

public class EntityListExpr extends EntityExpr {

	public EntityListExpr(ImmutableList<EntityExpr> entityList, ImmutableList<GeneratorFilter> generators) {
		this.entityList = ImmutableList.copyOf(entityList);
		this.generators = ImmutableList.copyOf(generators);
	}

	public ImmutableList<EntityExpr> getEntityList() {
		return entityList;
	}

	public ImmutableList<GeneratorFilter> getGenerators() {
		return generators;
	}

	@Override
	public <R, P> R accept(EntityExprVisitor<R, P> v, P p) {
		return v.visitEntityListExpr(this, p);
	}

	private ImmutableList<EntityExpr> entityList;
	private ImmutableList<GeneratorFilter> generators;
}
