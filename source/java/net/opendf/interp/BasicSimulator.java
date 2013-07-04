package net.opendf.interp;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import net.opendf.interp.values.RefView;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Condition;
import net.opendf.ir.am.ConditionVisitor;
import net.opendf.ir.am.ICall;
import net.opendf.ir.am.ITest;
import net.opendf.ir.am.IWait;
import net.opendf.ir.am.Instruction;
import net.opendf.ir.am.InstructionVisitor;
import net.opendf.ir.am.PortCondition;
import net.opendf.ir.am.PredicateCondition;
import net.opendf.ir.am.Transition;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.Statement;

public class BasicSimulator implements Simulator, InstructionVisitor<Integer, Environment>,
		ConditionVisitor<Boolean, Environment> {

	private final ActorMachine actorMachine;
	private final Environment environment;
	private final Interpreter interpreter;
//	private final Decl[] decls;
	
	private final TypeConverter converter;

	private BitSet liveVariables;
	private int state;

	public BasicSimulator(ActorMachine actorMachine, Environment environment, Interpreter interpreter) {
		this.actorMachine = actorMachine;
		this.environment = environment;
		this.interpreter = interpreter;
//		this.decls = collectDecls(actorMachine);
		this.converter = TypeConverter.getInstance();
		this.liveVariables = new BitSet();
		this.state = 0;
	}
/*
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
*/
	@Override
	public void step() {
		boolean done = false;
		while (!done) {
			Instruction i = actorMachine.getInstructions(state).get(0);
			state = i.accept(this, environment);
			if (i instanceof ICall || i instanceof IWait) {
				done = true;
			}
		}
	}
/*
	private void initVars(BitSet vars) {
		BitSet s = new BitSet();
		s.or(vars);
		s.andNot(liveVariables);
		for (int i = s.nextSetBit(0); i >= 0; i = s.nextSetBit(i + 1)) {
			interpreter.declare(decls[i], environment);
		}
		liveVariables.or(s);
	}
*/
	private void remVars(BitSet vars) {
		liveVariables.andNot(vars);
	}

	@Override
	public Integer visitWait(IWait i, Environment p) {
		return i.S();
	}

	@Override
	public Integer visitTest(ITest i, Environment p) {
		Condition condExpr = actorMachine.getCondition(i.C());
		boolean cond = condExpr.accept(this, p);
		return cond ? i.S1() : i.S0();
	}

	@Override
	public Integer visitCall(ICall i, Environment p) {
//FIXME		initVars(i.T().getRequiredVariables());
		Transition trans = actorMachine.getTransition(i.T());
//FIXME		trans.getBody().execute(s, p);
//FIXME		remVars(i.T().getInvalidatedVariables());
		return i.S();
	}

	@Override
	public Boolean visitInputCondition(PortCondition c, Environment p) {
		int id = c.getPortName().getOffset();
		Channel.OutputEnd channel = environment.getChannelOut(id);
		return channel.tokens(c.N());
	}

	@Override
	public Boolean visitOutputCondition(PortCondition c, Environment p) {
		int id = c.getPortName().getOffset();
		Channel.InputEnd channel = environment.getChannelIn(id);
		return channel.space(c.N());
	}

	@Override
	public Boolean visitPredicateCondition(PredicateCondition c, Environment p) {
		initVars(c.getRequiredVariables());
		RefView cond = interpreter.evaluate(c.getExpression(), p);
		return converter.getBoolean(cond);
	}

}
