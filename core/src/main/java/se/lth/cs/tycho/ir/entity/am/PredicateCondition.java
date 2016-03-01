package se.lth.cs.tycho.ir.entity.am;

import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.expr.Expression;

/**
 * A predicate condition represents the {@link Condition condition} that a
 * boolean expression evaluates to <tt>true</tt>.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 * 
 */

public class PredicateCondition extends Condition {

	@Override
	public ConditionKind kind() {
		return ConditionKind.predicate;
	}

	@Override
	public <R, P> R accept(ConditionVisitor<R, P> v, P p) {
		return v.visitPredicateCondition(this, p);
	}

	public Expression getExpression() {
		return expression;
	}
	
	public NamespaceDecl getLocation() {
		return null; // TODO remove this method
	}

	public PredicateCondition(Expression expression) {
		this(null, expression);
	}

	private PredicateCondition(PredicateCondition original, Expression expression) {
		super(original);
		this.expression = expression;
	}

	public PredicateCondition copy(Expression expression) {
		if (Objects.equals(this.expression, expression)) {
			return this;
		}
		return new PredicateCondition(this, expression);
	}

	private Expression expression;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(expression);
	}

	@Override
	public PredicateCondition transformChildren(Transformation transformation) {
		return copy((Expression) transformation.apply(expression));
	}
}

