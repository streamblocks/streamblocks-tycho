package se.lth.cs.tycho.transformation.cal2am;

import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.decoration.TypeToTypeExpr;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.decl.InputVarDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.Match;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.expr.pattern.Pattern;
import se.lth.cs.tycho.ir.expr.pattern.PatternAlias;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstruction;
import se.lth.cs.tycho.ir.expr.pattern.PatternList;
import se.lth.cs.tycho.ir.expr.pattern.PatternVariable;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.attribute.ConstantEvaluator;
import se.lth.cs.tycho.util.BitSets;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Scopes {
	private final CalActor actor;
	private boolean initialized;
	private List<Scope> scopes;
	private BitSet persistentScopes;
	private BitSet transientScopes;
	private final ConstantEvaluator constants;
	private final Types types;

	public Scopes(CalActor actor, ConstantEvaluator constants, Types types) {
		this.actor = actor;
		initialized = false;
		this.constants = constants;
		this.types = types;
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
				for (InputVarDecl var : input.getMatches().stream().map(Match::getDeclaration).collect(Collectors.toList())) {
					varDecls.add(VarDecl.local(var.getType(), var.getName(), new ExprInput((Port) input.getPort().deepClone(), i), var.isConstant()));
					i = i + 1;
				}
				for (PatternVariable var : input.getMatches().stream().filter(match -> match.getExpression() != null).flatMap(match -> variables(match.getExpression().getAlternatives().get(0).getPattern())).collect(Collectors.toList())) {
					varDecls.add(VarDecl.local(TypeToTypeExpr.convert(types.type(var)), var.getVariable().getName(), null, false));
					i = i + 1;
				}
			} else {
				int repeat = (int) constants.intValue(input.getRepeatExpr()).getAsLong();
				int patternLength = input.getMatches().size();
				int i = 0;
				for (InputVarDecl var : input.getMatches().stream().map(Match::getDeclaration).collect(Collectors.toList())) {
					varDecls.add(VarDecl.local(var.getType(), var.getName(), new ExprInput((Port) input.getPort().deepClone(), i, repeat, patternLength), var.isConstant()));
					i = i + 1;
				}
				for (PatternVariable var : input.getMatches().stream().filter(match -> match.getExpression() != null).flatMap(match -> variables(match.getExpression().getAlternatives().get(0).getPattern())).collect(Collectors.toList())) {
					varDecls.add(VarDecl.local(TypeToTypeExpr.convert(types.type(var)), var.getVariable().getName(), null, false));
					i = i + 1;
				}
			}
		}
		varDecls.addAll(action.getVarDecls());
		return new Scope(varDecls.build(), false);
	}

	private Stream<PatternVariable> variables(Pattern pattern) {
		if (pattern instanceof PatternVariable) {
			return Stream.of(((PatternVariable) pattern));
		} else if (pattern instanceof PatternDeconstruction) {
			return ((PatternDeconstruction) pattern).getPatterns().stream().flatMap(this::variables);
		} else if (pattern instanceof PatternList) {
			return ((PatternList) pattern).getPatterns().stream().flatMap(this::variables);
		} else if (pattern instanceof PatternAlias) {
			return variables(((PatternAlias) pattern).getAlias());
		} else {
			return Stream.empty();
		}
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
