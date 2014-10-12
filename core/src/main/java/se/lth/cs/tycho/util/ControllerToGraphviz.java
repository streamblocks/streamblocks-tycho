package se.lth.cs.tycho.util;

import java.io.PrintWriter;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.ICall;
import se.lth.cs.tycho.instance.am.ITest;
import se.lth.cs.tycho.instance.am.IWait;
import se.lth.cs.tycho.instance.am.Instruction;
import se.lth.cs.tycho.instance.am.InstructionVisitor;
import se.lth.cs.tycho.instance.am.State;

public class ControllerToGraphviz {

	public static void print(PrintWriter pw, ActorMachine am, String name) {
		PrintVisitor pv = new PrintVisitor(pw, am, name);
		pv.print();
	}

	static class PrintVisitor implements InstructionVisitor<Void, Void> {
		private PrintWriter printWriter;
		private ActorMachine am;
		private String name;
		private int currentState = 0;
		private int instrCount = 0;

		private PrintVisitor(PrintWriter pw, ActorMachine am, String name) {
			this.printWriter = pw;
			this.am = am;
			this.name = name;
		}

		void print() {
			printWriter.println("digraph " + name + " {");
			currentState = 0;
			instrCount = 0;
			for (State state : am.getController()) {
				printWriter.println("  " + currentState + " [shape=circle,style=filled];");
				if (state != null)
					for (Instruction i : state.getInstructions()) {
						i.accept(this, null);
						instrCount++;
					}
				currentState++;
			}
			printWriter.println("}");
			printWriter.flush();
		}

		public Void visitWait(IWait i, Void v) {
			printWriter.print("  i" + instrCount + "[shape=doublecircle,label=\"\",width=\"0.2\",heigth=\"0.2\"];");
			printWriter.println(" " + currentState + " -> i" + instrCount + " -> " + i.S() + ";");
			return null;
		}

		public Void visitTest(ITest i, Void v) {
			printWriter.print("  i" + instrCount + "[shape=diamond,label=\"" + i.C() + "\"];");
			printWriter.print(" " + currentState + " -> i" + instrCount + ";");
			printWriter.print(" i" + instrCount + " -> " + i.S0() + " [style=dashed];");
			printWriter.println(" i" + instrCount + " -> " + i.S1() + ";");
			return null;
		}

		public Void visitCall(ICall i, Void v) {
			printWriter.print("  i" + instrCount + "[shape=rectangle,label=\"" + i.T() + "\"];");
			printWriter.println(" " + currentState + " -> i" + instrCount + " -> " + i.S() + ";");
			return null;
		}

	}

}
