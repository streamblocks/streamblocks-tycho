package se.lth.cs.tycho.transformation.regexp;

import java.util.HashSet;
import java.util.Set;


import org.jgrapht.graph.DefaultDirectedGraph;
import se.lth.cs.tycho.ir.QID;

public class Automaton extends DefaultDirectedGraph<Integer, SimpleEdge> {

    private static final long serialVersionUID = 1L;

    private Set<QID> alphabet;

    private Set<Integer> finalStates;

    private Integer initialState;

    public Automaton(Class<? extends SimpleEdge> edgeClass) {
        super(edgeClass);
        initialState = -1;
        finalStates = new HashSet<Integer>();
        alphabet = new HashSet<QID>();
    }

    public boolean addFinalState(Integer s) {
        return finalStates.add(s);
    }

    public Set<QID> getAlphabet() {
        return alphabet;
    }

    public Set<Integer> getFinalStates() {
        return finalStates;
    }

    public Integer getInitialState() {
        return initialState;
    }

    public boolean registerLetter(QID letter) {
        return alphabet.add(letter);
    }

    public void setAlphabet(Set<QID> newAlphabet) {
        alphabet = newAlphabet;
    }

    public void setInitialState(Integer s) {
        initialState = s;
    }

}