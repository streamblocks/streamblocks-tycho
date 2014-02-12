package net.opendf.backend.c.att;

import java.io.PrintWriter;

import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.net.Node;

public class ActorMachines extends Module<ActorMachines.Required>{

	public interface Required {

		Node node(ActorMachine actorMachine);

		int index(Object node);

		void scopes(ActorMachine actorMachine, PrintWriter writer);

		void transitions(ActorMachine actorMachine, PrintWriter writer);

		void controller(ActorMachine actorMachine, PrintWriter writer);

		void functionDeclarations(ActorMachine actorMachine, PrintWriter writer);

		void functionDefinitions(ActorMachine actorMachine, PrintWriter writer);

	}
	
	@Inherited
	public ActorMachine actorMachine(ActorMachine am) {
		return am;
	}
	
	@Synthesized
	public void translateNode(ActorMachine actorMachine, PrintWriter writer) {
		Node node = get().node(actorMachine);
		writer.println("// ACTOR MACHINE");
		writer.println("// " + node.getName());
		get().functionDeclarations(actorMachine, writer);
		get().scopes(actorMachine, writer);
		get().functionDefinitions(actorMachine, writer);
		get().transitions(actorMachine, writer);
		get().controller(actorMachine, writer);
	}
	
}
