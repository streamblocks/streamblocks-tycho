package se.lth.cs.tycho.backend.c.att;

import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.ICall;
import se.lth.cs.tycho.ir.entity.am.ITest;
import se.lth.cs.tycho.ir.entity.am.IWait;
import se.lth.cs.tycho.ir.entity.am.Instruction;
import se.lth.cs.tycho.ir.entity.am.PredicateCondition;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.am.State;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.net.Node;
import javarag.*;

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
	}

	public void controller(ActorMachine actorMachine, PrintWriter writer) {
		int node = e().index(e().node(actorMachine));
		writer.println("static _Bool actor_n" + node + "(void) {");
		writer.println("_Bool progress = false;");
		writer.println("static int state = -1;");
		e().controllerContinue(actorMachine, writer);
		for (Scope s : e().persistentScopes(actorMachine.getController().get(0))) {
			int scope = e().index(s);
			writer.println("init_n" + node + "s" + scope + "();");
		}

		int state = 0;
		for (State s : actorMachine.getController()) {
			writer.println("S" + state + ":");
			writer.println("AM_TRACE_STATE(" + state + ");");
			Instruction i = s.getInstructions().get(0);
			e().initScopes(i, writer);
			e().controllerInstruction(i, writer);
			state += 1;
		}
		writer.println("}");
	}

	public void initScopes(Instruction i, PrintWriter writer) {
		int node = e().index(e().node(e().actorMachine(i)));
		for (Scope s : e().scopesToInit(i)) {
			int scope = e().index(s);
			writer.println("init_n" + node + "s" + scope + "();");
		}
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
		writer.println("AM_TRACE_TEST(" + test.C() + ");");
		writer.print("if (");
		writer.print(e().condition(e().getCondition(test, test.C())));
		writer.println(") goto S" + test.S1() + ";");
		writer.println("else goto S" + test.S0() + ";");
	}

	public void controllerInstruction(IWait wait, PrintWriter writer) {
		writer.println("AM_TRACE_WAIT();");
		writer.println("state = " + wait.S() + ";");
		writer.println("return progress;");
	}

	public void controllerInstruction(ICall call, PrintWriter writer) {
		writer.println("AM_TRACE_CALL(" + call.T() + ");");
		ActorMachine am = e().actorMachine(call);
		Node node = e().node(am);
		int n = e().index(node);
		writer.println("transition_n" + n + "t" + call.T() + "();");
		writer.println("progress = true;");
		writer.println("goto S" + call.S() + ";");
	}

}