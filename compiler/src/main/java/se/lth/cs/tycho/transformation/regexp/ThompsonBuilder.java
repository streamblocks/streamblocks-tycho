package se.lth.cs.tycho.transformation.regexp;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.cal.regexp.RegExp;
import se.lth.cs.tycho.ir.entity.cal.regexp.RegExpBinary;
import se.lth.cs.tycho.ir.entity.cal.regexp.RegExpTag;
import se.lth.cs.tycho.ir.entity.cal.regexp.RegExpUnary;
import se.lth.cs.tycho.util.Box;

import java.util.Stack;

@Module
public interface ThompsonBuilder {


    @Binding(BindingKind.LAZY)
    default Automaton eNFA() {
        return new Automaton(SimpleEdge.class);
    }

    @Binding(BindingKind.LAZY)
    default Box<Integer> index() {
        return Box.of(0);
    }

    @Binding(BindingKind.LAZY)
    default Stack<Integer> recursionStack() {
        return new Stack<>();
    }

    default Automaton nfa(RegExp regExp) {
        visit(regExp);
        return eNFA();
    }

    void visit(RegExp regExp);

    default void visit(RegExpTag tag) {
        int initialCurrent = index().get();
        int finalCurrent = index().get() + 1;
        index().set(index().get() + 2);

        eNFA().addVertex(initialCurrent);
        eNFA().addVertex(finalCurrent);
        QID label = tag.getQID();
        eNFA().registerLetter(label);
        eNFA().addEdge(initialCurrent, finalCurrent, new SimpleEdge(label));

        recursionStack().push(finalCurrent);
        recursionStack().push(initialCurrent);
    }

    default void visit(RegExpUnary unary) {
        if (unary.getOperation().equals("*")) {
            caseStar(unary);
        } else {
            caseZeroOrOne(unary);
        }
    }


    default void caseStar(RegExpUnary regexp) {
        int initialCurrent = index().get();
        index().set(index().get() + 1);

        eNFA().addVertex(initialCurrent);

        visit(regexp.getOperand());

        int initialChild = recursionStack().pop();
        int finalChild = recursionStack().pop();

        int finalCurrent = index().get();
        ;
        index().set(index().get() + 1);
        eNFA().addVertex(finalCurrent);
        // epsilon transition for 0 occurrences.
        eNFA().addEdge(initialCurrent, finalCurrent, new SimpleEdge(null));

        // entering child regexp
        eNFA().addEdge(initialCurrent, initialChild, new SimpleEdge(null));
        // returning to the beginning of the child regexp
        eNFA().addEdge(finalChild, initialChild, new SimpleEdge(null));
        // exiting child regexp
        eNFA().addEdge(finalChild, finalCurrent, new SimpleEdge(null));

        recursionStack().push(finalCurrent);
        recursionStack().push(initialCurrent);
    }

    default void caseZeroOrOne(RegExpUnary regexp) {
        // Match 0 or 1 element
        // Similar to Kleene Star but with no loop back.

        int initialCurrent = index().get();
        index().set(index().get() + 1);
        eNFA().addVertex(initialCurrent);

        visit(regexp.getOperand());
        int initialChild = recursionStack().pop();
        int finalChild = recursionStack().pop();

        int finalCurrent = index().get();
        index().set(index().get() + 1);
        eNFA().addVertex(finalCurrent);
        // epsilon transition for 0 occurrences.
        eNFA().addEdge(initialCurrent, finalCurrent, new SimpleEdge(null));

        // entering child regexp
        eNFA().addEdge(initialCurrent, initialChild, new SimpleEdge(null));
        // exiting child regexp
        eNFA().addEdge(finalChild, finalCurrent, new SimpleEdge(null));

        recursionStack().push(finalCurrent);
        recursionStack().push(initialCurrent);
    }


    default void visit(RegExpBinary binary) {
        if (binary.getOperations().get(0).equals("|")) {
            caseAlternation(binary);
        } else {
            caseConcatenation(binary);
        }
    }

    default void caseAlternation(RegExpBinary regexp) {
        int initialCurrent = index().get();
        index().set(index().get() + 1);
        eNFA().addVertex(initialCurrent);

        visit(regexp.getOperands().get(0));
        int initialLeft = recursionStack().pop();
        int finalLeft = recursionStack().pop();

        visit(regexp.getOperands().get(1));
        int initialRight = recursionStack().pop();
        int finalRight = recursionStack().pop();

        int finalCurrent = index().get();
        index().set(index().get() + 1);
        eNFA().addVertex(finalCurrent);

        eNFA().addEdge(initialCurrent, initialLeft, new SimpleEdge(null));
        eNFA().addEdge(initialCurrent, initialRight, new SimpleEdge(null));
        eNFA().addEdge(finalLeft, finalCurrent, new SimpleEdge(null));
        eNFA().addEdge(finalRight, finalCurrent, new SimpleEdge(null));

        recursionStack().push(finalCurrent);
        recursionStack().push(initialCurrent);
    }

    default void caseConcatenation(RegExpBinary regexp) {
        visit(regexp.getOperands().get(0));
        int initialLeft = recursionStack().pop();
        int finalLeft = recursionStack().pop();

        visit(regexp.getOperands().get(1));
        int initialRight = recursionStack().pop();
        int finalRight = recursionStack().pop();

        // Simply add an epsilon transition between the two sub automata
        eNFA().addEdge(finalLeft, initialRight, new SimpleEdge(null));
        recursionStack().push(finalRight);
        recursionStack().push(initialLeft);
    }

}


