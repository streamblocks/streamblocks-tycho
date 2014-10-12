package se.lth.cs.tycho.classifier.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.Instruction;
import se.lth.cs.tycho.instance.am.PortCondition;
import se.lth.cs.tycho.instance.am.State;
import se.lth.cs.tycho.instance.am.Transition;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import javarag.TreeTraverser;

public class ControllerTraverser implements TreeTraverser<IRNode> {

	@Override
	public Iterable<? extends IRNode> getChildren(IRNode root) {
		if (root instanceof ActorMachine) {
			List<IRNode> children = new ArrayList<>();
			ActorMachine actorMachine = (ActorMachine) root;
			children.addAll(actorMachine.getController());
			children.addAll(actorMachine.getConditions());
			children.addAll(actorMachine.getTransitions());
			return children;
		}
		if (root instanceof State) {
			return ((State) root).getInstructions();
		}
		if (root instanceof PortCondition) {
			return Arrays.asList(((PortCondition) root).getPortName());
		}
		if (root instanceof Instruction || root instanceof Condition || root instanceof Transition || root instanceof Port) {
			return Collections.emptyList();
		}
		throw new Error("Missing definition for class " + root.getClass().getSimpleName());
	}

}
