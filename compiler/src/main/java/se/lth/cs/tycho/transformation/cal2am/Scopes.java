package se.lth.cs.tycho.transformation.cal2am;

import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.decl.InputVarDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.attribute.ConstantEvaluator;
import se.lth.cs.tycho.util.BitSets;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Scopes {
	private final CalActor actor;
	private boolean initialized;
	private List<Scope> scopes;
	private BitSet persistentScopes;
	private BitSet transientScopes;
	private final ConstantEvaluator constants;

	public Scopes(CalActor actor, ConstantEvaluator constants) {
		this.actor = actor;
		initialized = false;
		this.constants = constants;
	}

	private void init() {
		if (!initialized) {
			initialized = true;
			scopes = new ArrayList<>();

			scopes.add(new Scope(actor.getVarDecls(), true));
			persistentScopes = new BitSet();
			persistentScopes.set(0);

			for (Action action : actor.getActions()) {
				Scope scope = createScope(action);
				if (!scope.getDeclarations().isEmpty()) {
					scopes.add(scope);
				}
			}
			transientScopes = new BitSet();
			transientScopes.set(1, scopes.size());
		}
	}

	private Scope createScope(Action action) {
		ImmutableList.Builder<LocalVarDecl> varDecls = ImmutableList.builder();
		for (InputPattern input : action.getInputPatterns()) {
			if (input.getRepeatExpr() == null) {
				int i = 0;
				for (InputVarDecl var : input.getVariables()) {
					varDecls.add(VarDecl.local(var.getType(), var.getName(), new ExprInput((Port) input.getPort().deepClone(), i), var.isConstant()));
					i = i + 1;
				}
			} else {
				int repeat = (int) constants.intValue(input.getRepeatExpr()).getAsLong();
				int patternLength = input.getVariables().size();
				int i = 0;
				for (InputVarDecl var : input.getVariables()) {
					varDecls.add(VarDecl.local(var.getType(), var.getName(), new ExprInput((Port) input.getPort().deepClone(), i, repeat, patternLength), var.isConstant()));
					i = i + 1;
				}
			}
		}
		varDecls.addAll(action.getVarDecls());
		return new Scope(varDecls.build(), false);
	}

	public List<Scope> getScopes() {
		init();
		return scopes;
	}

	public BitSet getPersistentScopes() {
		init();
		return BitSets.copyOf(persistentScopes);
	}

	public BitSet getTransientScopes() {
		init();
		return BitSets.copyOf(transientScopes);
	}

}
