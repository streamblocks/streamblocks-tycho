package net.opendf.ir.am;

import java.util.List;
import java.util.Map;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.common.PortName;
import net.opendf.ir.common.Statement;

/**
 * Objects of this class contain the information necessary to execute the code
 * that needs to be run during a {@link ICall call instruction}. This
 * information includes the number of input tokens consumed and output tokens
 * produced at each port during the transition, a list of required actor machine
 * variables, a list of actor machine variables that are invalidated (killed) by
 * this transition, and the code.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 * 
 */

public class Transition extends AbstractIRNode {

	public Map<PortName, Integer> getInputRates() {
		return inputRates;
	}

	public int getInputRate(PortName p) {
		if (inputRates.containsKey(p)) {
			return inputRates.get(p);
		} else {
			return 0;
		}
	}

	public Map<PortName, Integer> getOutputRates() {
		return outputRates;
	}

	public int getOutputRate(PortName p) {
		if (outputRates.containsKey(p)) {
			return outputRates.get(p);
		} else {
			return 0;
		}
	}

	public List<Integer> getRequiredVars() {
		return required;
	}

	public List<Integer> getKilledVars() {
		return killed;
	}

	public Statement[] getBody() {
		return body;
	}

	public Transition(Map<PortName, Integer> inputRates, Map<PortName, Integer> outputRates, List<Integer> required,
			List<Integer> killed, Statement[] body) {
		this.inputRates = inputRates;
		this.outputRates = outputRates;
		this.required = required;
		this.killed = killed;
		this.body = body;
	}

	private List<Integer> required;
	private List<Integer> killed;
	private Statement[] body;
	private Map<PortName, Integer> inputRates;
	private Map<PortName, Integer> outputRates;
}
