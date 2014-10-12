package se.lth.cs.tycho.backend.c.att;

import java.io.PrintWriter;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.Transition;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.stmt.Statement;
import javarag.Module;
import javarag.Procedural;
import javarag.Synthesized;

public class Transitions extends Module<Transitions.Decls> {

	public interface Decls {

		@Procedural
		public void transitions(ActorMachine actorMachine, PrintWriter writer);

		@Procedural
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
