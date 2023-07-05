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

	// When considering a MultiInstructionState and making use of the OrderedConditionChecking (OCC) reducer, a unique
	// order number is assigned to each Test instruction. The OCC transforms the MultiInstructionState to a
	// SingleInstructionState by selecting the Test instruction with the lowest order number. The order number assigned
	// to the Test instruction is based on the order number of this condition.
	private int orderNumber = -1;

	public int getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}

	public int compareTo(Condition other){
		if(this.getOrderNumber() == other.getOrderNumber()){
			return 0;
		}else if(this.getOrderNumber() < other.getOrderNumber()){
			return -1;
		}else{
			return 1;
		}
	}
}
