package se.lth.cs.tycho.transformation.regexp;

import org.jgrapht.nio.dot.DOTExporter;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.cal.ScheduleFSM;
import se.lth.cs.tycho.ir.entity.cal.Transition;
import se.lth.cs.tycho.ir.entity.cal.regexp.RegExp;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class RegExpConverter {

    private Map<Integer, String> nameMap;

    private RegExp scheduleRegExp;

    public RegExpConverter(RegExp scheduleRegExp) {
        this.scheduleRegExp = scheduleRegExp;
        nameMap = new TreeMap<>();
    }

    public ScheduleFSM convert() {
        ThompsonBuilder builder = MultiJ.from(ThompsonBuilder.class).instance();
        Automaton eNFA = builder.nfa(scheduleRegExp);
        printAutomaton(new BufferedWriter(new PrintWriter(System.out)), eNFA);
        Automaton DFA = new eNFAtoDFA(eNFA).convert();

        ImmutableList.Builder<Transition> transitions = ImmutableList.builder();

        // Convert the states to String and add them to the FSM.
        Set<Integer> states = DFA.vertexSet();
        for (Integer state : states) {
            String fsmState = "S" + state;
            nameMap.put(state, fsmState);

        }
        // Add the transitions
        for (Integer source : states) {
            Set<SimpleEdge> edges = DFA.outgoingEdgesOf(source);

            for (SimpleEdge edge : edges) {
                Integer target = DFA.getEdgeTarget(edge);
                String sourceState = nameMap.get(source);
                String targetState = nameMap.get(target);

                QID qid = (QID) edge.getObject();
                Transition transition = new Transition(sourceState, targetState, ImmutableList.of(qid));
                transitions.add(transition);
            }

        }

        String initialState = nameMap.get(DFA.getInitialState());

        ScheduleFSM schedule = new ScheduleFSM(transitions.build(), initialState);
        return schedule;
    }

    private void printAutomaton(BufferedWriter out, Automaton automaton) {
        DOTExporter<Integer, SimpleEdge> exporter = new DOTExporter<>();
        exporter.exportGraph(automaton, out);
    }
}
