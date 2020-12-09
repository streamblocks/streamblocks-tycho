package se.lth.cs.tycho.transformation.cal2am;


import se.lth.cs.tycho.attribute.*;
import se.lth.cs.tycho.decoration.TypeToTypeExpr;
import se.lth.cs.tycho.ir.*;
import se.lth.cs.tycho.ir.decl.InputVarDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.entity.cal.*;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.expr.pattern.*;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtConsume;
import se.lth.cs.tycho.ir.stmt.StmtWrite;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.TreeShadow;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Transitions {
    private final CalActor actor;
    private final Conditions conditions;
    private final ImmutableList<Integer> transientScopes;
    private boolean initialized;
    private ImmutableList<Transition> transitions;
    private Map<Action, Integer> indexMap;
    private final ConstantEvaluator constants;
    private final Types types;
    private final VariableDeclarations variableDecl;
    private final VariableScopes variableScopes;
    private final TreeShadow tree;
    private final Ports ports;

    public Transitions(CalActor actor, ConstantEvaluator constants, Types types, TreeShadow tree, Scopes scopes, Ports ports, Conditions conditions, VariableDeclarations variableDecl, VariableScopes variableScopes) {
        this.actor = actor;
        this.conditions = conditions;
        this.variableDecl = variableDecl;
        this.variableScopes = variableScopes;
        this.transientScopes = scopes.getTransientScopes().stream().boxed().collect(ImmutableList.collector());
        this.constants = constants;
        this.types = types;
        this.tree = tree;
        this.ports = ports;
        this.initialized = false;
    }

    private void init() {
        if (!initialized) {
            this.transitions = actor.getActions().stream()
                    .map(this::actionToTransition)
                    .collect(ImmutableList.collector());
            this.indexMap = new HashMap<>();
            int i = 0;
            for (Action action : actor.getActions()) {
                indexMap.put(action, i++);
            }
            initialized = true;
        }
    }

    private Transition actionToTransition(Action action) {
        ImmutableList.Builder<Annotation> annotations = ImmutableList.builder();
        annotations.addAll(action.getAnnotations());
        // -- Add action tag as an annotation to the transition
        AnnotationParameter actionIdNameParameter = new AnnotationParameter("name", new ExprLiteral(ExprLiteral.Kind.String, action.getTag().toString()));
        Annotation actionId = new Annotation("ActionId", ImmutableList.of(actionIdNameParameter));
        annotations.add(actionId);

        // -- Find input pattern variables that are not used in guards and add them to the LocalVarDecl of the StmtBlock

        ImmutableList.Builder<Statement> builder = ImmutableList.builder();
        builder.addAll(action.getBody());
        addOutputStmts(action.getOutputExpressions(), builder);
        addConsumeStmts(action.getInputPatterns(), builder);

        ImmutableList.Builder<LocalVarDecl> bodyVarDecl = ImmutableList.builder();
        bodyVarDecl.addAll(createBodyLocalVarDecl(action));
        bodyVarDecl.addAll(actionVariablesUsedInGuards(action));

        StmtBlock block = new StmtBlock(ImmutableList.empty(), bodyVarDecl.build(), builder.build());
        return new Transition(annotations.build(), getInputRates(action.getInputPatterns()), getOutputRates(action.getOutputExpressions()), transientScopes, ImmutableList.of(block));
    }

    private ImmutableList<InputPattern> pattersToBeUsedInBody(Action action) {
        ImmutableList.Builder<InputPattern> declarations = ImmutableList.builder();

        List<VarDecl> declsUsedInGuards = new ArrayList<>();
        for (Expression expression : action.getGuards()) {
            declsUsedInGuards.addAll(variableDecl.declarations(expression));
        }

        for (InputPattern pattern : action.getInputPatterns()) {
            List<VarDecl> inputPatternDecls = new ArrayList<>();
            inputPatternDecls.addAll(variableScopes.declarations(pattern));

            if (Collections.disjoint(inputPatternDecls, declsUsedInGuards)) {
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
            if(!declsUsedInGuards.contains(decl)){
                decls.add(decl);
            }else{
                System.out.println("oops");
            }
        }
        return decls.build();
    }

    ImmutableList<LocalVarDecl> createBodyLocalVarDecl(Action action) {
        ImmutableList<InputPattern> bodyVarDecl = pattersToBeUsedInBody(action);

        ImmutableList.Builder<LocalVarDecl> varDecls = ImmutableList.builder();
        for (InputPattern input : action.getInputPatterns()) {
            if (bodyVarDecl.contains(input)) {
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
        return varDecls.build();
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

    private Map<Port, Integer> getOutputRates(ImmutableList<OutputExpression> outputExpressions) {
        return outputExpressions.stream()
                .map(conditions::getCondition)
                .map(PortCondition::deepClone)
                .collect(Collectors.toMap(PortCondition::getPortName, PortCondition::N));
    }

    private Map<Port, Integer> getInputRates(ImmutableList<InputPattern> inputPatterns) {
        return inputPatterns.stream()
                .map(conditions::getCondition)
                .map(PortCondition::deepClone)
                .collect(Collectors.toMap(PortCondition::getPortName, PortCondition::N));
    }

    private void addConsumeStmts(ImmutableList<InputPattern> inputPatterns, Consumer<Statement> builder) {
        inputPatterns.stream()
                .map(conditions::getCondition)
                .map(cond -> new StmtConsume((Port) cond.getPortName().deepClone(), cond.N()))
                .forEach(builder);
    }

    private void addOutputStmts(ImmutableList<OutputExpression> outputExpressions, Consumer<Statement> builder) {
        outputExpressions.stream()
                .map(output -> new StmtWrite((Port) output.getPort().deepClone(), output.getExpressions(), output.getRepeatExpr()))
                .forEach(builder);
    }

    public List<Transition> getAllTransitions() {
        init();
        return transitions;
    }

    public Transition getTransition(Action action) {
        init();
        return transitions.get(getTransitionIndex(action));
    }

    public int getTransitionIndex(Action action) {
        init();
        if (indexMap.containsKey(action)) {
            return indexMap.get(action);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
