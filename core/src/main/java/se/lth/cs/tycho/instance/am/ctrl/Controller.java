package se.lth.cs.tycho.instance.am.ctrl;

import java.util.Collection;
import java.util.List;

public interface Controller {
	State getInitialState();
	List<State> getAllStates();
}
