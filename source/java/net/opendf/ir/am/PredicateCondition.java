package net.opendf.ir.am;

import net.opendf.ir.common.Expression;
import net.opendf.ir.util.ImmutableList;

/**
 * A predicate condition represents the {@link Condition condition} that a
 * boolean expression evaluates to <tt>true</tt>.
 * 
 * It contains the expression itself, as well as a list of actor machine
 * variables that are required by the expression.
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

	public ImmutableList<Integer> getRequiredVars() {
		return required;
	}

	public PredicateCondition(Expression expression, ImmutableList<Integer> required) {
		this.expression = expression;
		this.required = ImmutableList.copyOf(required);
	}

	private Expression expression;
	private ImmutableList<Integer> required;
}
