package se.lth.cs.tycho.transform.reduction.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import se.lth.cs.tycho.messages.Message;
import se.lth.cs.tycho.messages.util.Result;

public class ProbabilityTable {
	private final double[] probabilities;
	private final double defaultValue;
	
	private ProbabilityTable(double[] probabilities, double defaultValue) {
		this.probabilities = probabilities;
		this.defaultValue = defaultValue;
	}

	public static Result<ProbabilityTable> fromFile(Path file, double defaultValue) {
		try (BufferedReader reader = Files.newBufferedReader(file)) {
			Map<Integer, Double> probabilities = new HashMap<>();
			reader.lines().forEach(line -> parseLine(probabilities, line, file));
			int max = probabilities.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
			double[] table = new double[max+1];
			for (int i = 0; i <= max; i++) {
				table[i] = probabilities.getOrDefault(i, defaultValue);
			}
			return Result.success(new ProbabilityTable(table, defaultValue));
		} catch (IOException e) {
			return Result.failure(Message.error("Could not read probabilities: " + e.getMessage()));
		} catch (ProbabilityReaderException e) {
			return Result.failure(Message.error(e.getMessage()));
		}
	}
	
	public static ProbabilityTable empty(double defaultValue) {
		return new ProbabilityTable(new double[0], defaultValue);
	}
	
	public double probability(int index) {
		if (index >= probabilities.length) {
			return defaultValue;
		} else {
			return probabilities[index];
		}
	}

	private final static class ProbabilityReaderException extends RuntimeException {
		private static final long serialVersionUID = -136675827441424139L;

		public ProbabilityReaderException(String message) {
			super(message);
		}

	}

	private static void parseLine(Map<Integer, Double> map, String line, Path file) throws ProbabilityReaderException {
		try {
			String[] split = line.split("\\s+");
			Integer key = Integer.valueOf(split[0]);
			Double value = Double.valueOf(split[1]);
			if (key < 0) {
				throw new ProbabilityReaderException("Index out of bounds in data file \"" + file + "\"");
			}
			if (value < 0.0 || value > 1.0) {
				throw new ProbabilityReaderException("Probability out of range in data file \"" + file + "\"");
			}
			map.put(key, value);
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			throw new ProbabilityReaderException("Could not parse probability record \"" + line + "\" in file \"" + file
					+ "\"");
		}
	}

}
