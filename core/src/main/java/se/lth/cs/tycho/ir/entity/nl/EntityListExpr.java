package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.function.Consumer;

public class EntityListExpr extends EntityExpr {

	public EntityListExpr(ImmutableList<EntityExpr> entityList, ImmutableList<GeneratorFilter> generators) {
		this.entityList = ImmutableList.copyOf(entityList);
		this.generators = ImmutableList.copyOf(generators);
	}

	public EntityListExpr copy(ImmutableList<EntityExpr> entityList, ImmutableList<GeneratorFilter> generators) {
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
	}
}
