package se.lth.cs.tycho.transform.caltoam;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.lth.cs.tycho.instance.am.Scope;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.transform.util.AbstractActorTransformer;

class ActorVariableExtractor extends AbstractActorTransformer<ActorVariableExtractor.Variables> {

	public Result extractVariables(CalActor calActor, NamespaceDecl location) {
		Variables data = new Variables();
		CalActor resultActor = transformActor(calActor, data);
		ImmutableList<Scope> scopes = generateScopes(resultActor, location);
		ImmutableList<Integer> transientScopes = transientScopes(resultActor);
		return new Result(resultActor, scopes, transientScopes);
	}

	private ImmutableList<Integer> transientScopes(CalActor calActor) {
		ImmutableList.Builder<Integer> builder = ImmutableList.builder();
		int nbrOfActions = calActor.getActions().size() + calActor.getInitializers().size();
		for (int i = 1; i <= nbrOfActions; i++) {
			builder.add(i);
		}
		return builder.build();
	}

	private ImmutableList<Scope> generateScopes(CalActor calActor, NamespaceDecl location) {
		ImmutableList.Builder<Scope> result = ImmutableList.builder();
		result.add(new Scope(calActor.getVarDecls(), true));
		ImmutableList<Action> actions = ImmutableList.<Action> builder()
				.addAll(calActor.getInitializers())
				.addAll(calActor.getActions())
				.build();
		for (Action a : actions) {
			ImmutableList.Builder<VarDecl> builder = ImmutableList.builder();
			int port = 0;
			for (InputPattern in : a.getInputPatterns()) {
				PortDecl portDecl = getPortDecl(calActor, port, in);
				port += 1;
				addInputVarDecls(portDecl, in, builder);
			}
			builder.addAll(a.getVarDecls());
			result.add(new Scope(builder.build(), false));
		}
		return result.build();
	}

	private PortDecl getPortDecl(CalActor calActor, int port, InputPattern in) {
		if(in.getPort() != null) {
			if (in.getPort().hasLocation()) {
				return calActor.getInputPorts().get(in.getPort().getOffset());
			} else {
				for (PortDecl d : calActor.getInputPorts()) {
					if (d.getName().equals(in.getPort().getName())) {
						return d;
					}
				}
			}
		} else {
			return calActor.getInputPorts().get(port);
		}
		return null;
	}

	private void addInputVarDecls(PortDecl portDecl, InputPattern input, ImmutableList.Builder<VarDecl> builder) {
		int offset = 0;
		for (VarDecl var : input.getVariables()) {
			Expression read;
			if (input.getRepeatExpr() == null) {
				read = new ExprInput(copyPort(input.getPort()), offset);
			} else {
				read = new ExprInput(copyPort(input.getPort()), offset, evalRepeat(input.getRepeatExpr()), input.getVariables().size());
			}
			builder.add(var.copyAsLocal(null, var.getName(), true, read));
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
		if (expr instanceof ExprLiteral) {
			return Integer.parseInt(((ExprLiteral) expr).getText());
		} else {
			throw new Error("Repeat expressions must be integer literals");
		}
	}

	@Override
	public CalActor transformActor(CalActor calActor, Variables vars) {
		for (VarDecl var : calActor.getVarDecls()) {
			vars.addStaticVar(var.getName());
		}
		CalActor result = super.transformActor(calActor, vars);
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
	public VarDecl transformVarDecl(VarDecl varDecl, Variables vars) {
		vars.declare(varDecl.getName());
		return super.transformVarDecl(varDecl, vars);
	}

	@Override
	public VarDecl transformValueParameter(VarDecl valueParam, Variables vars) {
		vars.declare(valueParam.getName());
		return super.transformValueParameter(valueParam, vars);
	}

	@Override
	public Variable transformVariable(Variable var, Variables vars) {
		return vars.transform(var);
	}

	public static class Result {
		public final CalActor calActor;
		public final ImmutableList<Scope> scopes;
		public final ImmutableList<Integer> transientScopes;

		public Result(CalActor calActor, ImmutableList<Scope> scopes,
				ImmutableList<Integer> persistentScopes) {
			this.calActor = calActor;
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
