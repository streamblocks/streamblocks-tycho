package net.opendf.backend.c.att;

import java.io.PrintWriter;

import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Transition;
import net.opendf.ir.common.stmt.Statement;
import net.opendf.ir.net.Node;

public class Transitions extends Module<Transitions.Decls> {

	public interface Decls {

		@Synthesized
		public void transitions(ActorMachine actorMachine, PrintWriter writer);

		@Synthesized
		void transition(Transition t, PrintWriter writer);

		ActorMachine actorMachine(Transition trans);

		Node node(ActorMachine actorMachine);

		int index(Object node);

		String blockified(Statement body);

	}

	public void transitions(ActorMachine actorMachine, PrintWriter writer) {
		for (Transition t : actorMachine.getTransitions()) {
			e().transition(t, writer);
		}
	}

	public void transition(Transition trans, PrintWriter writer) {
		int node = e().index(e().node(e().actorMachine(trans)));
		int index = e().index(trans);
		writer.print("static void transition_n" + node + "t" + index + "(void) ");
		writer.print(e().blockified(trans.getBody()));
	}

}
