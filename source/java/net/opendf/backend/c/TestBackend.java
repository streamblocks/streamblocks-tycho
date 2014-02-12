package net.opendf.backend.c;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import net.opendf.errorhandling.ErrorModule;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.net.Network;
import net.opendf.ir.util.ImmutableList;
import net.opendf.parser.lth.CalParser;
import net.opendf.transform.caltoam.ActorStates;
import net.opendf.transform.caltoam.ActorToActorMachine;
import net.opendf.transform.filter.PrioritizeCallInstructions;
import net.opendf.transform.filter.SelectRandomInstruction;
import net.opendf.transform.operators.ActorOpTransformer;
import net.opendf.transform.outcond.OutputConditionAdder;


public class TestBackend {
	private static ActorToActorMachine translator = new ActorToActorMachine(ImmutableList.of(
			PrioritizeCallInstructions.<ActorStates.State> getFactory(),
			SelectRandomInstruction.<ActorStates.State> getFactory()));

	private static Network testNetwork(File file) {
		CalParser parser = new CalParser();
		Actor actor = parser.parse(file, null, null);
		ErrorModule errors = parser.getErrorModule();
		if (errors.hasError()) {
			errors.printErrors();
			return null;
		}
		actor = ActorOpTransformer.transformActor(actor, null);
		ActorMachine actorMachine = translator.translate(actor);
		actorMachine = OutputConditionAdder.addOutputConditions(actorMachine);
		String fileName = file.getName();
		String name = fileName.substring(0, fileName.length() - 4);
		return NetworkFunctions.fromActorMachine(actorMachine, name);
	}

	public static void main(String[] args) throws IOException {
		for (String arg : args) {
			File inFile = new File(arg);
			File outFile = new File(inFile.getName() + ".c");
			try {
				Network network = testNetwork(inFile);
				PrintWriter out = new PrintWriter(new java.io.FileWriter(outFile), true);
				Backend.generateCode(network, out);
			} catch (Throwable e) {
				System.err.println("ERROR IN " + inFile.getName() + ":");
				e.printStackTrace();
				//System.err.println(e.getMessage());
				//System.err.println();
			}
			System.out.println("GENERATED FILE " + outFile.getPath());
		}
	}

}
