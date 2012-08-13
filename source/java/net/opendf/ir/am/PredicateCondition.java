package net.opendf.ir.am;

import java.util.List;

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
	public <R,P> R accept(ConditionVisitor<R,P> v, P p) {
		return v.visitPredicateCondition(this, p);
	}
	

	public Expression getExpression() {
		return expression;
	}

	public List<Scope> getScope() {
		return scope;
	}
	
	//
	//  Ctor
	//
	
	public PredicateCondition(Expression expression, List<Scope> scope) {
		this.expression = expression;
		this.scope = scope;
	}
		
	private Expression  expression;
	private List<Scope>	scope;
}
