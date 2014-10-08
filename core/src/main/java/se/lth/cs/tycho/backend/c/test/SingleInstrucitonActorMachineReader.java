package se.lth.cs.tycho.backend.c.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Random;

import se.lth.cs.tycho.errorhandling.ErrorModule;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.cal.Actor;
import se.lth.cs.tycho.parser.lth.CalParser;
import se.lth.cs.tycho.transform.caltoam.ActorStates;
import se.lth.cs.tycho.transform.caltoam.ActorToActorMachine;
import se.lth.cs.tycho.transform.filter.PrioritizeCallInstructions;
import se.lth.cs.tycho.transform.filter.SelectFirstInstruction;
import se.lth.cs.tycho.transform.filter.SelectRandomInstruction;
import se.lth.cs.tycho.transform.operators.ActorOpTransformer;
import se.lth.cs.tycho.transform.outcond.OutputConditionAdder;
import se.lth.cs.tycho.transform.reduction.FixedInstructionWeight;
import se.lth.cs.tycho.transform.reduction.PriorityListSelector;
import se.lth.cs.tycho.transform.reduction.SelectMinimumInteger;
import se.lth.cs.tycho.transform.reduction.ShortestPathStateHandler;
import se.lth.cs.tycho.transform.reduction.TransitionPriorityStateHandler;
import se.lth.cs.tycho.transform.siam.PickFirstInstruction;
import se.lth.cs.tycho.transform.util.StateHandler;

public class SingleInstrucitonActorMachineReader implements NodeReader {
	private final Path basePath;
	private final ActorToActorMachine translator;

	public SingleInstrucitonActorMachineReader(Path basePath) {
		this.basePath = basePath;
		this.translator = new ActorToActorMachine() {
			@Override
			protected StateHandler<ActorStates.State> getStateHandler(StateHandler<ActorStates.State> stateHandler) {
				//int[] prio = new int[0];
				//try {
				//	prio = PriorityListSelector.readIntsFromFile(new File("parseheaders/prio.txt"));
				//} catch (FileNotFoundException e) {
				//	e.printStackTrace();
				//}
				stateHandler = new PrioritizeCallInstructions<>(stateHandler);
				stateHandler = new SelectFirstInstruction<>(stateHandler);
				//stateHandler = new TransitionPriorityStateHandler<>(stateHandler, new SelectMinimumInteger());
				//stateHandler = new TransitionPriorityStateHandler<>(stateHandler, new PriorityListSelector(prio));
				//stateHandler = new ShortestPathStateHandler<>(new FixedInstructionWeight<ActorStates.State>(1, 1, 1), stateHandler);
				//stateHandler = new SelectRandomInstruction<>(stateHandler);
				return stateHandler;
			}
		};
	}

	@Override
	public ActorMachine fromId(String id) {
		Path fileName = basePath.resolve(id.replace('.', '/') + ".cal");
		File file = fileName.toFile();
		return fromFile(file);
	}

	@Override
	public ActorMachine fromFile(File file) {
		System.out.println(file.getName());
		CalParser parser = new CalParser();
		Actor actor = (Actor) parser.parse(file, null, null).getEntity();
		ErrorModule errors = parser.getErrorModule();
		if (errors.hasError()) {
			errors.printErrors();
			return null;
		}
		actor = ActorOpTransformer.transformActor(actor, null);
		ActorMachine actorMachine = translator.translate(actor);
		System.out.println("States: " + actorMachine.getController().size());
		actorMachine = OutputConditionAdder.addOutputConditions(actorMachine);
		actorMachine = PickFirstInstruction.transform(actorMachine);
		return actorMachine;
	}

}
