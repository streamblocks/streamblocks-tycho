package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class ProcedureTypeExpr extends AbstractIRNode implements TypeExpr<ProcedureTypeExpr> {
	private final ImmutableList<TypeExpr> parameterTypes;

	public ProcedureTypeExpr(List<TypeExpr> parameterTypes) {
		this(null, parameterTypes);
	}

	private ProcedureTypeExpr(ProcedureTypeExpr original, List<TypeExpr> parameterTypes) {
		super(original);
		this.parameterTypes = ImmutableList.from(parameterTypes);
	}

	private ProcedureTypeExpr copy(List<TypeExpr> parameterTypes) {
		if (Lists.sameElements(this.parameterTypes, parameterTypes)) {
			return this;
		} else {
			return new ProcedureTypeExpr(parameterTypes);
		}
	}

	public ImmutableList<TypeExpr> getParameterTypes() {
		return parameterTypes;
	}

	public ProcedureTypeExpr withParameterTypes(List<TypeExpr> parameterTypes) {
		return copy(parameterTypes);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		parameterTypes.forEach(action);
	}

	@Override
	public ProcedureTypeExpr transformChildren(Transformation transformation) {
		return copy(transformation.mapChecked(TypeExpr.class, parameterTypes));
	}
}
