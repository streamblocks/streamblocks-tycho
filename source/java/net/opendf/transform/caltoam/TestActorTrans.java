package net.opendf.transform.caltoam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import net.opendf.errorhandling.ErrorModule;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Condition;
import net.opendf.ir.am.PortCondition;
import net.opendf.ir.am.PredicateCondition;
import net.opendf.ir.am.State;
import net.opendf.ir.am.Transition;
import net.opendf.ir.cal.Actor;
import net.opendf.parser.lth.CalParser;
import net.opendf.transform.filter.PrioritizeCallInstructions;
import net.opendf.transform.util.StateHandler;
import net.opendf.util.ControllerToGraphviz;
import net.opendf.util.PrettyPrint;

class TestActorTrans {
	private CalParser parser = new CalParser();
	private PrettyPrint prettyPrint = new PrettyPrint();
	private ActorToActorMachine translator = new ActorToActorMachine() {
		@Override
		protected StateHandler<ActorStates.State> getStateHandler(StateHandler<ActorStates.State> stateHandler) {
			stateHandler = new PrioritizeCallInstructions<>(stateHandler);
			//stateHandler = new SelectRandomInstruction<>(stateHandler);
			return stateHandler;
		}
	};

	private Actor parse(File file) {
		Actor actor = parser.parse(file, null, null);
		ErrorModule errors = parser.getErrorModule();
		if (errors.hasError()) {
			errors.printErrors();
		}
		return actor;
	}
	
	private void run() throws FileNotFoundException {
		//File file = new File("KahnExample/Init.cal");
		//File file = new File("/Users/gustav/Programmering/orc-apps/Research/src/ch/epfl/mpeg4/part2/motion/Algo_Add.cal");
		//File file = new File("/Users/gustav/Programmering/dataflow/examples/MPEG4_SP_Decoder/ParseHeaders.cal");
		//File file = new File("/Users/gustav/Programmering/dataflow-public/codegen/WriteParserOutput.cal");
		File file = new File("/Users/gustav/Programmering/dataflow/doc/papers/2013 Asilomar -- AM Classification/ex_again/Split.cal");
		File gv = new File("controller.gv");
		Actor actor = parse(file);
		//ActorMachine actorMachine = OutputConditionAdder.addOutputConditions(translate(actor));
		ActorMachine actorMachine = translate(actor);
		ControllerToGraphviz.print(new PrintWriter(gv), actorMachine, "controller");
		
		System.out.println("=== CONDITIONS ===");
		int c = 0;
		for (Condition cond : actorMachine.getConditions()) {
			System.out.print("[" + c + "] ");
			if (cond instanceof PortCondition) {
				PortCondition pc = (PortCondition) cond;
				System.out.println(pc.getPortName().getName() + "," + pc.N());
			} else if (cond instanceof PredicateCondition) {
				PredicateCondition pc = (PredicateCondition) cond;
				prettyPrint.print(pc.getExpression());
				System.out.println();
			}
			c += 1;
		}
		System.out.println("\n\n\n=== TRANSITIONS ===");
		int t = 0;
		for (Transition trans : actorMachine.getTransitions()) {
			System.out.println("[" + t + "]");
			prettyPrint.print(trans.getBody());
			System.out.println();
			t += 1;
		}
		
		System.out.println("\n\n\n=== KILL SETS ===");
		t = 0;
		for (Transition trans : actorMachine.getTransitions()) {
			System.out.println("[" + t + "] " + trans.getScopesToKill());
			t += 1;
		}

		System.out.println("\n\n\n=== INSTRUCIOTNS PER STATE HISTOGRAM ===");
		int[] instructions = new int[10];
		for (State state : actorMachine.getController()) {
			instructions[state.getInstructions().size()] += 1;
		}
		for (int i = 0; i < instructions.length; i++) {
			System.out.println("[" + i + "] " + instructions[i]);
		}
	}
	
	private ActorMachine translate(Actor actor) {
		return translator.translate(actor);
	}

	public static void main(String[] args) throws FileNotFoundException {
		new TestActorTrans().run();
	}
	
}
