package net.opendf.ir.am;

import net.opendf.ir.common.Expression;

/**
 * A predicate condition represents the {@link Condition condition} that a boolean expression evaluates to <tt>true</tt>. 
 * 
 * It contains the expression itself, as well as the id of the local scope which the expression is to be evaluated in.
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
	public void accept(ConditionVisitor v) {
		v.visitPredicateCondition(this);
	}
	

	public Expression getExpression() {
		return expression;
	}

	public int getScope() {
		return scope;
	}
	
	//
	//  Ctor
	//
	
	public PredicateCondition(Expression expression, int scope) {
		this.expression = expression;
		this.scope = scope;
	}
		
	private Expression  expression;
	private int			scope;
}
