package net.opendf.ir.am;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.common.Port;
import net.opendf.ir.common.Statement;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

/**
 * Objects of this class contain the information necessary to execute the code
 * that needs to be run during a {@link ICall call instruction}. This
 * information includes the number of input tokens consumed and output tokens
 * produced at each port during the transition, a list of scopes, and the code.
 * 
 * The list of scopes need to be initialized before the transition, and will be
 * killed after the transition. Note that a {@link PredicateCondition predicate
 * condition} may have already initialized the scope, which makes
 * re-initialization unnecessary.
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

	public ImmutableList<Integer> getScopes() {
		return scopes;
	}

	public Statement getBody() {
		return body;
	}

	public Transition(Map<Port, Integer> inputRates, Map<Port, Integer> outputRates, ImmutableList<Integer> scopes,
			Statement body) {
		this(null, inputRates, outputRates, scopes, body);
	}

	private Transition(Transition original, Map<Port, Integer> inputRates, Map<Port, Integer> outputRates,
			ImmutableList<Integer> scopes, Statement body) {
		super(original);
		this.inputRates = Collections.unmodifiableMap(new HashMap<>(inputRates));
		this.outputRates = Collections.unmodifiableMap(new HashMap<>(outputRates));
		this.scopes = ImmutableList.copyOf(scopes);
		this.body = body;
	}

	public Transition copy(Map<Port, Integer> inputRates, Map<Port, Integer> outputRates,
			ImmutableList<Integer> scopes, Statement body) {
		if (Objects.equals(this.inputRates, inputRates) && Objects.equals(this.outputRates, outputRates)
				&& Lists.equals(this.scopes, scopes) && Objects.equals(this.body, body)) {
			return this;
		}
		return new Transition(this, inputRates, outputRates, scopes, body);
	}

	private ImmutableList<Integer> scopes;
	private Statement body;
	private Map<Port, Integer> inputRates;
	private Map<Port, Integer> outputRates;
}
