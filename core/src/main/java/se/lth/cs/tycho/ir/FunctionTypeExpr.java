package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class FunctionTypeExpr extends AbstractIRNode implements TypeExpr<FunctionTypeExpr> {
	private final ImmutableList<TypeExpr> parameterTypes;
	private final TypeExpr returnType;

	public FunctionTypeExpr(List<TypeExpr> parameterTypes, TypeExpr returnType) {
		this(null, parameterTypes, returnType);
	}

	private FunctionTypeExpr(FunctionTypeExpr original, List<TypeExpr> parameterTypes, TypeExpr returnType) {
		super(original);
		this.parameterTypes = ImmutableList.from(parameterTypes);
		this.returnType = returnType;
	}

	private FunctionTypeExpr copy(List<TypeExpr> parameterTypes, TypeExpr returnType) {
		if (Lists.sameElements(this.parameterTypes, parameterTypes) && this.returnType == returnType) {
			return this;
		} else {
			return new FunctionTypeExpr(parameterTypes, returnType);
		}
	}

	public ImmutableList<TypeExpr> getParameterTypes() {
		return parameterTypes;
	}

	public FunctionTypeExpr withParameterTypes(List<TypeExpr> parameterTypes) {
		return copy(parameterTypes, returnType);
	}

	public TypeExpr getReturnType() {
		return returnType;
	}

	public FunctionTypeExpr withReturnType(TypeExpr returnType) {
		return copy(parameterTypes, returnType);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		parameterTypes.forEach(action);
		action.accept(returnType);
	}

	@Override
	public FunctionTypeExpr transformChildren(Transformation transformation) {
		return copy(transformation.mapChecked(TypeExpr.class, parameterTypes), transformation.applyChecked(TypeExpr.class, returnType));
	}
}
