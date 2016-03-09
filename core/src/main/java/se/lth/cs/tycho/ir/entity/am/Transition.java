package se.lth.cs.tycho.ir.entity.am;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Objects of this class contain the information necessary to execute the code
 * that needs to be run during a call instruction. This
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

	public ImmutableList<Statement> getBody() {
		return body;
	}

	public Transition(Map<Port, Integer> inputRates, Map<Port, Integer> outputRates, List<Integer> kill,
			List<Statement> body) {
		this(null, inputRates, outputRates, kill, body);
	}

	private Transition(Transition original, Map<Port, Integer> inputRates, Map<Port, Integer> outputRates,
			List<Integer> kill, List<Statement> body) {
		super(original);
		this.inputRates = Collections.unmodifiableMap(new HashMap<>(inputRates));
		this.outputRates = Collections.unmodifiableMap(new HashMap<>(outputRates));
		this.kill = ImmutableList.from(kill);
		this.body = ImmutableList.from(body);
	}

	public Transition copy(Map<Port, Integer> inputRates, Map<Port, Integer> outputRates,
			List<Integer> kill, List<Statement> body) {
		if (Objects.equals(this.inputRates, inputRates) && Objects.equals(this.outputRates, outputRates)
				&& Lists.equals(this.kill, kill) && Lists.sameElements(this.body, body)) {
			return this;
		}
		return new Transition(this, inputRates, outputRates, kill, body);
	}

	private final ImmutableList<Integer> kill;
	private final ImmutableList<Statement> body;
	private final Map<Port, Integer> inputRates; // TODO compute
	private final Map<Port, Integer> outputRates; // TODO compute

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		body.forEach(action);
	}

	@Override
	public Transition transformChildren(Transformation transformation) {
		return copy(inputRates, outputRates, kill, transformation.mapChecked(Statement.class, body));
	}

	public Transition withBody(List<Statement> body) {
		return copy(inputRates, outputRates, kill, body);
	}
}
