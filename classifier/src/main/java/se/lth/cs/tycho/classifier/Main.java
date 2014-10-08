package se.lth.cs.tycho.classifier;

import java.io.File;

import net.opendf.errorhandling.ErrorModule;
import net.opendf.ir.entity.am.ActorMachine;
import net.opendf.ir.entity.cal.Actor;
import net.opendf.parser.lth.CalParser;
import net.opendf.transform.caltoam.ActorStates;
import net.opendf.transform.caltoam.ActorToActorMachine;
import net.opendf.transform.filter.PrioritizeCallInstructions;
import net.opendf.transform.util.StateHandler;

public class Main {
	
	private final ActorToActorMachine actorToActorMachine;
	
	public Main() {
		actorToActorMachine = new ActorToActorMachine() {
			@Override
			protected StateHandler<ActorStates.State> getStateHandler(StateHandler<ActorStates.State> stateHandler) {
				return new PrioritizeCallInstructions<>(stateHandler);
			}
		};
	}
	
	public static void main(String[] args) throws InterruptedException {
		if (args.length == 0) {
			printUsage();
			System.exit(1);
		}
		Main m = new Main();
		for (String arg : args) {
			m.analyze(arg);
		}
	}

	private void analyze(String arg) {
		CalParser parser = new CalParser();
		File file = new File(arg);
		String name = file.getName();
		System.out.println(name);
		System.out.println(name.replaceAll(".", "="));
		Actor actor = (Actor) parser.parse(file, null, null).getEntity();
		ActorMachine actorMachine = actorToActorMachine.translate(actor);
		ErrorModule errors = parser.getErrorModule();
		if (!errors.hasError()) {
			Classifier classifier = Classifier.getInstance(actorMachine);
			for (String c : classifier.getClasses()) {
				long t = System.nanoTime();
				System.out.println(c + ": " + classifier.isOfClass(c) + " [" + (System.nanoTime()-t)/1000000 + " ms]");
			}
			System.out.println();
		} else {
			errors.printErrors();
			System.out.println();
		}
	}

	private static void printUsage() {
		System.out.println("java " + Main.class.getName() + " <file>...");
	}

}
