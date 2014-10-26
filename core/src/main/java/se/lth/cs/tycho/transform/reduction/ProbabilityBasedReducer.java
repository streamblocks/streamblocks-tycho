package se.lth.cs.tycho.transform.reduction;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.messages.Message;
import se.lth.cs.tycho.messages.MessageReporter;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.GenInstruction;

public abstract class ProbabilityBasedReducer<S> implements Controller<S> {

	private final Controller<S> controller;
	private final Path file;
	private final MessageReporter msg;
	private Map<Integer, Double> probabilities = null;
	private boolean initialized = false;

	public ProbabilityBasedReducer(Controller<S> controller, Path dataPath, MessageReporter msg) {
		this.controller = controller;
		this.msg = msg;
		Path path = dataPath.resolve(Paths.get("", instanceId().getButLast()
				.parts()
				.stream()
				.map(QID::toString)
				.toArray(String[]::new)));
		this.file = path.resolve(instanceId().getLast().toString() + ".data");
	}

	protected double probability(int index) {
		if (!initialized) {
			init();
		}
		return probabilities.getOrDefault(index, defaultValue());
	}
	
	protected abstract double defaultValue();

	private void init() {
		initialized = true;
		try (BufferedReader reader = Files.newBufferedReader(file)) {
			probabilities = reader.lines()
					.map(this::parseLine)
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		} catch (IOException e) {
			msg.report(Message.error("Could not read input data for reducer: " + e.getMessage()));
		}
	}
	
	private Entry<Integer, Double> parseLine(String line) {
		try {
			String[] split = line.split("\\s+");
			Integer key = Integer.valueOf(split[0]);
			Double value = Double.valueOf(split[1]);
			if (value < 0.0 || value > 1.0) {
				msg.report(Message.error("Probability out of range in data file \"" + file + "\""));
			}
			return new AbstractMap.SimpleImmutableEntry<>(key, value);
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			msg.report(Message.error("Could not parse probability record \"" + line + "\" in file \"" + file + "\""));
			return null;
		}
	}

	@Override
	public List<GenInstruction<S>> instructions(S state) {
		return select(controller.instructions(state));
	}

	protected abstract List<GenInstruction<S>> select(List<GenInstruction<S>> instructions);

	@Override
	public S initialState() {
		return controller.initialState();
	}

	@Override
	public QID instanceId() {
		return controller.instanceId();
	}

}
