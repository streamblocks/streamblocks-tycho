package se.lth.cs.tycho.ir.entity.am;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

/**
 * Objects of this class contain the information necessary to execute the code
 * that needs to be run during a {@link ICall call instruction}. This
 * information includes the number of input tokens consumed and output tokens
 * produced at each port during the transition, the code to be executed and a
 * set of scopes that should be killed after the transition.
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

	public ImmutableList<Integer> getScopesToKill() {
		return kill;
	}

	public Statement getBody() {
		return body;
	}

	public Transition(Map<Port, Integer> inputRates, Map<Port, Integer> outputRates, ImmutableList<Integer> kill,
			Statement body) {
		this(null, inputRates, outputRates, kill, body);
	}

	private Transition(Transition original, Map<Port, Integer> inputRates, Map<Port, Integer> outputRates,
			ImmutableList<Integer> kill, Statement body) {
		super(original);
		this.inputRates = Collections.unmodifiableMap(new HashMap<>(inputRates));
		this.outputRates = Collections.unmodifiableMap(new HashMap<>(outputRates));
		this.kill = ImmutableList.copyOf(kill);
		this.body = body;
	}

	public Transition copy(Map<Port, Integer> inputRates, Map<Port, Integer> outputRates,
			ImmutableList<Integer> kill, Statement body) {
		if (Objects.equals(this.inputRates, inputRates) && Objects.equals(this.outputRates, outputRates)
				&& Lists.equals(this.kill, kill) && Objects.equals(this.body, body)) {
			return this;
		}
		return new Transition(this, inputRates, outputRates, kill, body);
	}

	private ImmutableList<Integer> kill;
	private Statement body;
	private Map<Port, Integer> inputRates;
	private Map<Port, Integer> outputRates;
}
