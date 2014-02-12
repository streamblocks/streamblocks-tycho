package net.opendf.backend.c.att;

import java.io.PrintWriter;

import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Transition;
import net.opendf.ir.common.Statement;
import net.opendf.ir.net.Node;

public class Transitions extends Module<Transitions.Required> {

	public interface Required {

		void transition(Transition t, PrintWriter writer);

		ActorMachine actorMachine(Transition trans);

		Node node(ActorMachine actorMachine);

		int index(Object node);

		String statement(Statement body);

		String blockified(Statement body);

	}

	@Synthesized
	public void transitions(ActorMachine actorMachine, PrintWriter writer) {
		for (Transition t : actorMachine.getTransitions()) {
			get().transition(t, writer);
		}
	}
	
	@Synthesized
	public void transition(Transition trans, PrintWriter writer) {
		int node = get().index(get().node(get().actorMachine(trans)));
		int index = get().index(trans);
		writer.print("static void transition_n"+node+"t"+index+"(void) ");
		writer.print(get().blockified(trans.getBody()));
	}

}
