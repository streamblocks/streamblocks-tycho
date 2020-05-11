package se.lth.cs.tycho.interp;

import se.lth.cs.tycho.interp.values.Ref;
import se.lth.cs.tycho.interp.values.RefView;

public interface Stack {
    Ref pop();

    void remove(int n);

    void push(RefView r);

    Ref push();

    void alloca(int n);

    /**
     * @param i , 0 = top of stack
     */
    Ref peek(int i);

    Ref closure(int select);

    boolean isEmpty();
}
