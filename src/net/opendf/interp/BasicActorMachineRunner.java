package net.opendf.interp;

import java.util.BitSet;

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
import net.opendf.ir.common.Statement;

public class BasicActorMachineRunner implements ActorMachineRunner, InstructionVisitor<Integer, Environment>, ConditionVisitor<Boolean, Environment> {
	
	private final Simulator simulator;
	private final ActorMachine actorMachine;

	private BitSet liveVariables;
	private int state;

	public BasicActorMachineRunner(Simulator simulator, ActorMachine actorMachine) {
		this.simulator = simulator;
		this.actorMachine = actorMachine;
		this.liveVariables = new BitSet();
		this.state = 0;
	}
	
	@Override
	public void step() {
		Instruction i = actorMachine.getInstructions(state).get(0);
		state = i.accept(this, null);
	}
	
	private void initVars(BitSet vars) {
		BitSet s = new BitSet();
		s.or(vars);
		s.andNot(liveVariables);
		for (int i = s.nextSetBit(0); i >= 0; i = s.nextSetBit(i+1)) {
			// TODO implement
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
		initVars(i.getRequiredVariables());
		boolean cond = i.C().accept(this, p);
		return cond ? i.S1() : i.S0();
	}

	@Override
	public Integer visitCall(ICall i, Environment p) {
		initVars(i.getRequiredVariables());
		Executor exec = simulator.executor();
		for (Statement s : i.T().getBody()) {
			exec.execute(s, p);
		}
		remVars(i.getInvalidatedVariables());
		return i.S();
	}

	@Override
	public Boolean visitInputCondition(PortCondition c, Environment p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visitOutputCondition(PortCondition c, Environment p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visitPredicateCondition(PredicateCondition c, Environment p) {
		RefView cond = simulator.evaluator().evaluate(c.getExpression(), p);
		return simulator.converter().getBoolean(cond);
	}
	
}
