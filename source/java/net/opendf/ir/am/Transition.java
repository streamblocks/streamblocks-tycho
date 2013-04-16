package net.opendf.ir.am;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.common.Port;
import net.opendf.ir.common.Statement;
import net.opendf.ir.util.ImmutableList;

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

	public Map<Port, Integer> getInputRates() {
		return inputRates;
	}

	public int getInputRate(Port p) {
		if (inputRates.containsKey(p)) {
			return inputRates.get(p);
		} else {
			return 0;
		}
	}

	public Map<Port, Integer> getOutputRates() {
		return outputRates;
	}

	public int getOutputRate(Port p) {
		if (outputRates.containsKey(p)) {
			return outputRates.get(p);
		} else {
			return 0;
		}
	}

	public ImmutableList<Integer> getRequiredVars() {
		return required;
	}

	public ImmutableList<Integer> getKilledVars() {
		return killed;
	}

	public Statement getBody() {
		return body;
	}

	public Transition(Map<Port, Integer> inputRates, Map<Port, Integer> outputRates, ImmutableList<Integer> required,
			ImmutableList<Integer> killed, Statement body) {
		this.inputRates = Collections.unmodifiableMap(new HashMap<>(inputRates));
		this.outputRates = Collections.unmodifiableMap(new HashMap<>(outputRates));
		this.required = ImmutableList.copyOf(required);
		this.killed = ImmutableList.copyOf(killed);
		this.body = body;
	}

	private ImmutableList<Integer> required;
	private ImmutableList<Integer> killed;
	private Statement body;
	private Map<Port, Integer> inputRates;
	private Map<Port, Integer> outputRates;
}
