package se.lth.cs.tycho.transform.reduction;

import se.lth.cs.tycho.instance.Instance;
import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.State;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.transform.Transformation;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.ControllerGenerator;

public class ActorMachineReducer implements Transformation<Node> {
	
	private final StateFactory<?> factory;
	
	public ActorMachineReducer(StateFactory<?> stateFactory) {
		this.factory = stateFactory;
	}

	@Override
	public Node apply(Node node) {
		Instance instance = node.getContent();
		if (instance instanceof ActorMachine) {
			String name = node.getName();
			ActorMachine actorMachine = (ActorMachine) instance;
			ImmutableList<ToolAttribute> toolAttributes = node.getToolAttributes();
			return node.copy(
					name, 
					actorMachine.copy(
							actorMachine.getInputPorts(),
							actorMachine.getOutputPorts(),
							actorMachine.getScopes(),
							generateController(name, actorMachine, toolAttributes),
							actorMachine.getTransitions(),
							actorMachine.getConditions()),
					toolAttributes);
		} else {
			return node;
		}
	}
	
	private ImmutableList<State> generateController(String name, ActorMachine actorMachine, ImmutableList<ToolAttribute> toolAttributes) {
		Controller<?> state = factory.newState(name, actorMachine, toolAttributes);
		return ControllerGenerator.generate(state).getController();
	}

	public interface StateFactory<S> {
		public Controller<S> newState(String name, ActorMachine actorMachine,
				ImmutableList<ToolAttribute> toolAttributes);
	}

}
