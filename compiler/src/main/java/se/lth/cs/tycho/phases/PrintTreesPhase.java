package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.settings.OnOffSetting;
import se.lth.cs.tycho.settings.Setting;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class PrintTreesPhase implements Phase {
	public static Setting<Boolean> printTrees = new OnOffSetting() {
		@Override public String getKey() { return "print-trees"; }
		@Override public String getDescription() { return "Enables printing of source tree structures"; }
		@Override public Boolean defaultValue() { return false; }
	};

	@Override
	public List<Setting<?>> getPhaseSettings() {
		return Collections.singletonList(printTrees);
	}

	@Override
	public String getDescription() {
		return "Prints the tree.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		if (context.getConfiguration().get(printTrees)) {
			Printer printer = new Printer();
			task.forEachChild(printer::accept);
		}
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
				String extra = extra(node);
				if (extra != null) {
					System.out.print(node.getClass().getSimpleName());
					System.out.print(" [");
					System.out.print(extra);
					System.out.println("]");
				} else {
					System.out.println(node.getClass().getSimpleName());
				}
				indentation++;
				node.forEachChild(this);
				indentation--;
			}
		}
		private String extra(IRNode node) {
			if (node instanceof Variable) {
				return ((Variable) node).getName();
			}
			if (node instanceof Decl) {
				return ((Decl) node).getName();
			}
			return null;
		}

	}
}
