package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class EntityListExpr extends AbstractIRNode implements EntityExpr {

	public EntityListExpr(List<EntityExpr> entityList) {
		this(null, entityList);
	}

	private EntityListExpr(EntityListExpr original, List<EntityExpr> entityList) {
		super(original);
		this.entityList = ImmutableList.from(entityList);
	}

	public EntityListExpr copy(List<EntityExpr> entityList) {
		if (Lists.sameElements(this.entityList, entityList)) {
			return this;
		}
		return new EntityListExpr(this, entityList);
	}

	public ImmutableList<EntityExpr> getEntityList() {
		return entityList;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder("[");
		String sep = "";
		for(EntityExpr e : entityList){
			sb.append(sep);
			sep = ", ";
			sb.append(e);
		}
		sb.append("]");
		return sb.toString();
	}

	private ImmutableList<EntityExpr> entityList;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		entityList.forEach(action);
	}

	@Override
	public EntityListExpr transformChildren(Transformation transformation) {
		return copy(
				transformation.mapChecked(EntityExpr.class, entityList)
		);
	}

	@Override
	public <R, P> R accept(EntityExprVisitor<R, P> v, P p) {
		return v.visitEntityListExpr(this, p);
	}
}
