package se.lth.cs.tycho.ir.entity.am;

/**
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public interface ConditionVisitor<R,P> {
	
	R visitInputCondition(PortCondition c, P p);
	R visitOutputCondition(PortCondition c, P p);
	R visitPredicateCondition(PredicateCondition c, P p);

}
