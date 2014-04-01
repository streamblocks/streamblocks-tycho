package net.opendf.transform.siam;

import net.opendf.ir.am.ActorMachine;
import net.opendf.transform.util.ControllerGenerator;

public class PickFirstInstruction {
	
	public static ActorMachine transform(ActorMachine am) {
		ControllerGenerator<Integer> result = ControllerGenerator.generate(new StateReducerPickFirst(am));
		return am.copy(am.getInputPorts(), am.getOutputPorts(), am.getScopes(), result.getController(), am.getTransitions(), am.getConditions());
	}

}
