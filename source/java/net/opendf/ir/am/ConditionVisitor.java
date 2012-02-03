package net.opendf.ir.am;

/**
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public interface ConditionVisitor {
	
	public void  visitInputCondition(PortCondition c);
	public void  visitOutputCondition(PortCondition c);
	public void  visitPredicateCondition(PredicateCondition c);

}
