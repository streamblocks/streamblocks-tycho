package net.opendf.backend.c.test;

import java.io.File;
import java.nio.file.Path;

import net.opendf.errorhandling.ErrorModule;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.util.ImmutableList;
import net.opendf.parser.lth.CalParser;
import net.opendf.transform.caltoam.ActorStates;
import net.opendf.transform.caltoam.ActorToActorMachine;
import net.opendf.transform.filter.PrioritizeCallInstructions;
import net.opendf.transform.filter.SelectFirstInstruction;
import net.opendf.transform.operators.ActorOpTransformer;
import net.opendf.transform.outcond.OutputConditionAdder;
import net.opendf.transform.siam.PickFirstInstruction;

public class SingleInstrucitonActorMachineReader implements NodeReader {
	private final Path basePath;
	private final ActorToActorMachine translator;

	public SingleInstrucitonActorMachineReader(Path basePath) {
		this.basePath = basePath;
		this.translator = new ActorToActorMachine(ImmutableList.of(
				PrioritizeCallInstructions.<ActorStates.State> getFactory(),
				SelectFirstInstruction.<ActorStates.State> getFactory()));
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
		actorMachine = OutputConditionAdder.addOutputConditions(actorMachine);
		actorMachine = PickFirstInstruction.transform(actorMachine);
		return actorMachine;
	}

}
