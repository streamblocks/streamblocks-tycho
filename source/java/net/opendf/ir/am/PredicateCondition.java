package net.opendf.ir.am;

import java.util.Objects;

import net.opendf.ir.common.Expression;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

/**
 * A predicate condition represents the {@link Condition condition} that a
 * boolean expression evaluates to <tt>true</tt>.
 * 
 * It contains the expression itself, as well as a list of scopes that need to
 * be initialized before evaluating the expression.
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

	public ImmutableList<Integer> getScopes() {
		return scopes;
	}

	public PredicateCondition(Expression expression, ImmutableList<Integer> required) {
		this(null, expression, required);
	}

	private PredicateCondition(PredicateCondition original, Expression expression, ImmutableList<Integer> scopes) {
		super(original);
		this.expression = expression;
		this.scopes = ImmutableList.copyOf(scopes);
	}

	public PredicateCondition copy(Expression expression, ImmutableList<Integer> scopes) {
		if (Objects.equals(this.expression, expression) && Lists.equals(this.scopes, scopes)) {
			return this;
		}
		return new PredicateCondition(this, expression, scopes);
	}

	private Expression expression;
	private ImmutableList<Integer> scopes;
}
