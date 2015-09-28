package se.lth.cs.tycho.cfg;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CfgToDot  {
	private final PrintWriter writer;
	private final Set<Block> visited;
	private final Map<Block, Integer> numbers;
	private final SuccessorWriter successorWriter;

	private CfgToDot(PrintWriter writer) {
		this.writer = writer;
		this.visited = new HashSet<>();
		this.numbers = new HashMap<>();
		this.successorWriter = new SuccessorWriter();
	}

	private int number(Block block) {
		if (numbers.containsKey(block)) {
			return numbers.get(block);
		} else {
			int number = numbers.size();
			numbers.put(block, number);
			return number;
		}
	}

	public static void write(Path file, Block entry) {
		try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(file))) {
			new CfgToDot(pw).write(entry);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	private void write(Block entry) {
		writer.println("digraph {");
		writeBlock(entry);
		writer.println("}");
	}

	private void writeBlock(Block b) {
		if (visited.add(b)) {
			b.accept(successorWriter);
			b.forEachSuccessor(this::writeBlock);
		}
	}

	private class SuccessorWriter implements BlockVisitor<Void, Void> {
		@Override
		public Void visitActionBlock(ActionBlock b, Void p) {
			writer.print('\t');
			writer.print(number(b));
			writer.print(" -> ");
			writer.println(number(b.getSuccessor()));
			return null;
		}

		@Override
		public Void visitConditionBlock(ConditionBlock b, Void p) {
			writer.print('\t');
			writer.print(number(b));
			writer.print(" -> { ");
			writer.print(number(b.getSuccessorIfTrue()));
			writer.print(' ');
			writer.println(number(b.getSuccessorIfFalse()));
			writer.println(" }");
			return null;
		}
	}
}
