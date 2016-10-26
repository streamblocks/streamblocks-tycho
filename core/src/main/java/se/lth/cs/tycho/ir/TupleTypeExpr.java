package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class TupleTypeExpr extends AbstractIRNode implements TypeExpr<TupleTypeExpr> {
	private final ImmutableList<TypeExpr> types;

	public TupleTypeExpr(List<TypeExpr> types) {
		this(null, types);
	}
	private TupleTypeExpr(TupleTypeExpr original, List<TypeExpr> types) {
		super(original);
		this.types = ImmutableList.from(types);
	}

	public ImmutableList<TypeExpr> getTypes() {
		return types;
	}

	public TupleTypeExpr withTypes(List<TypeExpr> types) {
		if (Lists.sameElements(this.types, types)) {
			return this;
		} else {
			return new TupleTypeExpr(this, types);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		types.forEach(action);
	}

	@Override
	public TupleTypeExpr transformChildren(Transformation transformation) {
		return withTypes(transformation.mapChecked(TypeExpr.class, types));
	}
}
