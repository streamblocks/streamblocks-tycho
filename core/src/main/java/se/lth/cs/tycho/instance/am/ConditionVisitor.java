package se.lth.cs.tycho.instance.am;

/**
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public interface ConditionVisitor<R,P> {
	
	public R visitInputCondition(PortCondition c, P p);
	public R visitOutputCondition(PortCondition c, P p);
	public R visitPredicateCondition(PredicateCondition c, P p);

}
