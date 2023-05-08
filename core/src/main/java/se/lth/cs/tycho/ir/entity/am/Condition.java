package se.lth.cs.tycho.ir.entity.am;

import se.lth.cs.tycho.ir.AbstractIRNode;

/**
 * Conditions are part of test instructions and describe situations that are either true or false during the execution of
 * an calActor machine, viz. the existence of sufficiently many input tokens at a specific port, the existence of sufficient space in a
 * specified output queue, and the value of a boolean predicate that could depend on input tokens and state variables.
 * 
 * @see PortCondition
 * @see PredicateCondition
 * 
 * @author Jorn W. Janneck
 *
 */

abstract public class Condition extends AbstractIRNode {
	
	public enum ConditionKind { input, output, predicate }
	
	abstract public ConditionKind  kind();
	
	public Condition(Condition original) {
		super(original);
	}

	private int knowledgePriority;

	public int getKnowledgePriority() {
		return knowledgePriority;
	}

	public void setKnowledgePriority(int knowledgePriority) {
		this.knowledgePriority = knowledgePriority;
	}
}
