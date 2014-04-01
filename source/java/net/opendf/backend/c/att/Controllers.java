package net.opendf.backend.c.att;

import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import javarag.*;
import net.opendf.ir.IRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Condition;
import net.opendf.ir.am.ICall;
import net.opendf.ir.am.ITest;
import net.opendf.ir.am.IWait;
import net.opendf.ir.am.Instruction;
import net.opendf.ir.am.PredicateCondition;
import net.opendf.ir.am.Scope;
import net.opendf.ir.am.State;
import net.opendf.ir.common.Expression;
import net.opendf.ir.net.Node;

public class Controllers extends Module<Controllers.Required> {

	interface Required {
		Node node(ActorMachine actorMachine);
		int index(Node node);
		Condition getCondition(ITest test, int condition);
		String condition(Condition cond);
		void controllerInstruction(Instruction instr, PrintWriter writer);
		String simpleExpression(Expression expression);
		ActorMachine actorMachine(IRNode node);
		void controllerContinue(ActorMachine actorMachine, PrintWriter writer);
		Set<Scope> scopeDependencies(Scope s);
		void initScopes(Instruction i, PrintWriter writer);
		List<Scope> scopesToInit(Instruction i);
		int index(IRNode node);
		Set<Scope> persistentScopes(IRNode node);
	}

	@Synthesized
	public void controller(ActorMachine actorMachine, PrintWriter writer) {
		int node = get().index(get().node(actorMachine));
		writer.println("static _Bool actor_n" + node + "(void) {");
		writer.println("_Bool progress = false;");
		writer.println("static int state = -1;");
		get().controllerContinue(actorMachine, writer);
		for (Scope s : get().persistentScopes(actorMachine.getController().get(0))) {
			int scope = get().index(s);
			writer.println("init_n"+node+"s"+scope+"();");
		}

		int state = 0;
		for (State s : actorMachine.getController()) {
			writer.println("S"+state+":");
			Instruction i = s.getInstructions().get(0);
			get().initScopes(i, writer);
			get().controllerInstruction(i, writer);
			state += 1;
		}
		writer.println("}");
	}

	@Synthesized
	public void initScopes(Instruction i, PrintWriter writer) {
		int node = get().index(get().node(get().actorMachine(i)));
		for (Scope s : get().scopesToInit(i)) {
			int scope = get().index(s);
			writer.println("init_n"+node+"s"+scope+"();");
		}
	}

	@Synthesized
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
			writer.println("case "+s+": goto S"+s+";");
		}
		writer.println("}");
	}

	@Inherited
	public Condition getCondition(ActorMachine actorMachine, int condition) {
		return actorMachine.getCondition(condition);
	}

	@Synthesized
	public String condition(PredicateCondition cond) {
		return get().simpleExpression(cond.getExpression());
	}

	@Synthesized
	public void controllerInstruction(ITest test, PrintWriter writer) {
		writer.print("if (");
		writer.print(get().condition(get().getCondition(test, test.C())));
		writer.println(") goto S"+test.S1()+";");
		writer.println("else goto S"+test.S0()+";");
	}

	@Synthesized
	public void controllerInstruction(IWait wait, PrintWriter writer) {
		writer.println("state = " + wait.S() + ";");
		writer.println("return progress;");
	}

	@Synthesized
	public void controllerInstruction(ICall call, PrintWriter writer) {
		ActorMachine am = get().actorMachine(call);
		Node node = get().node(am);
		int n = get().index(node);
		writer.println("transition_n"+n+"t"+call.T()+"();");
		writer.println("progress = true;");
		writer.println("goto S"+call.S()+";");
	}

}