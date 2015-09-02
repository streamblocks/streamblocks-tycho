package se.lth.cs.tycho.instance.am;

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
		return location;
	}

	public PredicateCondition(Expression expression, NamespaceDecl location) {
		this(null, expression, location);
	}

	private PredicateCondition(PredicateCondition original, Expression expression, NamespaceDecl location) {
		super(original);
		this.expression = expression;
		this.location = location;
	}

	public PredicateCondition copy(Expression expression, NamespaceDecl origin) {
		if (Objects.equals(this.expression, expression) && Objects.equals(this.location, origin)) {
			return this;
		}
		return new PredicateCondition(this, expression, origin);
	}

	private Expression expression;
	private final NamespaceDecl location;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(expression);
	}
}
