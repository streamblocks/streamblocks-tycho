package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.ir.IRNode;

import java.util.function.Consumer;

public class PrintTreesPhase implements Phase {
	@Override
	public String getDescription() {
		return "Prints the tree.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		Printer printer = new Printer();
		task.getSourceUnits().forEach(sourceUnit -> {
			System.out.println(sourceUnit.getLocation());
			printer.accept(sourceUnit.getTree());
			System.out.println();
		});
		return task;
	}

	private static class Printer implements Consumer<IRNode> {
		private int indentation = 0;

		@Override
		public void accept(IRNode node) {
			for (int i = 0; i < indentation; i++) {
				System.out.print("  ");
			}
			if (node == null) {
				System.out.println("null");
			} else {
				System.out.println(node.getClass().getSimpleName());
				indentation++;
				node.forEachChild(this);
				indentation--;
			}
		}
	}
}
