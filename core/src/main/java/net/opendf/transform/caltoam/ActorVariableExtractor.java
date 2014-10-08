package net.opendf.transform.caltoam;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.opendf.ir.Port;
import net.opendf.ir.Variable;
import net.opendf.ir.decl.LocalVarDecl;
import net.opendf.ir.decl.ParDeclValue;
import net.opendf.ir.decl.VarDecl;
import net.opendf.ir.entity.PortDecl;
import net.opendf.ir.entity.am.Scope;
import net.opendf.ir.entity.cal.Action;
import net.opendf.ir.entity.cal.Actor;
import net.opendf.ir.entity.cal.InputPattern;
import net.opendf.ir.expr.ExprInput;
import net.opendf.ir.expr.ExprLambda;
import net.opendf.ir.expr.ExprLet;
import net.opendf.ir.expr.ExprLiteral;
import net.opendf.ir.expr.ExprProc;
import net.opendf.ir.expr.Expression;
import net.opendf.ir.stmt.Statement;
import net.opendf.ir.stmt.StmtBlock;
import net.opendf.ir.util.ImmutableList;
import net.opendf.transform.util.AbstractActorTransformer;

class ActorVariableExtractor extends AbstractActorTransformer<ActorVariableExtractor.Variables> {

	public Result extractVariables(Actor actor) {
		Variables data = new Variables();
		Actor resultActor = transformActor(actor, data);
		ImmutableList<Scope> scopes = generateScopes(resultActor);
		ImmutableList<Integer> transientScopes = transientScopes(resultActor);
		return new Result(resultActor, scopes, transientScopes);
	}

	private ImmutableList<Integer> transientScopes(Actor actor) {
		ImmutableList.Builder<Integer> builder = ImmutableList.builder();
		int nbrOfActions = actor.getActions().size() + actor.getInitializers().size();
		for (int i = 1; i <= nbrOfActions; i++) {
			builder.add(i);
		}
		return builder.build();
	}

	private ImmutableList<Scope> generateScopes(Actor actor) {
		ImmutableList.Builder<Scope> result = ImmutableList.builder();
		result.add(new Scope(actor.getVarDecls()));
		ImmutableList<Action> actions = ImmutableList.<Action> builder()
				.addAll(actor.getInitializers())
				.addAll(actor.getActions())
				.build();
		for (Action a : actions) {
			ImmutableList.Builder<LocalVarDecl> builder = ImmutableList.builder();
			int port = 0;
			for (InputPattern in : a.getInputPatterns()) {
				PortDecl portDecl = getPortDecl(actor, port, in);
				port += 1;
				addInputVarDecls(portDecl, in, builder);
			}
			builder.addAll(a.getVarDecls());
			result.add(new Scope(builder.build()));
		}
		return result.build();
	}

	private PortDecl getPortDecl(Actor actor, int port, InputPattern in) {
		if(in.getPort() != null) {
			if (in.getPort().hasLocation()) {
				return actor.getInputPorts().get(in.getPort().getOffset());
			} else {
				for (PortDecl d : actor.getInputPorts()) {
					if (d.getName().equals(in.getPort().getName())) {
						return d;
					}
				}
			}
		} else {
			return actor.getInputPorts().get(port);
		}
		return null;
	}

	private void addInputVarDecls(PortDecl portDecl, InputPattern input, ImmutableList.Builder<LocalVarDecl> builder) {
		int offset = 0;
		for (LocalVarDecl var : input.getVariables()) {
			Expression read;
			if (input.getRepeatExpr() == null) {
				read = new ExprInput(copyPort(input.getPort()), offset);
			} else {
				read = new ExprInput(copyPort(input.getPort()), offset, evalRepeat(input.getRepeatExpr()), input.getVariables().size());
			}
			builder.add(var.copy(null, var.getName(), read, false));
			offset += 1;
		}
	}
	
	private Port copyPort(Port port) {
		if (port.hasLocation()) {
			return new Port(port.getName(), port.getOffset());
		} else {
			return new Port(port.getName());
		}
	}

	private int evalRepeat(Expression expr) {
		return Integer.parseInt(((ExprLiteral) expr).getText());
	}

	@Override
	public Actor transformActor(Actor actor, Variables vars) {
		for (VarDecl var : actor.getVarDecls()) {
			vars.addStaticVar(var.getName());
		}
		Actor result = super.transformActor(actor, vars);
		return result;
	}

	@Override
	public ImmutableList<Action> transformActions(ImmutableList<Action> actions, Variables vars) {
		ImmutableList.Builder<Action> builder = ImmutableList.builder();
		for (Action action : actions) {
			vars = vars.staticFrame();
			builder.add(transformAction(action, vars));
		}
		return builder.build();
	}

	@Override
	public Action transformAction(Action action, Variables vars) {
		for (InputPattern input : action.getInputPatterns()) {
			for (VarDecl var : input.getVariables()) {
				vars.addStaticVar(var.getName());
			}
		}
		for (VarDecl var : action.getVarDecls()) {
			vars.addStaticVar(var.getName());
		}
		return super.transformAction(action, vars);
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
	public LocalVarDecl transformVarDecl(LocalVarDecl varDecl, Variables vars) {
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

	public static class Result {
		public final Actor actor;
		public final ImmutableList<Scope> scopes;
		public final ImmutableList<Integer> transientScopes;

		public Result(Actor actor, ImmutableList<Scope> scopes,
				ImmutableList<Integer> persistentScopes) {
			this.actor = actor;
			this.scopes = scopes;
			this.transientScopes = persistentScopes;
		}
	}

	private static class IntBox {
		public int value;
		public IntBox(int value) {
			this.value = value;
		}
	}
	
	public static class Variables {
		private final Map<String, Integer> staticVarsMap;
		private final Set<String> shaddowedVars;
		private final boolean dynamic;
		private final IntBox scopeNumber;

		public Variables() {
			staticVarsMap = new HashMap<>();
			shaddowedVars = new HashSet<>();
			dynamic = false;
			scopeNumber = new IntBox(0);
		}

		private Variables(Map<String, Integer> staticVarMap, Set<String> shaddowedVars, boolean dynamic,
				IntBox scopeNumber) {
			this.staticVarsMap = new HashMap<>(staticVarMap);
			this.shaddowedVars = new HashSet<>(shaddowedVars);
			this.dynamic = dynamic;
			this.scopeNumber = scopeNumber;
		}

		public void addStaticVar(String name) {
			staticVarsMap.put(name, scopeNumber.value);
		}

		public void declare(String name) {
			if (dynamic) {
				shaddowedVars.add(name);
			}
		}

		public Variable transform(Variable var) {
			String name = var.getName();
			if (!shaddowedVars.contains(name) && staticVarsMap.containsKey(name)) {
				int scope = staticVarsMap.get(name);
				return var.copy(name, scope);
			}
			return var;
		}

		public Variables staticFrame() {
			scopeNumber.value += 1;
			return new Variables(staticVarsMap, shaddowedVars, false, scopeNumber);
		}

		public Variables dynamicFrame() {
			return new Variables(staticVarsMap, shaddowedVars, true, scopeNumber);
		}
	}
}