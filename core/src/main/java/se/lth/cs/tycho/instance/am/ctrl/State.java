package se.lth.cs.tycho.instance.am.ctrl;

import java.util.Collection;

public interface State {
	Collection<Transition> getTransitions();
}
