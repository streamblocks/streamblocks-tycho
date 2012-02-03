package net.opendf.ir.am;

import net.opendf.ir.AbstractIRNode;

/**
 * Conditions are part of {@link ITest test instructions} and describe situations that are either true or false during the execution of
 * an actor machine, viz. the existence of sufficiently many input tokens at a specific port, the existence of sufficient space in a
 * specified output queue, and the value of a boolean predicate that could depend on input tokens and state variables.
 * 
 * @see PortCondition
 * @see PredicateCondition
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

abstract public class Condition extends AbstractIRNode {
	
	public enum ConditionKind { input, output, predicate };
	
	abstract public ConditionKind  kind();
	
	abstract public void accept(ConditionVisitor v);

}
