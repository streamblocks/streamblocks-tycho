package se.lth.cs.tycho.transform.siam;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.transform.util.ControllerGenerator;

public class PickFirstInstruction {
	
	public static ActorMachine transform(ActorMachine am, QID instanceId) {
		ControllerGenerator<Integer> result = ControllerGenerator.generate(new StateReducerPickFirst(am, instanceId));
		return am.copy(am.getInputPorts(), am.getOutputPorts(), am.getScopes(), result.getController(), am.getTransitions(), am.getConditions());
	}

}
