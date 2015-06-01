package se.lth.cs.tycho.instance.am.ctrl;

import java.util.List;

public interface State {
	List<Transition> getTransitions();
}
