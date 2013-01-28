package net.opendf.interp;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import net.opendf.interp.values.RefView;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.ConditionVisitor;
import net.opendf.ir.am.ICall;
import net.opendf.ir.am.ITest;
import net.opendf.ir.am.IWait;
import net.opendf.ir.am.Instruction;
import net.opendf.ir.am.InstructionVisitor;
import net.opendf.ir.am.PortCondition;
import net.opendf.ir.am.PredicateCondition;
import net.opendf.ir.am.Scope;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.Statement;

public class BasicActorMachineRunner implements ActorMachineRunner, InstructionVisitor<Integer, Environment>,
		ConditionVisitor<Boolean, Environment> {

	private final Simulator simulator;
	private final ActorMachine actorMachine;
	private final Decl[] decls;

	private BitSet liveVariables;
	private int state;

	public BasicActorMachineRunner(Simulator simulator, ActorMachine actorMachine) {
		this.simulator = simulator;
		this.actorMachine = actorMachine;
		this.decls = collectDecls(actorMachine);
		this.liveVariables = new BitSet();
		this.state = 0;
	}

	private Decl[] collectDecls(ActorMachine actorMachine) {
		HashMap<Integer, Decl> declMap = new HashMap<Integer, Decl>();
		int max = 0;
		for (Scope s : actorMachine.getScopes()) {
			for (Decl d : s.getDeclarations()) {
				if (d instanceof DeclVar) {
					DeclVar varDecl = (DeclVar) d;
					assert !varDecl.isVariableOnStack();
					int pos = varDecl.getVariablePosition();
					declMap.put(pos, varDecl);
					max = Math.max(max, pos);
				}
			}
		}
		Decl[] d = new Decl[max + 1];
		for (Map.Entry<Integer, Decl> declEntry : declMap.entrySet()) {
			d[declEntry.getKey()] = declEntry.getValue();
		}
		return d;
	}

	@Override
	public void step() {
		boolean done = false;
		while (!done) {
			Instruction i = actorMachine.getInstructions(state).get(0);
			state = i.accept(this, simulator.actorMachineEnvironment());
			if (i instanceof ICall || i instanceof IWait) {
				done = true;
			}
		}
	}

	private void initVars(BitSet vars) {
		BitSet s = new BitSet();
		s.or(vars);
		s.andNot(liveVariables);
		Declarator d = simulator.declarator();
		Environment env = simulator.actorMachineEnvironment();
		for (int i = s.nextSetBit(0); i >= 0; i = s.nextSetBit(i + 1)) {
			d.declare(decls[i], env);
		}
		liveVariables.or(s);
	}

	private void remVars(BitSet vars) {
		liveVariables.andNot(vars);
	}

	@Override
	public Integer visitWait(IWait i, Environment p) {
		return i.S();
	}

	@Override
	public Integer visitTest(ITest i, Environment p) {
		boolean cond = i.C().accept(this, p);
		return cond ? i.S1() : i.S0();
	}

	@Override
	public Integer visitCall(ICall i, Environment p) {
		initVars(i.T().getRequiredVariables());
		Executor exec = simulator.executor();
		for (Statement s : i.T().getBody()) {
			exec.execute(s, p);
		}
		remVars(i.T().getInvalidatedVariables());
		return i.S();
	}

	@Override
	public Boolean visitInputCondition(PortCondition c, Environment p) {
		int id = c.getChannelId();
		Channel channel = simulator.actorMachineEnvironment().getChannel(id);
		return channel.tokens(c.N());
	}

	@Override
	public Boolean visitOutputCondition(PortCondition c, Environment p) {
		int id = c.getChannelId();
		Channel channel = simulator.actorMachineEnvironment().getChannel(id);
		return channel.space(c.N());
	}

	@Override
	public Boolean visitPredicateCondition(PredicateCondition c, Environment p) {
		initVars(c.getRequiredVariables());
		RefView cond = simulator.evaluator().evaluate(c.getExpression(), p);
		return simulator.converter().getBoolean(cond);
	}

}
