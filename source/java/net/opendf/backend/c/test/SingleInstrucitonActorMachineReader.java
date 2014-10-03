package net.opendf.backend.c.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Random;

import net.opendf.errorhandling.ErrorModule;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.cal.Actor;
import net.opendf.parser.lth.CalParser;
import net.opendf.transform.caltoam.ActorStates;
import net.opendf.transform.caltoam.ActorToActorMachine;
import net.opendf.transform.filter.PrioritizeCallInstructions;
import net.opendf.transform.filter.SelectFirstInstruction;
import net.opendf.transform.filter.SelectRandomInstruction;
import net.opendf.transform.operators.ActorOpTransformer;
import net.opendf.transform.outcond.OutputConditionAdder;
import net.opendf.transform.reduction.FixedInstructionWeight;
import net.opendf.transform.reduction.PriorityListSelector;
import net.opendf.transform.reduction.SelectMinimumInteger;
import net.opendf.transform.reduction.ShortestPathStateHandler;
import net.opendf.transform.reduction.TransitionPriorityStateHandler;
import net.opendf.transform.siam.PickFirstInstruction;
import net.opendf.transform.util.StateHandler;

public class SingleInstrucitonActorMachineReader implements NodeReader {
	private final Path basePath;
	private final ActorToActorMachine translator;

	public SingleInstrucitonActorMachineReader(Path basePath) {
		this.basePath = basePath;
		this.translator = new ActorToActorMachine() {
			@Override
			protected StateHandler<ActorStates.State> getStateHandler(StateHandler<ActorStates.State> stateHandler) {
				int[] prio = new int[0];
				try {
					prio = PriorityListSelector.readIntsFromFile(new File("parseheaders/prio.txt"));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
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
		Actor actor = parser.parse(file, null, null);
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
