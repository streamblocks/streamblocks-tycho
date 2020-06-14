package se.lth.cs.tycho.ir.type;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class UnionTypeExpr extends AbstractIRNode implements TypeExpr<UnionTypeExpr> {

	private final ImmutableList<TypeExpr> types;

	public UnionTypeExpr(List<TypeExpr> types) {
		this(null, types);
	}

	public UnionTypeExpr(IRNode original, List<TypeExpr> types) {
		super(original);
		this.types = ImmutableList.from(types);
	}

	public UnionTypeExpr copy(List<TypeExpr> types) {
		if (Lists.sameElements(getTypes(), types)) {
			return this;
		} else {
			return new UnionTypeExpr(this, types);
		}
	}

	public ImmutableList<TypeExpr> getTypes() {
		return types;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		getTypes().forEach(action);
	}

	@Override
	public UnionTypeExpr transformChildren(Transformation transformation) {
		return copy(transformation.mapChecked(TypeExpr.class, getTypes()));
	}
}
