/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.lth.cs.tycho.transform.util;

import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.Transition;
import se.lth.cs.tycho.ir.QID;

/**
 * 
 * @author gustav
 * @param <S>
 */
public abstract class FilteredController<S> implements Controller<S> {
    
    protected final Controller<S> original;
    
    protected FilteredController(Controller<S> original) {
        this.original = original;
    }

    @Override
    public S initialState() {
        return original.initialState();
    }

    @Override
    public QID instanceId() {
        return original.instanceId();
    }

    @Override
    public Condition getCondition(int c) {
        return original.getCondition(c);
    }

    @Override
    public Transition getTransition(int t) {
        return original.getTransition(t);
    }
    
}
