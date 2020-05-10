package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class ExprTuple extends Expression {

	private final ImmutableList<Expression> elements;

	public ExprTuple(List<Expression> elements) {
		this(null, elements);
	}

	public ExprTuple(IRNode original, List<Expression> elements) {
		super(original);
		this.elements = ImmutableList.from(elements);
	}

	public ExprTuple copy(List<Expression> elements) {
		if (Lists.sameElements(getElements(), elements)) {
			return this;
		} else {
			return new ExprTuple(this, elements);
		}
	}

	public ImmutableList<Expression> getElements() {
		return elements;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		elements.forEach(action);
	}

	@Override
	public Expression transformChildren(Transformation transformation) {
		return copy(transformation.mapChecked(Expression.class, getElements()));
	}
}
