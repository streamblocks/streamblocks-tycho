package se.lth.cs.tycho.backend.c.att;

import javarag.Cached;
import javarag.Inherited;
import javarag.Module;
import javarag.Procedural;
import javarag.Synthesized;
import se.lth.cs.tycho.backend.c.ScopeInitialization;
import se.lth.cs.tycho.backend.c.ScopeInitializationNew;
import se.lth.cs.tycho.backend.c.ScopeInitializationOld;
import se.lth.cs.tycho.instance.am.*;
import se.lth.cs.tycho.instance.am.State;
import se.lth.cs.tycho.instance.am.ctrl.*;
import se.lth.cs.tycho.instance.am.ctrl.Transition;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;

import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class Controllers extends Module<Controllers.Decls> {

	public interface Decls {
		@Procedural
		void controller(ActorMachine actorMachine, PrintWriter writer);

		@Procedural
		void initScopes(Instruction i, PrintWriter writer);

		@Procedural
		void controllerContinue(ActorMachine actorMachine, PrintWriter writer);

		@Inherited
		Condition getCondition(ITest test, int condition);

		@Synthesized
		String condition(Condition cond);

		@Procedural
		void controllerInstruction(Instruction instr, PrintWriter writer);

		Node node(ActorMachine actorMachine);

		String simpleExpression(Expression expression);

		ActorMachine actorMachine(IRNode node);

		List<Scope> scopesToInit(Instruction i);

		int index(IRNode node);

		Set<Scope> persistentScopes(IRNode node);

		@Synthesized
		@Cached
		ScopeInitializationNew scopeInitialization(ActorMachine am);

	}

//	public void controllerNew(ActorMachine actorMachine, PrintWriter writer) {
//		int node = e().index(e().node(actorMachine));
//		Map<se.lth.cs.tycho.instance.am.ctrl.State, Integer> stateNumbers;
//		{
//			stateNumbers = new HashMap<>();
//			int i = 0;
//			for (se.lth.cs.tycho.instance.am.ctrl.State s : actorMachine.controller().getAllStates()) {
//				stateNumbers.put(s, i++);
//			}
//		}
//		int[] waitTargets = actorMachine.controller().getAllStates().stream()
//				.flatMap(s -> s.getTransitions().stream())
//				.filter(t -> t.getKind() == TransitionKind.WAIT)
//				.map(t -> ((Wait) t).target())
//				.mapToInt(stateNumbers::get)
//				.sorted()
//				.distinct()
//				.toArray();
//
//		BitSet persistentScopes;
//		{
//			persistentScopes = new BitSet();
//			persistentScopes.set(0, actorMachine.getScopes().size());
//			actorMachine.getTransitions().stream()
//					.flatMapToInt(t -> t.getScopesToKill().stream().mapToInt(Integer::intValue))
//					.distinct()
//					.forEach(persistentScopes::clear);
//		}
//
//		writer.println("static _Bool actor_n" + node + "(void) {");
//		writer.println("_Bool progress = false;");
//		writer.println("static int state = -1;");
//
//		writer.println("switch (state) {");
//		writer.println("case -1: break;");
//		for (int s : waitTargets) {
//			writer.println("case " + s + ": goto S" + s + ";");
//		}
//		writer.println("}");
//
//		persistentScopes.stream().forEach(
//				scope -> writer.println("init_n" + node + "s" + scope + "();")
//		);
//
//		int state = 0;
//		for (se.lth.cs.tycho.instance.am.ctrl.State s : actorMachine.controller().getAllStates()) {
//			writer.println("S" + state + ":");
//			writer.println("AM_TRACE_STATE(" + node + ", " + state + ");");
//			Transition i = s.getTransitions().get(0);
//
//			// FIXME e().initScopes(i, writer);
//			// FIXME e().controllerInstruction(i, writer);
//			state += 1;
//		}
//		writer.println("}");
//	}

	public void controller(ActorMachine actorMachine, PrintWriter writer) {
		int node = e().index(e().node(actorMachine));
		writer.println("static _Bool actor_n" + node + "(void) {");
		writer.println("_Bool progress = false;");
		writer.println("static int state = -1;");
		e().controllerContinue(actorMachine, writer);
		e().scopeInitialization(actorMachine)
				.persistentScopes().stream()
				.forEach(scope -> writer.println("init_n" + node + "s" + scope + "();"));
//		for (Scope s : e().persistentScopes(actorMachine.getController().get(0))) {
//			int scope = e().index(s);
//			writer.println("init_n" + node + "s" + scope + "();");
//		}

//		int state = 0;
//		for (se.lth.cs.tycho.instance.am.ctrl.State s : actorMachine.controller().getAllStates()) {
//			writer.println("S" + state + ":");
//			writer.println("AM_TRACE_STATE(" + node + ", " + state + ");");
//			se.lth.cs.tycho.instance.am.ctrl.Transition i = s.getTransitions().get(0);
//
//			ScopeInitializationNew init = e().scopeInitialization(actorMachine);
//			init.scopesToInitialize(i).stream().forEach(scope -> {
//				writer.println("init_n" + node + "s" + scope + "();");
//			});
//
//			e().controllerInstructionNew(i, writer);
//			state += 1;
//		}


		int state = 0;
		for (State s : actorMachine.getController()) {
			writer.println("S" + state + ":");
			writer.println("AM_TRACE_STATE(" + node + ", " + state + ");");
			Instruction i = s.getInstructions().get(0);
			e().initScopes(i, writer);
			e().controllerInstruction(i, writer);
			state += 1;
		}
		writer.println("}");
	}

	public ScopeInitializationNew scopeInitialization(ActorMachine am) {
		return new ScopeInitializationNew(am);
	}

	private void initScopesOld(Instruction i, PrintWriter writer) {
		int node = e().index(e().node(e().actorMachine(i)));
		for (Scope s : e().scopesToInit(i)) {
			int scope = e().index(s);
			writer.println("init_n" + node + "s" + scope + "();");
		}
	}

	public void initScopes(Instruction i, PrintWriter writer) {
		ActorMachine actorMachine = e().actorMachine(i);
		int node = e().index(e().node(actorMachine));
		ScopeInitialization init = e().scopeInitialization(actorMachine);
		init.scopesToInitialize(i).stream().forEach(scope -> {
			writer.println("init_n" + node + "s" + scope + "();");
		});
	}

	public void controllerContinue(ActorMachine actorMachine, PrintWriter writer) {
		TreeSet<Integer> states = new TreeSet<>();
		states.add(0);
		for (State s : actorMachine.getController()) {
			Instruction i = s.getInstructions().get(0);
			if (i instanceof IWait) {
				states.add(((IWait) i).S());
			}
		}
		writer.println("switch (state) {");
		writer.println("case -1: break;");
		for (int s : states) {
			writer.println("case " + s + ": goto S" + s + ";");
		}
		writer.println("}");
	}

	public Condition getCondition(ActorMachine actorMachine, int condition) {
		return actorMachine.getCondition(condition);
	}

	public String condition(PredicateCondition cond) {
		return e().simpleExpression(cond.getExpression());
	}

	public void controllerInstruction(ITest test, PrintWriter writer) {
		ActorMachine am = e().actorMachine(test);
		Node node = e().node(am);
		int n = e().index(node);
		writer.print("if (");
		writer.print(e().condition(e().getCondition(test, test.C())));
		writer.println(") {");
		writer.println("AM_TRACE_TEST(" + n + ", " + test.C() + ", true);");
		writer.println("goto S" + test.S1() + ";");
		writer.println("} else {");
		writer.println("AM_TRACE_TEST(" + n + ", " + test.C() + ", false);");
		writer.println("goto S" + test.S0() + ";");
		writer.println("}");
	}

	public void controllerInstruction(IWait wait, PrintWriter writer) {
		ActorMachine am = e().actorMachine(wait);
		Node node = e().node(am);
		int n = e().index(node);
		writer.println("AM_TRACE_WAIT(" + n + ");");
		writer.println("state = " + wait.S() + ";");
		writer.println("return progress;");
	}

	public void controllerInstruction(ICall call, PrintWriter writer) {
		ActorMachine am = e().actorMachine(call);
		Node node = e().node(am);
		int n = e().index(node);
		writer.println("AM_TRACE_CALL(" + n + "," + call.T() + ");");
		writer.println("transition_n" + n + "t" + call.T() + "();");
		writer.println("progress = true;");
		writer.println("goto S" + call.S() + ";");
	}

}