package se.lth.cs.tycho.transform.caltoam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import se.lth.cs.tycho.errorhandling.ErrorModule;
import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.PortCondition;
import se.lth.cs.tycho.instance.am.PredicateCondition;
import se.lth.cs.tycho.instance.am.State;
import se.lth.cs.tycho.instance.am.Transition;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.cal.Actor;
import se.lth.cs.tycho.parser.lth.CalParser;
import se.lth.cs.tycho.transform.filter.PrioritizeCallInstructions;
import se.lth.cs.tycho.transform.util.StateHandler;
import se.lth.cs.tycho.util.ControllerToGraphviz;
import se.lth.cs.tycho.util.PrettyPrint;

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

	private GlobalEntityDecl parse(File file) {
		GlobalEntityDecl actor = parser.parse(file, null, null);
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
		GlobalEntityDecl actor = parse(file);
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
	
	private ActorMachine translate(GlobalEntityDecl actor) {
		return translator.translate((Actor) actor.getEntity());
	}

	public static void main(String[] args) throws FileNotFoundException {
		new TestActorTrans().run();
	}
	
}
