package se.lth.cs.tycho.ir.expr.pattern;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.ExprLiteral;

import java.util.Objects;
import java.util.function.Consumer;

public class PatternLiteral extends Pattern {

	private ExprLiteral literal;

	public PatternLiteral(ExprLiteral literal) {
		this(null, literal);
	}

	public PatternLiteral(IRNode original, ExprLiteral literal) {
		super(original);
		this.literal = literal;
	}

	public PatternLiteral copy(ExprLiteral literal) {
		if (Objects.equals(getLiteral(), literal)) {
			return this;
		} else {
			return new PatternLiteral(this, literal);
		}
	}

	public ExprLiteral getLiteral() {
		return literal;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getLiteral());
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(transformation.applyChecked(ExprLiteral.class, getLiteral()));
	}
}
