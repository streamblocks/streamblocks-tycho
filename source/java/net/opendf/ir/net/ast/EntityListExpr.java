package net.opendf.ir.net.ast;

import net.opendf.ir.common.GeneratorFilter;

public class EntityListExpr extends EntityExpr {

	public EntityListExpr(EntityExpr[] entityList, GeneratorFilter[] generators){
		this.entityList = entityList;
		this.generators = generators;
	}

	public EntityExpr[] getEntityList(){
		return entityList;
	}

	public GeneratorFilter[] getGenerators(){
		return generators;
	}

	@Override
	public <R, P> R accept(EntityExprVisitor<R, P> v, P p) {
		return v.visitEntityListExpr(this, p);
	}
	private EntityExpr[] entityList;
	private GeneratorFilter[] generators;
}
