package se.lth.cs.tycho.ir.entity.am;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.Annotation;
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
 * @author Jorn W. Janneck
 */

public class Transition extends AbstractIRNode {

    public List<Annotation> getAnnotations() {return  annotations;}

    public Map<Port, Integer> getInputRates() {
        return inputRates;
    }

    public int getInputRate(Port p) {
        return inputRates.getOrDefault(p, 0);
    }

    public Map<Port, Integer> getOutputRates() {
        return outputRates;
    }

    public int getOutputRate(Port p) {
        return outputRates.getOrDefault(p, 0);
    }

    public ImmutableList<Integer> getScopesToKill() {
        return kill;
    }

    public ImmutableList<Statement> getBody() {
        return body;
    }

    public Transition(List<Annotation> annotations, Map<Port, Integer> inputRates, Map<Port, Integer> outputRates, List<Integer> kill,
                      List<Statement> body) {
        this(null, annotations, inputRates, outputRates, kill, body);
    }

    private Transition(Transition original, List<Annotation> annotations, Map<Port, Integer> inputRates, Map<Port, Integer> outputRates,
                       List<Integer> kill, List<Statement> body) {
        super(original);
        this.annotations = ImmutableList.from(annotations);
        this.inputRates = Collections.unmodifiableMap(new HashMap<>(inputRates));
        this.outputRates = Collections.unmodifiableMap(new HashMap<>(outputRates));
        this.kill = ImmutableList.from(kill);
        this.body = ImmutableList.from(body);
    }

    public Transition copy(List<Annotation> annotations, Map<Port, Integer> inputRates, Map<Port, Integer> outputRates,
                           List<Integer> kill, List<Statement> body) {
        if (Objects.equals(this.annotations, annotations) && Objects.equals(this.inputRates, inputRates) && Objects.equals(this.outputRates, outputRates)
                && Lists.equals(this.kill, kill) && Lists.sameElements(this.body, body)) {
            return this;
        }
        return new Transition(this, annotations, inputRates, outputRates, kill, body);
    }

    private final ImmutableList<Integer> kill;
    private final ImmutableList<Statement> body;
    private final ImmutableList<Annotation> annotations;
    private final Map<Port, Integer> inputRates;
    private final Map<Port, Integer> outputRates;

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        annotations.forEach(action);
        inputRates.keySet().forEach(action);
        outputRates.keySet().forEach(action);
        body.forEach(action);
    }

    @Override
    public Transition transformChildren(Transformation transformation) {
        return copy(
                transformation.mapChecked(Annotation.class, annotations),
                transformRates(inputRates, transformation),
                transformRates(outputRates, transformation),
                kill,
                transformation.mapChecked(Statement.class, body));
    }

    private Map<Port, Integer> transformRates(Map<Port, Integer> rates, Transformation transformation) {
        Map<Port, Integer> result = new HashMap<>();
        for (Port p : rates.keySet()) {
            result.put(transformation.applyChecked(Port.class, p), rates.get(p));
        }
        return result;
    }

    public Transition withBody(List<Statement> body) {
        return copy(annotations, inputRates, outputRates, kill, body);
    }
}
