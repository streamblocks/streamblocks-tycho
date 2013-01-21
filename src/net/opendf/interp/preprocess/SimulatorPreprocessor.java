package net.opendf.interp.preprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.ICall;
import net.opendf.ir.am.ITest;
import net.opendf.ir.am.Instruction;
import net.opendf.ir.am.PortCondition;
import net.opendf.ir.am.PredicateCondition;
import net.opendf.ir.am.Scope;
import net.opendf.ir.am.Transition;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprInput;
import net.opendf.ir.common.ExprLambda;
import net.opendf.ir.common.ExprLet;
import net.opendf.ir.common.ExprList;
import net.opendf.ir.common.ExprMap;
import net.opendf.ir.common.ExprProc;
import net.opendf.ir.common.ExprSet;
import net.opendf.ir.common.ExprVariable;
import net.opendf.ir.common.PortName;
import net.opendf.ir.common.Statement;
import net.opendf.ir.common.StmtAssignment;
import net.opendf.ir.common.StmtOutput;
import net.opendf.util.AbstractDeclTraverser;
import net.opendf.util.AbstractExpressionTraverser;
import net.opendf.util.AbstractGeneratorFilterTraverser;
import net.opendf.util.AbstractStatementTraverser;

public class SimulatorPreprocessor {

	private final DeclTraverser declTraverser;
	private final StmtTraverser stmtTraverser;
	private final ExprTraverser exprTraverser;
	private final GenTraverser genTraverser;

	public SimulatorPreprocessor() {
		this.declTraverser = new DeclTraverser();
		this.stmtTraverser = new StmtTraverser();
		this.exprTraverser = new ExprTraverser();
		this.genTraverser = new GenTraverser();

		declTraverser.setExprTraverser(exprTraverser);

		stmtTraverser.setExprTraverser(exprTraverser);
		stmtTraverser.setDeclTraverser(declTraverser);
		stmtTraverser.setGenTraverser(genTraverser);

		exprTraverser.setDeclTraverser(declTraverser);
		exprTraverser.setStmtTraverser(stmtTraverser);
		exprTraverser.setGenTraverser(genTraverser);

		genTraverser.setDeclTraverser(declTraverser);
		genTraverser.setExprTraverser(exprTraverser);
	}

	private class DeclTraverser extends AbstractDeclTraverser<Env> {
		private void setExprTraverser(ExprTraverser exprTraverser) {
			exprVisitor = exprTraverser;
		}
		
		@Override
		public Env visitDeclVar(DeclVar d, Env p) {
			p.putOnStack(d.getName());
			return super.visitDeclVar(d, p);
		}

	}

	private class StmtTraverser extends AbstractStatementTraverser<Env> {
		private void setExprTraverser(ExprTraverser exprTraverser) {
			exprVisitor = exprTraverser;
		}

		private void setGenTraverser(GenTraverser genTraverser) {
			this.genTraverser = genTraverser;
		}

		private void setDeclTraverser(DeclTraverser declTraverser) {
			declVisitor = declTraverser;
		}
		
		@Override
		public Env visitStmtAssignment(StmtAssignment s, Env p) {
			int pos = p.getPosition(s.getVar());
			boolean stack = p.isOnStack(s.getVar());
			s.setVariablePosition(pos, stack);
			traverseExprs(s.getLocation(), p);
			return p;
		}
		
		@Override
		public Env visitStmtOutput(StmtOutput s, Env p) {
			s.setChannelId(p.getPort(s.getPort()));
			return super.visitStmtOutput(s, p);
		}

	}

	private class ExprTraverser extends AbstractExpressionTraverser<Env> {
		private void setGenTraverser(GenTraverser genTraverser) {
			this.genTraverser = genTraverser;
		}

		private void setDeclTraverser(DeclTraverser declTraverser) {
			declVisitor = declTraverser;
		}

		private void setStmtTraverser(StmtTraverser stmtTraverser) {
			stmtVisitor = stmtTraverser;
		}
		
		@Override
		public Env visitExprProc(ExprProc e, Env p) {
			super.visitExprProc(e, p.createFrame());
			return p;
		}

		@Override
		public Env visitExprLambda(ExprLambda e, Env p) {
			super.visitExprLambda(e, p.createFrame());
			return p;
		}

		@Override
		public Env visitExprLet(ExprLet e, Env p) {
			super.visitExprLet(e, p.createFrame());
			return p;
		}

		@Override
		public Env visitExprList(ExprList e, Env p) {
			super.visitExprList(e, p.createFrame());
			return p;
		}

		@Override
		public Env visitExprMap(ExprMap e, Env p) {
			super.visitExprMap(e, p.createFrame());
			return p;
		}

		@Override
		public Env visitExprSet(ExprSet e, Env p) {
			super.visitExprSet(e, p.createFrame());
			return p;
		}

		@Override
		public Env visitExprVariable(ExprVariable e, Env p) {
			String varName = e.getName();
			int pos = p.getPosition(varName);
			boolean stack = p.isOnStack(varName);
			e.setVariablePosition(pos, stack);
			return p;
		}
		
		@Override
		public Env visitExprInput(ExprInput e, Env p) {
			e.setChannelId(p.getPort(e.getPort()));
			return super.visitExprInput(e, p);
		}
	}

	private class GenTraverser extends AbstractGeneratorFilterTraverser<Env> {
		private void setExprTraverser(ExprTraverser exprTraverser) {
			exprVisitor = exprTraverser;
		}

		private void setDeclTraverser(DeclTraverser declTraverser) {
			declVisitor = declTraverser;
		}
	}

	public void process(ActorMachine actorMachine, Map<PortName, Integer> portMapping) {
		IntCounter memoryCounter = new IntCounter();
		Map<Scope, ScopeEnv> scopes = scopes(actorMachine, memoryCounter);
		Set<Transition> trans = new HashSet<Transition>();
		Set<PredicateCondition> pred = new HashSet<PredicateCondition>();
		Set<PortCondition> port = new HashSet<PortCondition>();
		getTransAndPredAndPort(actorMachine, trans, pred, port);
		for (Transition t : trans) {
			Env env = createEnv(t.getScope(), scopes, memoryCounter, portMapping);
			for (Statement s : t.getBody()) {
				s.accept(stmtTraverser, env);
			}
		}
		for (PredicateCondition c : pred) {
			Env env = createEnv(c.getScope(), scopes, memoryCounter, portMapping);
			c.getExpression().accept(exprTraverser, env);
		}
		for (PortCondition c : port) {
			int id = portMapping.get(c.getPortName());
			c.setChannelId(id);
		}
	}

	private Env createEnv(List<Scope> scopes, Map<Scope, ScopeEnv> envs, IntCounter memoryCounter, Map<PortName, Integer> portPos) {
		List<ScopeEnv> envList = new ArrayList<ScopeEnv>(scopes.size());
		for (Scope s : scopes) {
			envList.add(envs.get(s));
		}
		Env env = new ScopeListEnv(envList, memoryCounter).createFrame();
		for (Entry<PortName, Integer> entry : portPos.entrySet()) {
			env.putPort(entry.getKey(), entry.getValue());
		}
		return env;
	}

	private void getTransAndPredAndPort(ActorMachine actorMachine, Set<Transition> trans, Set<PredicateCondition> pred, Set<PortCondition> port) {
		for (List<Instruction> state : actorMachine.getController()) {
			for (Instruction instr : state) {
				if (instr instanceof ICall) {
					trans.add(((ICall) instr).T());
				}
				if (instr instanceof ITest) {
					ITest test = (ITest) instr;
					if (test.C() instanceof PredicateCondition) {
						pred.add((PredicateCondition) test.C());
					} else if (test.C() instanceof PortCondition) {
						port.add((PortCondition) test.C());
					}
				}
			}
		}
	}

	private Map<Scope, ScopeEnv> scopes(ActorMachine actorMachine, IntCounter memoryCounter) {
		Map<Scope, ScopeEnv> scopeEnvs = new HashMap<Scope, ScopeEnv>();
		for (Scope scope : actorMachine.getScopes()) {
			ScopeEnv env = new ScopeEnv(null, memoryCounter);
			scopeEnvs.put(scope, env);
			for (Decl d : scope.getDeclarations()) {
				if (d instanceof DeclVar) {
					env.putInMemory(d.getName());
				}
			}
		}
		return scopeEnvs;
	}
}
