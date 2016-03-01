package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class EntityListExpr extends EntityExpr {

	public EntityListExpr(List<EntityExpr> entityList, List<GeneratorFilter> generators) {
		super(null);
		this.entityList = ImmutableList.from(entityList);
		this.generators = ImmutableList.from(generators);
	}

	public EntityListExpr copy(List<EntityExpr> entityList, List<GeneratorFilter> generators) {
		if (Lists.equals(this.entityList, entityList) && Lists.equals(this.generators, generators)) {
			return this;
		}
		return new EntityListExpr(entityList, generators);
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
	
	public String toString(){
		StringBuffer sb = new StringBuffer("[");
		String sep = "";
		for(EntityExpr e : entityList){
			sb.append(sep);
			sep = ", ";
			sb.append(e);
		}
		sep = "";
		for(GeneratorFilter g : generators){
			sb.append(sep);
			sep = ", ";
			sb.append(g);
		}
		sb.append("]");
		return sb.toString();
	}

	private ImmutableList<EntityExpr> entityList;
	private ImmutableList<GeneratorFilter> generators;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		entityList.forEach(action);
		generators.forEach(action);
		getAttributes().forEach(action);
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(
				(List) entityList.map(transformation),
				(List) generators.map(transformation)
		).withAttributes((List) getAttributes().map(transformation));
	}
}
