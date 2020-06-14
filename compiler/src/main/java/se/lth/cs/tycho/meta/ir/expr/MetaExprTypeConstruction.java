package se.lth.cs.tycho.meta.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.ExprTypeConstruction;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.Lists;
import se.lth.cs.tycho.meta.core.MetaArgument;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class MetaExprTypeConstruction extends MetaExpr {

	private final ExprTypeConstruction exprTypeConstruction;

	public MetaExprTypeConstruction(List<MetaArgument> arguments, ExprTypeConstruction exprTypeConstruction) {
		super(arguments);
		this.exprTypeConstruction = exprTypeConstruction;
	}

	public MetaExprTypeConstruction copy(List<MetaArgument> arguments, ExprTypeConstruction expression) {
		if (Lists.sameElements(getArguments(), arguments) && Objects.equals(getExprTypeConstruction(), expression)) {
			return this;
		} else {
			return new MetaExprTypeConstruction(arguments, expression);
		}
	}

	public ExprTypeConstruction getExprTypeConstruction() {
		return exprTypeConstruction;
	}

	public MetaExprTypeConstruction withArguments(List<MetaArgument> arguments) {
		return copy(arguments, getExprTypeConstruction());
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		getArguments().forEach(action);
		action.accept(getExprTypeConstruction());
	}

	@Override
	public Expression transformChildren(Transformation transformation) {
		return copy(transformation.mapChecked(MetaArgument.class, getArguments()), transformation.applyChecked(ExprTypeConstruction.class, getExprTypeConstruction()));
	}
}

