package net.opendf.transform.caltoam;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opendf.ir.cal.Action;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.cal.InputPattern;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprInput;
import net.opendf.ir.common.ExprLambda;
import net.opendf.ir.common.ExprLet;
import net.opendf.ir.common.ExprLiteral;
import net.opendf.ir.common.ExprLiteral.Kind;
import net.opendf.ir.common.ExprProc;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.Port;
import net.opendf.ir.common.Statement;
import net.opendf.ir.common.StmtBlock;
import net.opendf.ir.common.Variable;
import net.opendf.ir.util.ImmutableList;
import net.opendf.transform.caltoam.util.BitSets;
import net.opendf.transform.util.AbstractActorTransformer;

class ActorVariableExtractor extends AbstractActorTransformer<ActorVariableExtractor.Variables> {

	public Result extractVariables(Actor actor) {
		Variables data = new Variables();
		Actor resultActor = transformActor(actor, data);
		ImmutableList<DeclVar> varDecls = ImmutableList.copyOf(data.varDecls);
		ImmutableList<Integer> actorVars = ImmutableList.copyOf(BitSets.iterable(data.actorVars));
		ImmutableList.Builder<ImmutableList<Integer>> actionVarsBuilder = ImmutableList.builder();
		for (BitSet actionVars : data.actionVars) {
			actionVarsBuilder.add(ImmutableList.copyOf(BitSets.iterable(actionVars)));
		}
		return new Result(resultActor, varDecls, actorVars, actionVarsBuilder.build());
	}

	@Override
	public Actor transformActor(Actor actor, Variables vars) {
		for (DeclVar var : actor.getVarDecls()) {
			vars.addActorVar(var.getName());
		}
		Actor result = super.transformActor(actor, vars);
		for (DeclVar var : result.getVarDecls()) {
			vars.addVarDecl(var);
		}
		return result;
	}

	@Override
	public ImmutableList<Action> transformActions(ImmutableList<Action> actions, Variables vars) {
		ImmutableList.Builder<Action> builder = ImmutableList.builder();
		for (Action action : actions) {
			builder.add(transformAction(action, vars.actionFrame()));
		}
		return builder.build();
	}

	@Override
	public Action transformAction(Action action, Variables vars) {
		for (InputPattern input : action.getInputPatterns()) {
			for (String var : input.getVariables()) {
				vars.addActionVar(var);
			}
		}
		for (DeclVar var : action.getVarDecls()) {
			vars.addActionVar(var.getName());
		}
		Action result = super.transformAction(action, vars);
		for (InputPattern input : result.getInputPatterns()) {
			int offset = 0;
			final boolean hasRepeat = input.getRepeatExpr() != null;
			final int repeat = hasRepeat ? literalToInt(input.getRepeatExpr()) : -1;
			final int length = input.getVariables().size();
			final Port port = input.getPort();
			for (String var : input.getVariables()) {
				ExprInput exprInput = hasRepeat ? new ExprInput(port, offset, repeat, length) : new ExprInput(port,
						offset);
				vars.addVarDecl(new DeclVar(null, var, null, exprInput, false));
				offset += 1;
			}
		}
		for (DeclVar var : result.getVarDecls()) {
			vars.addVarDecl(var);
		}
		return result;
	}

	@Override
	public Expression visitExprLambda(ExprLambda lambda, Variables vars) {
		return super.visitExprLambda(lambda, vars.dynamicFrame());
	}

	@Override
	public Expression visitExprProc(ExprProc proc, Variables vars) {
		return super.visitExprProc(proc, vars.dynamicFrame());
	}

	@Override
	public Expression visitExprLet(ExprLet let, Variables vars) {
		return super.visitExprLet(let, vars.dynamicFrame());
	}

	@Override
	public Statement visitStmtBlock(StmtBlock block, Variables vars) {
		return super.visitStmtBlock(block, vars.dynamicFrame());
	}

	@Override
	public DeclVar transformVarDecl(DeclVar varDecl, Variables vars) {
		vars.declare(varDecl.getName());
		return super.transformVarDecl(varDecl, vars);
	}

	@Override
	public ParDeclValue transformValueParameter(ParDeclValue valueParam, Variables vars) {
		vars.declare(valueParam.getName());
		return super.transformValueParameter(valueParam, vars);
	}

	@Override
	public Variable transformVariable(Variable var, Variables vars) {
		return vars.transform(var);
	}

	private int literalToInt(Expression expr) {
		assert expr instanceof ExprLiteral;
		ExprLiteral lit = (ExprLiteral) expr;
		assert lit.getKind() == Kind.Integer;
		return Integer.parseInt(lit.getText());
	}

	public static class Result {
		public final Actor actor;
		public final ImmutableList<DeclVar> varDecls;
		public final ImmutableList<Integer> actorVars;
		/** Action variables for each initializer (first) and each action (after). */
		public final ImmutableList<ImmutableList<Integer>> actionVars;

		public Result(Actor actor, ImmutableList<DeclVar> varDecls, ImmutableList<Integer> actorVars,
				ImmutableList<ImmutableList<Integer>> actionVars) {
			this.actor = actor;
			this.varDecls = varDecls;
			this.actorVars = actorVars;
			this.actionVars = actionVars;
		}
	}

	public static class Variables {
		private final Map<String, Integer> staticVarsNums;
		private final Set<String> shaddowedVars;
		private final List<DeclVar> varDecls;
		private final BitSet actorVars;
		private final LinkedList<BitSet> actionVars;
		private final boolean dynamicScope;

		public Variables() {
			staticVarsNums = new HashMap<>();
			shaddowedVars = new HashSet<>();
			varDecls = new ArrayList<>();
			actorVars = new BitSet();
			actionVars = new LinkedList<>();
			dynamicScope = false;
		}

		private Variables(Map<String, Integer> staticVarNums, Set<String> shaddowedVars, List<DeclVar> varDecls,
				BitSet actorVars, LinkedList<BitSet> actionVars, boolean dynamicScope) {
			this.staticVarsNums = new HashMap<>(staticVarNums);
			this.shaddowedVars = new HashSet<>(shaddowedVars);
			this.varDecls = varDecls;
			this.actorVars = actorVars;
			this.actionVars = actionVars;
			this.dynamicScope = dynamicScope;
		}

		public void addActorVar(String name) {
			int offset = varDecls.size();
			staticVarsNums.put(name, offset);
			varDecls.add(null);
			actorVars.set(offset);
		}

		public void addActionVar(String name) {
			int offset = varDecls.size();
			staticVarsNums.put(name, offset);
			varDecls.add(null);
			actionVars.getLast().set(offset);
		}

		public void addVarDecl(DeclVar decl) {
			varDecls.set(staticVarsNums.get(decl.getName()), decl);
		}

		public void declare(String name) {
			if (dynamicScope) {
				shaddowedVars.add(name);
			}
		}

		public Variable transform(Variable var) {
			String name = var.getName();
			if (!shaddowedVars.contains(name) && staticVarsNums.containsKey(name)) {
				return var.copy(name, staticVarsNums.get(name));
			}
			return var;
		}

		public Variables actionFrame() {
			actionVars.addLast(new BitSet());
			return new Variables(staticVarsNums, shaddowedVars, varDecls, actorVars, actionVars, false);
		}

		public Variables dynamicFrame() {
			return new Variables(staticVarsNums, shaddowedVars, varDecls, actorVars, actionVars, true);
		}
	}
}
