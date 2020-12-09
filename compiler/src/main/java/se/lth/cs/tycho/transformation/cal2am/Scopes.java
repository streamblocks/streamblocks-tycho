package se.lth.cs.tycho.transformation.cal2am;

import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.attribute.VariableScopes;
import se.lth.cs.tycho.decoration.TypeToTypeExpr;
import se.lth.cs.tycho.ir.IRNode;
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
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.expr.pattern.Pattern;
import se.lth.cs.tycho.ir.expr.pattern.PatternAlias;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstruction;
import se.lth.cs.tycho.ir.expr.pattern.PatternList;
import se.lth.cs.tycho.ir.expr.pattern.PatternTuple;
import se.lth.cs.tycho.ir.expr.pattern.PatternVariable;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.attribute.ConstantEvaluator;
import se.lth.cs.tycho.phase.TreeShadow;
import se.lth.cs.tycho.util.BitSets;

import java.util.*;
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
    private final TreeShadow tree;
    private final VariableDeclarations variableDecl;
    private final VariableScopes variableScopes;

    public Scopes(CalActor actor, ConstantEvaluator constants, Types types, TreeShadow tree, VariableDeclarations variableDecl, VariableScopes variableScopes) {
        this.actor = actor;
        initialized = false;
        this.constants = constants;
        this.types = types;
        this.tree = tree;
        this.variableDecl = variableDecl;
        this.variableScopes = variableScopes;
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

    private ImmutableList<InputPattern> pattersToBeUsedInScope(Action action) {
        ImmutableList.Builder<InputPattern> declarations = ImmutableList.builder();

        List<VarDecl> declsUsedInGuards = new ArrayList<>();
        for (Expression expression : action.getGuards()) {
            declsUsedInGuards.addAll(variableDecl.declarations(expression));
        }

        for (InputPattern pattern : action.getInputPatterns()) {
            List<VarDecl> inputPatternDecls = new ArrayList<>();
            inputPatternDecls.addAll(variableScopes.declarations(pattern));

            if (!Collections.disjoint(inputPatternDecls, declsUsedInGuards)) {
                declarations.add(pattern);
            }
        }

        return declarations.build();
    }

    private ImmutableList<LocalVarDecl> actionVariablesUsedInGuards(Action action){
        ImmutableList.Builder<LocalVarDecl> decls = ImmutableList.builder();
        List<VarDecl> declsUsedInGuards = new ArrayList<>();
        for (Expression expression : action.getGuards()) {
            declsUsedInGuards.addAll(variableDecl.declarations(expression));
        }

        for(LocalVarDecl decl : action.getVarDecls()){
            if(declsUsedInGuards.contains(decl)){
                decls.add(decl);
            }
        }
        return decls.build();
    }

    private Scope createScope(Action action) {
        ImmutableList<InputPattern> patternsUsedInScopes = pattersToBeUsedInScope(action);

        ImmutableList.Builder<LocalVarDecl> varDecls = ImmutableList.builder();
        for (InputPattern input : action.getInputPatterns()) {
            if (patternsUsedInScopes.contains(input)) {
                if (input.getRepeatExpr() == null) {
                    int i = 0;
                    for (InputVarDecl var : input.getMatches().stream().map(Match::getDeclaration).collect(Collectors.toList())) {
                        varDecls.add(VarDecl.local(var.getAnnotations(), var.getType(), var.getName(), new ExprInput((Port) input.getPort().deepClone(), i), var.isConstant()));
                        i = i + 1;
                    }
                    for (PatternVariable var : input.getMatches().stream().filter(match -> match.getExpression() != null).flatMap(match -> variables(match.getExpression().getAlternatives().get(0).getPattern())).collect(Collectors.toList())) {
                        varDecls.add(VarDecl.local(ImmutableList.empty(), TypeToTypeExpr.convert(types.type(var)), var.getVariable().getName(), value(var), false));
                        i = i + 1;
                    }
                } else {
                    int repeat = (int) constants.intValue(input.getRepeatExpr()).getAsLong();
                    int patternLength = input.getMatches().size();
                    int i = 0;
                    for (InputVarDecl var : input.getMatches().stream().map(Match::getDeclaration).collect(Collectors.toList())) {
                        varDecls.add(VarDecl.local(var.getAnnotations(), var.getType(), var.getName(), new ExprInput((Port) input.getPort().deepClone(), i, repeat, patternLength), var.isConstant()));
                        i = i + 1;
                    }
                    for (PatternVariable var : input.getMatches().stream().filter(match -> match.getExpression() != null).flatMap(match -> variables(match.getExpression().getAlternatives().get(0).getPattern())).collect(Collectors.toList())) {
                        varDecls.add(VarDecl.local(ImmutableList.empty(), TypeToTypeExpr.convert(types.type(var)), var.getVariable().getName(), value(var), false));
                        i = i + 1;
                    }
                }
            }
        }

        varDecls.addAll(actionVariablesUsedInGuards(action));

        return new Scope(varDecls.build(), false);
    }

    private Stream<PatternVariable> variables(Pattern pattern) {
        if (pattern instanceof PatternVariable) {
            return Stream.of(((PatternVariable) pattern));
        } else if (pattern instanceof PatternDeconstruction) {
            return ((PatternDeconstruction) pattern).getPatterns().stream().flatMap(this::variables);
        } else if (pattern instanceof PatternList) {
            return ((PatternList) pattern).getPatterns().stream().flatMap(this::variables);
        } else if (pattern instanceof PatternTuple) {
            return ((PatternTuple) pattern).getPatterns().stream().flatMap(this::variables);
        } else if (pattern instanceof PatternAlias) {
            return variables(((PatternAlias) pattern).getAlias());
        } else {
            return Stream.empty();
        }
    }

    private Expression value(PatternVariable pattern) {
        IRNode parent = tree.parent(pattern);
        if (parent instanceof PatternAlias) {
            return ((PatternAlias) parent).getExpression();
        }
        return null;
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
