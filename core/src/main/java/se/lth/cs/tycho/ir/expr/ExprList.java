package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class ExprList extends Expression {
	private ImmutableList<Expression> elements;

	public ExprList(List<Expression> elements) {
		this(null, elements);
	}

	private ExprList(ExprList original, List<Expression> elements) {
		super(original);
		this.elements = ImmutableList.from(elements);
	}

	public ImmutableList<Expression> getElements() {
		return elements;
	}

	public ExprList withElements(List<Expression> elements) {
		if (Lists.sameElements(this.elements, elements)) {
			return this;
		} else {
			return new ExprList(this, elements);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		elements.forEach(action);
	}

	@Override
	public ExprList transformChildren(Transformation transformation) {
		return withElements(
				transformation.mapChecked(Expression.class, elements)
		);
	}
}
