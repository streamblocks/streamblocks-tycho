package net.opendf.ir.net.ast;

import net.opendf.ir.common.GeneratorFilter;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

public class EntityListExpr extends EntityExpr {

	public EntityListExpr(ImmutableList<EntityExpr> entityList, ImmutableList<GeneratorFilter> generators) {
		this(null, entityList, generators);
	}

	private EntityListExpr(EntityListExpr original, ImmutableList<EntityExpr> entityList,
			ImmutableList<GeneratorFilter> generators) {
		super(original);
		this.entityList = ImmutableList.copyOf(entityList);
		this.generators = ImmutableList.copyOf(generators);
	}

	public EntityListExpr copy(ImmutableList<EntityExpr> entityList, ImmutableList<GeneratorFilter> generators) {
		if (Lists.equals(this.entityList, entityList) && Lists.equals(this.generators, generators)) {
			return this;
		}
		return new EntityListExpr(this, entityList, generators);
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
