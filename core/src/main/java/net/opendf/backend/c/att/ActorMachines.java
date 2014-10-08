package net.opendf.backend.c.att;

import java.io.PrintWriter;

import javarag.Inherited;
import javarag.Module;
import javarag.Procedural;
import net.opendf.ir.IRNode;
import net.opendf.ir.entity.am.ActorMachine;
import net.opendf.ir.net.Node;

public class ActorMachines extends Module<ActorMachines.Decls> {

	public interface Decls {
		@Inherited
		ActorMachine actorMachine(IRNode node);

		@Procedural
		void translateNode(ActorMachine actorMachine, PrintWriter writer);

		Node node(ActorMachine actorMachine);

		int index(Object node);

		void scopes(ActorMachine actorMachine, PrintWriter writer);

		void transitions(ActorMachine actorMachine, PrintWriter writer);

		void controller(ActorMachine actorMachine, PrintWriter writer);

		void functionDeclarations(ActorMachine actorMachine, PrintWriter writer);

		void functionDefinitions(ActorMachine actorMachine, PrintWriter writer);

	}

	public ActorMachine actorMachine(ActorMachine am) {
		return am;
	}

	public void translateNode(ActorMachine actorMachine, PrintWriter writer) {
		Node node = e().node(actorMachine);
		writer.println("// ACTOR MACHINE");
		writer.println("// " + node.getName());
		e().functionDeclarations(actorMachine, writer);
		e().scopes(actorMachine, writer);
		e().functionDefinitions(actorMachine, writer);
		e().transitions(actorMachine, writer);
		e().controller(actorMachine, writer);
	}

}
