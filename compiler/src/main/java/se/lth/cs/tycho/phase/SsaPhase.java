package se.lth.cs.tycho.phase;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.stmt.*;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.ir.stmt.ssa.*;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.util.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static se.lth.cs.tycho.ir.Variable.variable;

/**
 * The Ssa phase.
 */
public class SsaPhase implements Phase {

    private static final CollectOrReplaceExprInStmt stmtExprCollector = MultiJ.from(CollectOrReplaceExprInStmt.class).instance();
    private static final CollectOrReplaceExpressions subExprCollectorOrReplacer = MultiJ.from(CollectOrReplaceExpressions.class).instance();
    private static final CollectOrReplaceSubStmts subStmtCollector = MultiJ.from(CollectOrReplaceSubStmts.class).instance();
    private static final CreateControlFlowGraphBlocks cfgCreator = MultiJ.from(CreateControlFlowGraphBlocks.class).instance();
    private static VariableDeclarations declarations = null;
    private static final List<LocalVarDecl> localVarDecl = new LinkedList<>();
    private static final Set<String> alreadyAddedPhis = new HashSet<>();
    private static final Pair<Integer, Optional<StmtLabeledSSA>> noStoppingCond = new Pair<>(0, Optional.empty());
    private static boolean cfgOnly;

    /**
     * Instantiates a new Ssa phase.
     *
     * @param cfgOnly only build cfg
     */
    public SsaPhase(boolean cfgOnly) {
        SsaPhase.cfgOnly = cfgOnly;
    }

    @Override
    public String getDescription() {
        return "Creates a CFG and optionally applies SSA transformation";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        declarations = task.getModule(VariableDeclarations.key);
        Transformation transformation = MultiJ.from(SsaPhase.Transformation.class).instance();
        return task.transformChildren(transformation);
    }

    //----------------------------------------------------------------Statement and Expression Collection/Replacement ---------------------------------------------------------------//

    /**
     * The interface used to collect or replace Statements inside another Statement.
     */
    @Module
    interface CollectOrReplaceSubStmts {

        /**
         * Collect list.
         *
         * @param s the Statement to collect from
         * @return a List containing Lists of Statements
         */
        default List<LinkedList<Statement>> collect(Statement s) {
            return new LinkedList<>(new LinkedList<>());
        }

        default List<LinkedList<Statement>> collect(StmtBlock s) {
            List<LinkedList<Statement>> res = new LinkedList<>();
            res.add(new LinkedList<>(s.getStatements()));
            return res;
        }

        default List<LinkedList<Statement>> collect(StmtWhile s) {
            List<LinkedList<Statement>> res = new LinkedList<>();
            res.add(new LinkedList<>(s.getBody()));
            return res;
        }

        default List<LinkedList<Statement>> collect(StmtForeach s) {
            List<LinkedList<Statement>> res = new LinkedList<>();
            res.add(new LinkedList<>(s.getBody()));
            return res;
        }

        default List<LinkedList<Statement>> collect(StmtIf s) {
            List<LinkedList<Statement>> res = new LinkedList<>();
            res.add(new LinkedList<>(s.getThenBranch()));
            res.add(new LinkedList<>(s.getElseBranch()));
            return res;
        }

        default List<LinkedList<Statement>> collect(StmtCase s) {
            List<LinkedList<Statement>> res = new LinkedList<>();
            s.getAlternatives().forEach(alt -> res.add(new LinkedList<>(alt.getStatements())));
            return res;
        }

        default List<LinkedList<Statement>> collect(StmtIfSSA s) {
            List<LinkedList<Statement>> res = new LinkedList<>();
            res.add(new LinkedList<>(s.getThenBranch()));
            res.add(new LinkedList<>(s.getElseBranch()));
            return res;
        }

        default List<LinkedList<Statement>> collect(StmtWhileSSA s) {
            List<LinkedList<Statement>> res = new LinkedList<>();
            res.add(new LinkedList<>(s.getBody()));
            return res;
        }

        /**
         * Replace statement.
         *
         * @param s the statement to have its body replace
         * @param l the new body containing the replaced Statements
         * @return the statement with the updated body
         */
        default Statement replace(Statement s, List<List<Statement>> l) {
            return s;
        }

        default Statement replace(StmtBlock block, List<List<Statement>> newBody) {
            return (Statement) block.withStatements(newBody.get(0));
        }

        default Statement replace(StmtWhile whilee, List<List<Statement>> newBody) {
            return (Statement) whilee.withBody(newBody.get(0));
        }

        default Statement replace(StmtForeach foreach, List<List<Statement>> newBody) {
            return (Statement) foreach.withBody(newBody.get(0));
        }

        default Statement replace(StmtIf iff, List<List<Statement>> newBody) {
            if (newBody.size() < 2) {
                throw new IllegalArgumentException("too few statement list given");
            }
            return (Statement) iff.withThenBranch(newBody.get(0)).withElseBranch(newBody.get(1));
        }

        default Statement replace(StmtCase casee, List<List<Statement>> newBody) {
            if (newBody.size() != casee.getAlternatives().size()) {
                throw new IllegalArgumentException("too few statement lists given");
            }
            AtomicInteger i = new AtomicInteger();
            List<StmtCase.Alternative> newAlts = casee.getAlternatives().stream().map(alt -> alt.copy(alt.getPattern(), alt.getGuards(), newBody.get(i.getAndIncrement()))).collect(Collectors.toList());
            return casee.copy(casee.getScrutinee(), newAlts);
        }

        default Statement replace(StmtIfSSA iff, List<List<Statement>> newBody) {
            if (newBody.size() < 2) {
                throw new IllegalArgumentException("too few statement list given");
            }
            return iff.withThenBranch(newBody.get(0)).withElseBranch(newBody.get(1));
        }

        default Statement replace(StmtWhileSSA whilee, List<List<Statement>> newBody) {
            return whilee.withBody(newBody.get(0));
        }

    }

    /**
     * The interface used to collect or replace Expressions in a Statement.
     */
    @Module
    interface CollectOrReplaceExprInStmt {

        /**
         * Collect multiple Expressions if a Statement contains multiple Expressions of the same type as well as other Expressions in other fields
         *
         * @param s the Statement
         * @return a pair composed of the list of similar Expressions and the unique one
         */
        Pair<List<Expression>, Expression> collectListAndSinglExpr(Statement s);

        default Pair<List<? extends IRNode>, Expression> collectMultipleExpr(StmtCall call) {
            return new Pair<>(call.getArgs(), call.getProcedure());
        }

        /**
         * Collect a List containing all the expression in a Statement.
         *
         * @param s the Statement
         * @return the List of Expressions
         */
        default List<Expression> collect(Statement s) {
            return new LinkedList<>();
        }

        default List<Expression> collect(StmtCall call) {
            return new LinkedList<>(call.getArgs());
        }

        default List<Expression> collect(StmtIf iff) {
            return new LinkedList<>(Collections.singletonList(iff.getCondition()));
        }

        default List<Expression> collect(StmtAssignment assignment) {
            return new LinkedList<>(Collections.singletonList(assignment.getExpression()));
        }

        default List<Expression> collect(StmtCase casee) {
            return new LinkedList<>(Collections.singletonList(casee.getScrutinee()));
        }

        default List<Expression> collect(StmtForeach forEach) {
            return new LinkedList<>(forEach.getFilters());
        }

        default List<Expression> collect(StmtReturn ret) {
            return new LinkedList<>(Collections.singletonList(ret.getExpression()));
        }

        default List<Expression> collect(StmtWhile whilee) {
            return new LinkedList<>(Collections.singletonList(whilee.getCondition()));
        }

        default List<Expression> collect(StmtWrite write) {
            List<Expression> e = new LinkedList<>(write.getValues());
            e.add(write.getRepeatExpression());
            return e;
        }

        default Pair<List<Expression>, Expression> collectListAndSinglExpr(StmtWrite write) {
            return new Pair<>(new LinkedList<>(write.getValues()), write.getRepeatExpression());
        }

        /**
         * Replace a single Expression in a Statement.
         *
         * @param s the Statement
         * @param e the Expression
         * @return the updated Statement
         */
        Statement replaceSingleExpr(Statement s, Expression e);

        /**
         * Replace a List of Expressions in a statement.
         *
         * @param s the Statement
         * @param e the Expression's List
         * @return the updated Statement
         */
        Statement replaceListExpr(Statement s, List<Expression> e);

        Statement replaceListAndSingleExpr(Statement s, List<Expression> le, Expression e);

        default Statement replaceSingleExpr(StmtReturn ret, Expression retVal) {
            return (Statement) ret.copy(retVal);
        }

        default Statement replaceSingleExpr(StmtWhile whilee, Expression cond) {
            return (Statement) whilee.withCondition(cond);
        }

        default Statement replaceSingleExpr(StmtIf iff, Expression condition) {
            return (Statement) iff.withCondition(condition);
        }

        default Statement replaceSingleExpr(StmtCase casee, Expression scrut) {
            return (Statement) casee.copy(scrut, casee.getAlternatives());
        }

        default Statement replaceListExpr(StmtForeach foreach, List<Expression> filters) {
            return (Statement) foreach.withFilters(filters);
        }

        default Statement replaceListExpr(StmtCall call, List<Expression> args) {
            return (Statement) call.copy(call.getProcedure(), args);
        }

        default Statement replaceListAndSingleExpr(StmtWrite write, List<Expression> args, Expression repeatExpr) {
            return (Statement) write.copy(write.getPort(), args, repeatExpr);
        }
    }

    /**
     * The interface used to collect the Expressions contained in another Expression.
     */
    @Module
    interface CollectOrReplaceExpressions {

        /**
         * Collect all the internal Expressions of an Expression.
         *
         * @param e the Expression
         * @return the List of all contained Expressions
         */
        default List<Expression> collectInternalExpr(Expression e) {
            return new LinkedList<>();
        }

        default List<Expression> collectInternalExpr(ExprApplication app) {
            return new LinkedList<>(app.getArgs());
        }

        default List<Expression> collectInternalExpr(ExprBinaryOp bin) {
            return new LinkedList<>(bin.getOperands());
        }

        default List<Expression> collectInternalExpr(ExprCase casee) {
            return new LinkedList<>(Collections.singletonList(casee.getScrutinee()));
        }

        default List<Expression> collectInternalExpr(ExprCase.Alternative alt) {
            return new LinkedList<>(Collections.singletonList(alt.getExpression()));
        }

        default List<Expression> collectInternalExpr(ExprComprehension comp) {
            List<Expression> expr = new LinkedList<>(comp.getFilters());
            expr.add(comp.getCollection());
            return expr;
        }

        default List<Expression> collectInternalExpr(ExprDeref deref) {
            return new LinkedList<>(Collections.singletonList(deref.getReference()));
        }

        default List<Expression> collectInternalExpr(ExprIf eif) {
            return new LinkedList<>(Arrays.asList(eif.getCondition(), eif.getElseExpr(), eif.getThenExpr()));
        }

        default List<Expression> collectInternalExpr(ExprLambda lambda) {
            return new LinkedList<>(Collections.singletonList(lambda.getBody()));
        }

        default List<Expression> collectInternalExpr(ExprList list) {
            return new LinkedList<>(list.getElements());
        }

        default List<Expression> collectInternalExpr(ExprSet set) {
            return new LinkedList<>(set.getElements());
        }

        default List<Expression> collectInternalExpr(ExprTuple tuple) {
            return new LinkedList<>(tuple.getElements());
        }

        default List<Expression> collectInternalExpr(ExprTypeConstruction typeConstr) {
            return new LinkedList<>(typeConstr.getArgs());
        }

        default List<Expression> collectInternalExpr(ExprUnaryOp unary) {
            return new LinkedList<>(Collections.singletonList(unary.getOperand()));
        }

        default List<Expression> collectInternalExpr(ExprVariable var) {
            return new LinkedList<>();
        }

        default List<Expression> collectInternalExpr(ExprField field) {
            return new LinkedList<>(Collections.singletonList(field.getStructure()));
        }

        default List<Expression> collectInternalExpr(ExprNth nth) {
            return new LinkedList<>(Collections.singletonList(nth.getStructure()));
        }

        default List<Expression> collectInternalExpr(ExprTypeAssertion typeAssert) {
            return new LinkedList<>(Collections.singletonList(typeAssert.getExpression()));
        }

        default List<Expression> collectInternalExpr(ExprIndexer indexer) {
            return new LinkedList<>(Arrays.asList(indexer.getStructure(), indexer.getStructure()));
        }

        default List<Expression> collectInternalExpr(ExprLet let) {
            return new LinkedList<>(Collections.singletonList(let.getBody()));
        }

        default List<Expression> collectInternalExpr(ExprMap map) {
            ImmutableList<ImmutableEntry<Expression, Expression>> mappings = map.getMappings();
            List<Expression> result = new LinkedList<>();
            mappings.forEach(entry -> result.addAll(Arrays.asList(entry.getKey(), entry.getValue())));
            return result;
        }

        default List<Expression> collectInternalExpr(ExprLiteral lit) {
            return new LinkedList<>();
        }

        /**
         * Gets local var decl.
         *
         * @param originalVarName the original var name
         * @param localVarDecls   the local var decls
         * @return the local var decl
         */
        default LocalVarDecl getLocalVarDecl(String originalVarName, Set<LocalVarDecl> localVarDecls) {
            for (LocalVarDecl lvd : localVarDecls) {
                if (lvd.getOriginalName().equals(originalVarName)) {
                    return lvd;
                }
            }
            throw new IllegalStateException("Missing ssa result for given variable");
        }

        /**
         * Replace all variables inside an Expression.
         *
         * @param original     the original Expression
         * @param replacements the Map containing the replacement for each Variable
         * @return the updated Expression
         */
        default Expression replaceExprVar(Expression original, Map<ExprVariable, LocalVarDecl> replacements) {
            return (original != null) ? original : null;
        }

        default Expression replaceExprVar(ExprVariable var, Map<ExprVariable, LocalVarDecl> replacements) {
            //check that var is contained in the new mapping
            if (replacements.containsKey(var)) {
                return var.copy(variable(replacements.get(var).getName()), var.getOld()).deepClone();
            } else if (!functionScopeVars.containsKey(var.getVariable().getOriginalName())) {
                return var.deepClone();
            } else {
                throw new IllegalStateException("Local Value Numbering missed this variable or the replacement mapping argument is incomplete");
            }
        }

        default Expression replaceExprVar(ExprIf iff, Map<ExprVariable, LocalVarDecl> replacements) {
            Expression cond = replaceExprVar(iff.getCondition(), replacements);
            Expression then = replaceExprVar(iff.getThenExpr(), replacements);
            Expression elze = replaceExprVar(iff.getElseExpr(), replacements);
            return new ExprIf(cond, then, elze);
        }

        default Expression replaceExprVar(ExprApplication app, Map<ExprVariable, LocalVarDecl> replacements) {
            List<Expression> args = new LinkedList<>(app.getArgs());
            args.replaceAll(arg -> replaceExprVar(arg, replacements));
            return new ExprApplication(app.getFunction(), ImmutableList.from(args));
        }

        default Expression replaceExprVar(ExprBinaryOp binOp, Map<ExprVariable, LocalVarDecl> replacements) {
            List<Expression> newOp = subExprCollectorOrReplacer.collectInternalExpr(binOp);
            newOp.replaceAll(op -> replaceExprVar(op, replacements));
            return new ExprBinaryOp(binOp.getOperations(), ImmutableList.from(newOp));
        }

        default Expression replaceExprVar(ExprCase casee, Map<ExprVariable, LocalVarDecl> replacements) {
            List<Expression> scrut = subExprCollectorOrReplacer.collectInternalExpr(casee);
            Expression newScrut = replaceExprVar(scrut.get(0), replacements);

            List<ExprCase.Alternative> alts = casee.getAlternatives();
            alts.replaceAll(alt -> new ExprCase.Alternative(alt.getPattern(), alt.getGuards(), replaceExprVar(subExprCollectorOrReplacer.collectInternalExpr(alt).get(0), replacements)));

            return new ExprCase(newScrut, alts);
        }

        default Expression replaceExprVar(ExprComprehension comp, Map<ExprVariable, LocalVarDecl> replacements) {
            Expression collection = replaceExprVar(comp.getCollection(), replacements);
            List<Expression> filters = comp.getFilters();
            filters.replaceAll(f -> replaceExprVar(f, replacements));
            return comp.copy(comp.getGenerator(), filters, collection).deepClone();
        }

        default Expression replaceExprVar(ExprDeref deref, Map<ExprVariable, LocalVarDecl> replacements) {
            return deref.withReference(replaceExprVar(deref.getReference(), replacements)).deepClone();
        }

        default Expression replaceExprVar(ExprLambda lambda, Map<ExprVariable, LocalVarDecl> replacements) {
            return lambda.copy(lambda.getValueParameters(), replaceExprVar(lambda.getBody(), replacements), lambda.getReturnType()).deepClone();
        }

        default Expression replaceExprVar(ExprList list, Map<ExprVariable, LocalVarDecl> replacements) {
            List<Expression> elems = list.getElements();
            elems.replaceAll(e -> replaceExprVar(e, replacements));
            return list.withElements(elems).deepClone();
        }

        default Expression replaceExprVar(ExprSet set, Map<ExprVariable, LocalVarDecl> replacements) {
            List<Expression> elems = set.getElements();
            elems.replaceAll(e -> replaceExprVar(e, replacements));
            return set.withElements(elems).deepClone();
        }

        default Expression replaceExprVar(ExprTuple tuple, Map<ExprVariable, LocalVarDecl> replacements) {
            List<Expression> elems = tuple.getElements();
            elems.replaceAll(e -> replaceExprVar(e, replacements));
            return tuple.copy(elems).deepClone();
        }

        default Expression replaceExprVar(ExprTypeConstruction typeConstruction, Map<ExprVariable, LocalVarDecl> replacements) {
            List<Expression> elems = typeConstruction.getArgs();
            elems.replaceAll(e -> replaceExprVar(e, replacements));
            return typeConstruction.copy(typeConstruction.getConstructor(), typeConstruction.getTypeParameters(), typeConstruction.getValueParameters(), elems).deepClone();
        }

        default Expression replaceExprVar(ExprUnaryOp unOp, Map<ExprVariable, LocalVarDecl> replacements) {
            return unOp.copy(unOp.getOperation(), replaceExprVar(unOp.getOperand(), replacements)).deepClone();
        }

        default Expression replaceExprVar(ExprField field, Map<ExprVariable, LocalVarDecl> replacements) {
            return field.copy(replaceExprVar(field.getStructure(), replacements), field.getField()).deepClone();
        }

        default Expression replaceExprVar(ExprNth nth, Map<ExprVariable, LocalVarDecl> replacements) {
            return nth.copy(replaceExprVar(nth.getStructure(), replacements), nth.getNth()).deepClone();
        }

        default Expression replaceExprVar(ExprTypeAssertion exprTypeAssertion, Map<ExprVariable, LocalVarDecl> replacements) {
            return exprTypeAssertion.copy(replaceExprVar(exprTypeAssertion.getExpression(), replacements), exprTypeAssertion.getType()).deepClone();
        }

        default Expression replaceExprVar(ExprIndexer indexer, Map<ExprVariable, LocalVarDecl> replacements) {
            Expression newStruct = replaceExprVar(indexer.getStructure(), replacements);
            Expression newIndex = replaceExprVar(indexer.getIndex(), replacements);
            return indexer.copy(newStruct, newIndex).deepClone();
        }

        default Expression replaceExprVarLet(ExprLet let, Map<LocalVarDecl, Boolean> replacements) {
            List<LocalVarDecl> lvd = let.getVarDecls();
            List<LocalVarDecl> newLvd = lvd.stream().map(lv -> getLocalVarDecl(lv.getOriginalName(), replacements.keySet())).collect(Collectors.toList());
            return let.withVarDecls(newLvd).deepClone();
        }

        default Expression replaceExprVar(ExprMap map, Map<ExprVariable, LocalVarDecl> replacements) {
            ImmutableList<ImmutableEntry<Expression, Expression>> mappings = map.getMappings();
            ImmutableList<ImmutableEntry<Expression, Expression>> newMappings = ImmutableList.empty();

            for (ImmutableEntry<Expression, Expression> entry : mappings) {
                ImmutableEntry<Expression, Expression> newEntry = ImmutableEntry.of(replaceExprVar(entry.getKey(), replacements), replaceExprVar(entry.getValue(), replacements));
                newMappings.add(newEntry);
            }
            return map.withMappings(newMappings).deepClone();
        }
    }


    /**
     * The interface used to create control flow graph blocks.
     */
    @Module
    interface CreateControlFlowGraphBlocks {

        /**
         * Transforms a Statement into a StatementLabeledSSA, connecting it with its sub Statements and creating buffer blocks if needed
         *
         * @param stmt            the original stmt
         * @param exitBlock       the exit block of the cfg
         * @param nestedLoopLevel the nested loop level
         * @return the stmt labeled ssa
         */
        StmtLabeledSSA createBlock(Statement stmt, StmtLabeledSSA exitBlock, int nestedLoopLevel);

        default StmtLabeledSSA createBlock(StmtConsume stmt, StmtLabeledSSA exitBlock, int nestedLoopLevel) {
            return new StmtLabeledSSA(assignLabel(stmt), stmt, nestedLoopLevel);
        }

        default StmtLabeledSSA createBlock(StmtWrite stmt, StmtLabeledSSA exitBlock, int nestedLoopLevel) {
            return new StmtLabeledSSA(assignLabel(stmt), stmt, nestedLoopLevel);
        }

        default StmtLabeledSSA createBlock(StmtRead stmt, StmtLabeledSSA exitBlock, int nestedLoopLevel) {
            return new StmtLabeledSSA(assignLabel(stmt), stmt, nestedLoopLevel);
        }

        default StmtLabeledSSA createBlock(StmtWhile stmt, StmtLabeledSSA exitBlock, int nestedLoopLevel) {
            List<Statement> body = subStmtCollector.collect(stmt).get(0);
            LinkedList<StmtLabeledSSA> currentBlocks = iterateSubStmts(body, exitBlock, nestedLoopLevel + 1);

            StmtLabeledSSA stmtWhileLabeled = new StmtLabeledSSA(assignLabel(stmt), stmt, nestedLoopLevel);

            //Add the while stmt as both predecessors and successor of its body
            wireRelations(currentBlocks, stmtWhileLabeled, stmtWhileLabeled);

            StmtLabeledSSA entryWhile = new StmtLabeledSSA(assignBufferLabel(stmt, true), emptyStmtBlock(), nestedLoopLevel);
            StmtLabeledSSA exitWhile = new StmtLabeledSSA(assignBufferLabel(stmt, false), emptyStmtBlock(), nestedLoopLevel);

            wireRelations(new LinkedList<>(Collections.singletonList(stmtWhileLabeled)), entryWhile, exitWhile);
            entryWhile.setShortCutToExit(stmtWhileLabeled);
            exitWhile.setShortCutToExit(stmtWhileLabeled);

            return entryWhile;
        }

        default StmtLabeledSSA createBlock(StmtIf stmt, StmtLabeledSSA exitBlock, int nestedLoopLevel) {

            StmtLabeledSSA stmtIfLabeled = new StmtLabeledSSA(assignLabel(stmt), stmt, nestedLoopLevel);
            StmtLabeledSSA ifExitBuffer = new StmtLabeledSSA(assignBufferLabel(stmt, false), emptyStmtBlock(), nestedLoopLevel);

            List<LinkedList<Statement>> thenElse = subStmtCollector.collect(stmt);
            LinkedList<StmtLabeledSSA> ifBlocks = iterateSubStmts(thenElse.get(0), exitBlock, nestedLoopLevel);
            LinkedList<StmtLabeledSSA> elseBlocks = iterateSubStmts(thenElse.get(1), exitBlock, nestedLoopLevel);

            wireRelations(ifBlocks, stmtIfLabeled, ifExitBuffer);
            wireRelations(elseBlocks, stmtIfLabeled, ifExitBuffer);
            stmtIfLabeled.setShortCutToExit(ifExitBuffer);
            ifExitBuffer.setShortCutToExit(stmtIfLabeled);

            return stmtIfLabeled;
        }

        default StmtLabeledSSA createBlock(StmtForeach stmt, StmtLabeledSSA exitBlock, int nestedLoopLevel) {
            StmtLabeledSSA stmtFELabeled = new StmtLabeledSSA(assignLabel(stmt), stmt, nestedLoopLevel);
            StmtLabeledSSA stmtFEExit = new StmtLabeledSSA(assignBufferLabel(stmt, false), emptyStmtBlock(), nestedLoopLevel);

            List<Statement> body = subStmtCollector.collect(stmt).get(0);
            LinkedList<StmtLabeledSSA> currentBlocks = iterateSubStmts(body, exitBlock, nestedLoopLevel);
            wireRelations(currentBlocks, stmtFELabeled, stmtFEExit);

            stmtFELabeled.setShortCutToExit(stmtFEExit);
            stmtFEExit.setShortCutToExit(stmtFELabeled);
            return stmtFELabeled;
        }

        default StmtLabeledSSA createBlock(StmtCase stmt, StmtLabeledSSA exitBlock, int nestedLoopLevel) {
            StmtLabeledSSA stmtCaseLabeled = new StmtLabeledSSA(assignLabel(stmt), stmt, nestedLoopLevel);
            StmtLabeledSSA stmtCaseExit = new StmtLabeledSSA(assignBufferLabel(stmt, false), emptyStmtBlock(), nestedLoopLevel);
            List<LinkedList<Statement>> alts = subStmtCollector.collect(stmt);
            alts.forEach(altStmt -> {
                LinkedList<StmtLabeledSSA> currentBlocks = iterateSubStmts(altStmt, exitBlock, nestedLoopLevel);
                wireRelations(currentBlocks, stmtCaseLabeled, stmtCaseExit);
            });

            stmtCaseLabeled.setShortCutToExit(stmtCaseExit);
            return stmtCaseLabeled;
        }

        default StmtLabeledSSA createBlock(StmtBlock stmt, StmtLabeledSSA exitBlock, int nestedLoopLevel) {

            StmtLabeledSSA stmtBlockLabeled = new StmtLabeledSSA(assignLabel(stmt), stmt, nestedLoopLevel);

            List<LocalVarDecl> localVarDecls = stmt.getVarDecls();
            localVarDecls.forEach(SsaPhase::addNewLocalVarMapping);
            localVarDecls.forEach(v -> stmtBlockLabeled.addLocalValueNumber((LocalVarDecl) v.withName(getNewUniqueVarName(v.getOriginalName())).deepClone(), false));
            localVarDecl.addAll(stmtBlockLabeled.getLocalValueNumbers().keySet());

            List<Statement> body = subStmtCollector.collect(stmt).get(0);
            LinkedList<StmtLabeledSSA> currentBlocks = iterateSubStmts(body, exitBlock, nestedLoopLevel);

            StmtLabeledSSA stmtBlockExit = new StmtLabeledSSA(assignBufferLabel(stmt, false), emptyStmtBlock(), nestedLoopLevel);
            wireRelations(currentBlocks, stmtBlockLabeled, stmtBlockExit);
            stmtBlockLabeled.setShortCutToExit(stmtBlockExit);
            stmtBlockExit.setShortCutToExit(stmtBlockLabeled);

            return stmtBlockLabeled;
        }

        default StmtLabeledSSA createBlock(StmtAssignment stmt, StmtLabeledSSA exitBlock, int nestedLoopLevel) {
            StmtLabeledSSA stmtAssignmentLabeled = new StmtLabeledSSA(assignLabel(stmt), stmt, nestedLoopLevel);
            LValue v = stmt.getLValue();
            if (v instanceof LValueVariable) {
                String varName = ((LValueVariable) v).getVariable().getOriginalName();
                if (originalLVD.containsKey(varName)) {
                    LocalVarDecl currentVarDecl = originalLVD.get(varName);
                    LocalVarDecl newVarDecl = (LocalVarDecl) currentVarDecl.withName(getNewUniqueVarName(varName)).withValue(stmt.getExpression()).deepClone();
                    stmtAssignmentLabeled.addLocalValueNumber(newVarDecl, false);
                    localVarDecl.add((LocalVarDecl) newVarDecl.withValue(null).deepClone());

                } else {
                    VarDecl decl = declarations.declaration((LValueVariable) v);
                    if (functionScopeVars.containsKey(decl.getOriginalName())) {
                        throw new IllegalStateException("Variable in the scope of the program but definition missing in the local variable declaration map");
                    }
                }
            }
            return stmtAssignmentLabeled;
        }

        default StmtLabeledSSA createBlock(StmtCall stmt, StmtLabeledSSA exitBlock, int nestedLoopLevel) {
            StmtLabeledSSA stmtBlockLabeled = new StmtLabeledSSA(assignLabel(stmt), stmt, nestedLoopLevel);

            List<Expression> args = new LinkedList<>(stmt.getArgs());
            args.removeIf(e -> !(e instanceof ExprLet));

            List<LocalVarDecl> lvd = new LinkedList<>();
            args.forEach(e -> lvd.addAll(((ExprLet) e).getVarDecls()));


            lvd.forEach(l -> {
                addNewLocalVarMapping(l);
                stmtBlockLabeled.addLocalValueNumber(l.withName(getNewUniqueVarName(l.getOriginalName())), false);
            });

            localVarDecl.addAll(stmtBlockLabeled.getLocalValueNumbers().keySet());

            return stmtBlockLabeled;
        }

        default StmtLabeledSSA createBlock(StmtReturn stmt, StmtLabeledSSA exitBlock, int nestedLoopLevel) {
            StmtLabeledSSA stmtRet = new StmtLabeledSSA(assignLabel(stmt), stmt, nestedLoopLevel);
            stmtRet.setSuccessors(ImmutableList.of(exitBlock));
            return stmtRet;
        }

    }

//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------//


    @Module
    interface Transformation extends IRNode.Transformation {

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        /**
         * Creates a CFG and applies SSA to an ExprProcReturn
         *
         * @param proc the ExprProcReturn
         * @return the updated ExprProcReturn
         */
        default IRNode apply(ExprProcReturn proc) {

            Pair<StmtLabeledSSA, StmtLabeledSSA> entryAndExit = generateCFG(proc);
            if (!cfgOnly) {
                applySSA(entryAndExit);
            }
            StmtLabeled entryLabeled = transformIntoStmtLabeled(entryAndExit, Direction.UP);

            return proc.withBody(ImmutableList.of(entryLabeled.getSsaStmt()));
        }

        default IRNode apply(Transition transition) {
            Pair<StmtLabeledSSA, StmtLabeledSSA> entryAndExit = generateCFG(transition);
            if (!cfgOnly) {
                applySSA(entryAndExit);
            }
            StmtLabeled entryLabeled = transformIntoStmtLabeled(entryAndExit, Direction.UP);

            return transition.withBody(ImmutableList.of(entryLabeled.getSsaStmt()));
        }

    }


    //--------------------------------------------------------------------------- CFG Generation Utils ---------------------------------------------------------------------------//

    /**
     * Create a Control Flow Graph from an ExprProcReturn
     *
     * @param proc the ExprProcReturn
     * @return a pair containing the Entry and Exit of the CFG
     */
    private static Pair<StmtLabeledSSA, StmtLabeledSSA> generateCFG(ExprProcReturn proc) {
        StmtBlock body = (StmtBlock) proc.getBody().get(0);
        ImmutableList<Statement> stmts;
        if (!(body.getVarDecls().isEmpty() && body.getTypeDecls().isEmpty())) {
            StmtBlock startingBlock = new StmtBlock(body.getTypeDecls(), body.getVarDecls(), body.getStatements());
            stmts = ImmutableList.of(startingBlock);
        } else {
            stmts = body.getStatements();
        }

        //Transform ParamVarDecl into LocalVarDecl for SSA
        List<ParameterVarDecl> paramDecl = proc.getValueParameters();
        if (!paramDecl.isEmpty()) {
            paramDecl.forEach(SsaPhase::addVarInScope);
            List<LocalVarDecl> pvdToLvd = paramDecl.stream().map(p -> new LocalVarDecl(p.getAnnotations(), p.getType(), p.getName(), p.getDefaultValue(), false)).collect(Collectors.toList());
            StmtBlock updatedStmt;
            if (stmts.size() == 1 && stmts.get(0) instanceof StmtBlock) {
                StmtBlock original = (StmtBlock) stmts.get(0);
                updatedStmt = original.withVarDecls(ImmutableList.concat(original.getVarDecls(), ImmutableList.from(pvdToLvd)));
            } else {
                updatedStmt = new StmtBlock(ImmutableList.empty(), pvdToLvd, stmts);
            }
            stmts = ImmutableList.of(updatedStmt);
        }

        StmtLabeledSSA entry = new StmtLabeledSSA("Entry", emptyStmtBlock(), 0);
        StmtLabeledSSA exit = new StmtLabeledSSA("Exit", emptyStmtBlock(), 0);
        entry.setShortCutToExit(exit);

        LinkedList<StmtLabeledSSA> sub = iterateSubStmts(stmts, exit, 0);
        wireRelations(sub, entry, exit);

        return new Pair<>(entry, exit);
    }

    private static Pair<StmtLabeledSSA, StmtLabeledSSA> generateCFG(Transition transition) {
        StmtBlock body = (StmtBlock) transition.getBody().get(0);
        ImmutableList<Statement> stmts;
        if (!(body.getVarDecls().isEmpty() && body.getTypeDecls().isEmpty())) {
            StmtBlock startingBlock = new StmtBlock(body.getTypeDecls(), body.getVarDecls(), body.getStatements());
            stmts = ImmutableList.of(startingBlock);
        } else {
            stmts = body.getStatements();
        }

        StmtLabeledSSA entry = new StmtLabeledSSA("Entry", emptyStmtBlock(), 0);
        StmtLabeledSSA exit = new StmtLabeledSSA("Exit", emptyStmtBlock(), 0);
        entry.setShortCutToExit(exit);

        LinkedList<StmtLabeledSSA> sub = iterateSubStmts(stmts, exit, 0);
        wireRelations(sub, entry, exit);

        return new Pair<>(entry, exit);
    }

    private static void addAllLocalVarDecl(StmtLabeledSSA stmtBlock) {
        if (stmtBlock.getOriginalStmt() instanceof StmtBlock) {
            StmtBlock stmtBlockSSA = (StmtBlock) stmtBlock.getSsaModified();
            //List<LocalVarDecl> allVarDecl = localVarDecl;
            //Map<String, LocalVarDecl> distinctVarDecl = allVarDecl.stream().collect(Collectors.toMap(AbstractDecl::getName, vd -> vd, (vd1, vd2) -> vd2));
            stmtBlockSSA = (StmtBlock) stmtBlockSSA.withVarDecls((localVarDecl)).deepClone();
            stmtBlock.setSSAStatement(stmtBlockSSA);
        }
    }


    /**
     * Recursively create CFG blocks for each Statement's sub Statements
     *
     * @param stmts           The sub Statement
     * @param exitBlock       The CFG exit
     * @param nestedLoopLevel The nested loop level of the original Statement
     * @return The sub CFG composed of the sub Statements
     */
    private static LinkedList<StmtLabeledSSA> iterateSubStmts(List<Statement> stmts, StmtLabeledSSA exitBlock, int nestedLoopLevel) {
        return stmts.stream().map(currentStmt -> cfgCreator.createBlock(currentStmt, exitBlock, nestedLoopLevel)).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Connect a List of Statement in a linked way (successor <-> predecessor), in the order of the List. Add pred as head and succ at the end of the tail.
     *
     * @param currentBlocks The list of statement to link
     * @param pred          the head
     * @param succ          the last element
     */
    private static void wireRelations(LinkedList<StmtLabeledSSA> currentBlocks, StmtLabeledSSA pred, StmtLabeledSSA succ) {

        if (currentBlocks.isEmpty()) {
            pred.setSuccessors(ImmutableList.concat(pred.getSuccessors(), ImmutableList.of(succ)));
            succ.setPredecessors(ImmutableList.concat(succ.getPredecessors(), ImmutableList.of(pred)));
            return;
        }

        final ListIterator<StmtLabeledSSA> it = currentBlocks.listIterator();

        StmtLabeledSSA prev = pred;
        StmtLabeledSSA current;
        StmtLabeledSSA next;

        while (it.hasNext()) {
            current = it.next();
            if (it.hasNext()) {
                next = it.next();
                it.previous();
            } else {
                //if last stmt is a return stmt, go to the end of the program. program exit is already the successor of return
                next = (current.getOriginalStmt() instanceof StmtReturn) ? current.getSuccessors().get(0) : succ;
            }
            if (current.hasNoShortCut()) {
                current.setRelations(ImmutableList.concat(ImmutableList.of(prev), ImmutableList.from(current.getPredecessors())),
                        ImmutableList.concat(ImmutableList.of(next), ImmutableList.from(current.getSuccessors())));
                prev = current;
            } else {
                current.setPredecessors(ImmutableList.concat(ImmutableList.of(prev), ImmutableList.from(current.getPredecessors())));
                current.getExitBlock().setSuccessors(ImmutableList.of(next));
                prev = current.getExitBlock();
            }
        }
        //set frontier blocks relations
        pred.setSuccessors(ImmutableList.concat(pred.getSuccessors(), ImmutableList.of(currentBlocks.getFirst())));
        succ.setPredecessors(ImmutableList.concat(succ.getPredecessors(), ImmutableList.of(currentBlocks.getLast().getExitBlock())));
    }

    //-------------------------------------- CFG Helpers --------------------------------------//

    /**
     * Check whether a Statement has sub Statements
     *
     * @param stmt the Statement
     * @return true if has sub Statements
     */
    private static boolean isNotTerminal(Statement stmt) {
        return !(stmt instanceof StmtConsume) && !(stmt instanceof StmtWrite) && !(stmt instanceof StmtRead) && !(stmt instanceof StmtCall)
                && !(stmt instanceof StmtAssignment) && !(stmt instanceof StmtIfSSA) && !(stmt instanceof StmtWhileSSA);
    }

    /**
     * Assign a label to a Statement. Currently takes the name of the Statement
     *
     * @param stmt the statement to assign a label to
     * @return the label
     */
    private static String assignLabel(Statement stmt) {
        String res = stmt.getClass().toString();
        return (stmt instanceof StmtPhi) ? res.substring(34) : res.substring(30);
    }

    /**
     * Assign a label for buffer Statements
     *
     * @param stmt    the original Statement
     * @param isEntry Whether this is an entry label or an exit
     * @return the buffer label
     */
    private static String assignBufferLabel(Statement stmt, boolean isEntry) {
        return assignLabel(stmt) + ((isEntry) ? "Entry" : "Exit");
    }


    private static Statement emptyStmtBlock() {
        return new StmtBlock(ImmutableList.empty(), ImmutableList.empty(), ImmutableList.empty());
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------//


    //------------------------------------------------------------------------- SSA Algorithm Application ------------------------------------------------------------------------//

    /**
     * Takes a CFG and applies SSA to all variables declared within the scope of the ExprProcReturn
     *
     * @param cfg the CFG
     */
    private static void applySSA(Pair<StmtLabeledSSA, StmtLabeledSSA> cfg) {
        applySSAToVariables(cfg.getSecond());
        createAndAddPhiStmts(cfg.getSecond());
        recRebuildStmts(cfg.getFirst());
        addAllLocalVarDecl(cfg.getFirst().getSuccessors().get(0));
        clearAllVarMaps();
    }

    /**
     * Recursively apply the SSA algorithm from the bottom up
     *
     * @param stmtLabeled The Statement
     */
    private static void applySSAToVariables(StmtLabeledSSA stmtLabeled) {
        //Stop recursion at the top of the cfg
        if (stmtLabeled.hasNoPredecessors() || stmtLabeled.hasBeenVisted()) {
            return;
        }

        //read variable in declarations and assignments
        LinkedList<LocalVarDecl> lvd = new LinkedList<>(stmtLabeled.getLocalValueNumbers().keySet());
        lvd.removeIf(lv -> lv.getValue() instanceof ExprPhi || stmtLabeled.getLocalValueNumbers().get(lv)); //if ExprPhi or lvd has already been visited
        if (!lvd.isEmpty()) {
            lvd.forEach(l -> stmtLabeled.addLocalValueNumber(l.withValue(readLocalVarExpr(l.getValue(), stmtLabeled).getFirst()), true));
            //localVarDecl.addAll((stmtLabeled.getLocalValueNumbers().keySet()));
        }

        //read variables in expressions
        if (!stmtLabeled.isBufferBlock()) {
            Statement originalStmt = stmtLabeled.getOriginalStmt();
            if (!doesModifyVar(originalStmt)) { //TODO check with stmtcall
                List<Expression> exprInStmt = stmtExprCollector.collect(originalStmt);
                exprInStmt.forEach(e -> readSubExpr(e, stmtLabeled));
            }
        }

        Statement ssaStmt = applySSAToStatements(stmtLabeled);
        stmtLabeled.setSSAStatement(ssaStmt);
        stmtLabeled.setHasBeenVisted();
        stmtLabeled.getPredecessors().forEach(SsaPhase::applySSAToVariables);

    }

    /**
     * Replace all variables with their SSA values in a Statement and to its predecessors
     *
     * @param stmtLabeled the statement
     * @return The statement in SSA form
     */
    private static Statement applySSAToStatements(StmtLabeledSSA stmtLabeled) {
        if (stmtLabeled.isBufferBlock()) {
            return emptyStmtBlock();

        } else if (stmtLabeled.lvnIsEmpty()) {
            return stmtLabeled.getOriginalStmt();

        } else if (stmtLabeled.isLostCopyBlock()) {
            return stmtLabeled.getSsaModified();

        } else {
            Statement originalStmt = stmtLabeled.getOriginalStmt();
            Set<LocalVarDecl> ssaLocalVarDecls = stmtLabeled.getLocalValueNumbers().keySet();
            Statement ssaBlock;

            if (originalStmt instanceof StmtBlock) {
                //Replace LocalVarDecls in statement block
                List<LocalVarDecl> updatedLocalVarDecl = new LinkedList<>(ssaLocalVarDecls);
                updatedLocalVarDecl.removeIf(v -> !ssaLocalVarDecls.contains(v));
                updatedLocalVarDecl = updatedLocalVarDecl.stream().map(v -> (LocalVarDecl) v.deepClone()).collect(Collectors.toList());
                ssaBlock = ((StmtBlock) originalStmt).withVarDecls(new LinkedList<>(updatedLocalVarDecl));

            } else if (originalStmt instanceof StmtAssignment) {
                //Lost copy blocks are already ssa
                if (stmtLabeled.isLostCopyBlock()) {
                    ssaBlock = originalStmt;
                } else {
                    //Replace ssa result to LValue in assignment
                    String assignedVarName = ((LValueVariable) ((StmtAssignment) originalStmt).getLValue()).getVariable().getOriginalName();
                    LinkedList<LocalVarDecl> updatedLocalVarDecl = new LinkedList<>(ssaLocalVarDecls);
                    updatedLocalVarDecl.removeIf(v -> !v.getOriginalName().equals(assignedVarName));
                    LocalVarDecl varDecl = updatedLocalVarDecl.getFirst();
                    ssaBlock = (Statement) ((StmtAssignment) originalStmt).copy(new LValueVariable(variable(varDecl.getName())), varDecl.getValue()).deepClone();
                }
            } else if (originalStmt instanceof StmtWrite) {
                Map<ExprVariable, LocalVarDecl> ssaLocalValueNumbering = stmtLabeled.getExprValueNumbering();
                Pair<List<Expression>, Expression> stmtExpr = stmtExprCollector.collectListAndSinglExpr(originalStmt);

                stmtExpr.getFirst().replaceAll(e -> subExprCollectorOrReplacer.replaceExprVar(e, ssaLocalValueNumbering));
                Expression updatedRepeat = subExprCollectorOrReplacer.replaceExprVar(stmtExpr.getSecond(), ssaLocalValueNumbering);

                ssaBlock = stmtExprCollector.replaceListAndSingleExpr(originalStmt, stmtExpr.getFirst(), updatedRepeat);

            } else {
                //Collect all expressions in originalStmt
                Map<ExprVariable, LocalVarDecl> ssaLocalValueNumbering = stmtLabeled.getExprValueNumbering();
                List<Expression> stmtExpr = stmtExprCollector.collect(originalStmt);

                Map<Boolean, List<Expression>> exprs = stmtExpr.stream().collect(Collectors.partitioningBy(s -> s instanceof ExprLet));

                //Statement is a StmtCall
                if (!exprs.get(true).isEmpty()) {
                    List<Expression> stmtLetExpr = exprs.get(true);
                    stmtLetExpr.replaceAll(e -> subExprCollectorOrReplacer.replaceExprVarLet((ExprLet) e, stmtLabeled.getLocalValueNumbers()));
                    ssaBlock = stmtExprCollector.replaceListExpr(originalStmt, stmtLetExpr);

                } else {
                    stmtExpr = exprs.get(false);
                    //Apply ssa result
                    stmtExpr.replaceAll(e -> subExprCollectorOrReplacer.replaceExprVar(e, ssaLocalValueNumbering));
                    //Write the results back
                    if (stmtExpr.size() == 1) {
                        ssaBlock = stmtExprCollector.replaceSingleExpr(originalStmt, stmtExpr.get(0));
                    } else {
                        ssaBlock = stmtExprCollector.replaceListExpr(originalStmt, stmtExpr);
                    }
                }
            }
            //Return statement with renamed variable
            return ssaBlock;
        }
    }

    /**
     * Recursively find an Expression's sub Expressions and apply SSA
     *
     * @param expr        the Expression
     * @param stmtLabeled The statement containing the Expression
     */
    private static void readSubExpr(Expression expr, StmtLabeledSSA stmtLabeled) {

        List<Expression> subExpr = subExprCollectorOrReplacer.collectInternalExpr(expr);
        if (subExpr.isEmpty()) {
            if (expr instanceof ExprVariable && !stmtLabeled.varHasBeenVisited((ExprVariable) expr)) {
                VarDecl exprVarDecl = declarations.declaration((ExprVariable) expr);

                if (originalLVD.containsKey(exprVarDecl.getOriginalName())) {
                    stmtLabeled.addNewLVNPair((ExprVariable) expr, null);
                    Pair<LocalVarDecl, Integer> resPair = resolveSSAName(stmtLabeled, (ExprVariable) expr, 0, new HashSet<>());
                    if (resPair.getFirst() != null && resPair.getSecond() >= 0) {
                        stmtLabeled.updateLVNPair((ExprVariable) expr, resPair.getFirst());
                    }
                }
            }
            //Expression has no sub expressions and is not a variable
        } else {
            //recursively look through each sub expressions
            subExpr.forEach(subE -> readSubExpr(subE, stmtLabeled));
        }
    }

    /**
     * The algorithm used to retrieve the correct SSA value for the ExprVariable at the point found.
     *
     * @param stmtLabeled  the statement labeled
     * @param exprVariable the variable to retrieve SSA form for
     * @param recLvl       the current recursion level
     * @param visited      the Set of all StmtLabeled already visited
     * @return a pair containing a potential definition for the variable and an integer only used for the algorithm inner workings
     */
    private static Pair<LocalVarDecl, Integer> resolveSSAName(StmtLabeledSSA stmtLabeled, ExprVariable exprVariable, int recLvl, Set<StmtLabeledSSA> visited) {
        //Reaches top without finding definition
        if (stmtLabeled.isEntry()) {
            return new Pair<>(null, -1);
        }

        //self reference due to a loop
        if (stmtLabeled.varHasBeenVisited(exprVariable) && recLvl != 0) {
            return new Pair<>(null, -2);
        }

        String originalVarRef = exprVariable.getVariable().getOriginalName();
        Optional<LocalVarDecl> localVarDecl = stmtLabeled.getVarDefIfExists(originalVarRef);

        //found locally
        if (localVarDecl.isPresent()) {
            return new Pair<>(localVarDecl.get(), recLvl);

        } else {
            List<Pair<LocalVarDecl, Integer>> prevVarFound = new LinkedList<>();
            stmtLabeled.getPredecessors().forEach(pred -> {
                        if (!visited.contains(pred)) {
                            visited.add(pred);
                            prevVarFound.add(resolveSSAName(pred, exprVariable, recLvl + 1, visited));
                        }
                    }
            );

            if (prevVarFound.isEmpty()) {
                if (recLvl == 0) {
                    throw new IllegalStateException("No definitions for this variable found in the program up to this expression");
                } else {
                    return new Pair<>(null, -2);
                }
            }

            boolean foundPhi = false;
            int nb_found = 0;
            int smallest = Integer.MAX_VALUE;
            int resIndex = -1;

            for (int i = 0; i < prevVarFound.size(); ++i) {
                Pair<LocalVarDecl, Integer> currentPair = prevVarFound.get(i);
                int recValue = currentPair.getSecond();
                //found a definition
                if (recValue >= 0) {
                    if (recValue <= smallest) {
                        //found two definitions in direct predecessors, meaning no phi was available in original block
                        if (recValue == smallest) {
                            foundPhi = false;
                        } else {
                            //found two definitions in different levels of predecessors, so must check if closest to original block is already a phi
                            smallest = recValue;
                            foundPhi = currentPair.getFirst().getValue() instanceof ExprPhi;
                        }
                        resIndex = i;
                    }
                    ++nb_found;
                }
            }

            if (nb_found > 0) {
                Pair<LocalVarDecl, Integer> resultPair = prevVarFound.get(resIndex);
                if (nb_found > 1 && !foundPhi) {
                    //had to add get a new lvd
                    LocalVarDecl lvd = readVar(stmtLabeled, exprVariable.getVariable(), noStoppingCond).getFirst();
                    stmtLabeled.addLocalValueNumber(lvd, true);
                    return new Pair<>(lvd, resultPair.getSecond());
                } else {
                    return resultPair;
                }
            } else if (prevVarFound.get(0).getSecond() == -2) {
                //self reference
                return prevVarFound.get(0);
            } else {
                //nothing found
                throw new IllegalStateException("No definitions for this variable found in the program up to this expression");
            }
        }
    }

    /**
     * Recursively apply SSA to all the variables found in an expression that is an assignment to or a declaration of a variable
     *
     * @param expr        the expression
     * @param stmtLabeled the StmtLabeled
     * @return a pair made of the SSA variable and a boolean indicating whether the Expression has been transformed
     */
    private static Pair<Expression, Boolean> readLocalVarExpr(Expression expr, StmtLabeledSSA stmtLabeled) {

        if (expr instanceof ExprLiteral) {
            return new Pair<>(expr, false);

        } else if (expr instanceof ExprVariable) {
            Pair<LocalVarDecl, Boolean> result = readVar(stmtLabeled, ((ExprVariable) expr).getVariable(), noStoppingCond);
            ExprVariable ret = ((ExprVariable) expr).copy(variable(result.getFirst().getName()), ((ExprVariable) expr).getOld());
            return new Pair<>(ret, result.getSecond());

        } else if (expr instanceof ExprBinaryOp) {

            List<Pair<Expression, Boolean>> results = ((ExprBinaryOp) expr).getOperands().stream().map(o -> readLocalVarExpr(o, stmtLabeled)).collect(Collectors.toList());
            List<Pair<Expression, Boolean>> newOperands = new LinkedList<>(results);

            Expression selfRef = results.stream().filter(Pair::getSecond).findAny().map(Pair::getFirst).orElse(null);
            //self ref exist and binaryOp must be replaced
            if (selfRef != null) {
                return new Pair<>(selfRef, true);
            } else {
                List<Expression> newOps = newOperands.stream().map(Pair::getFirst).collect(Collectors.toList());
                return new Pair<>(((ExprBinaryOp) expr).copy(((ExprBinaryOp) expr).getOperations(), ImmutableList.from(newOps)), false);
            }

        } else if (expr instanceof ExprUnaryOp) {

            Expression newOperand = Objects.requireNonNull(readLocalVarExpr(((ExprUnaryOp) expr).getOperand(), stmtLabeled)).getFirst();
            return new Pair<>(((ExprUnaryOp) expr).copy(((ExprUnaryOp) expr).getOperation(), newOperand), false);

        } else if (expr instanceof ExprIf) {

            List<Expression> ifOrElse = subExprCollectorOrReplacer.collectInternalExpr(expr);
            ifOrElse.remove(0);
            ifOrElse.replaceAll(e -> readLocalVarExpr(e, stmtLabeled).getFirst());
            return new Pair<>(((ExprIf) expr).copy(((ExprIf) expr).getCondition(), ifOrElse.get(0), ifOrElse.get(1)), false);
        } else if (expr instanceof ExprInput) {
        }
        //TODO make an interface if a LocalVarDecl can have other types of Expressions as values
        return new Pair<>(expr, false);
    }


    /**
     * Check whether the Statement can modify or declare variables
     *
     * @param stmt the Statement
     * @return True if it does modify variables
     */
    private static boolean doesModifyVar(Statement stmt) {
        return stmt instanceof StmtAssignment || stmt instanceof StmtBlock;
    }


    //--------------------------------------  Create and Add StmtPhi --------------------------------------//


    /**
     * Create and connect the StmtPhis containing the phi functions created during SSA transformation to the CFG.
     *
     * @param stmtLabeled the StmtLabeled containing the phi functions' definitions
     */
    private static void createPhiBlock(StmtLabeledSSA stmtLabeled) {

        List<Statement> phiStmts = new LinkedList<>();
        List<LocalVarDecl> phis = new LinkedList<>(stmtLabeled.getLocalValueNumbers().keySet());
        phis.addAll(stmtLabeled.getExprValueNumbering().values());
        phis.removeIf(l -> !(l.getValue() instanceof ExprPhi));

        if (!phis.isEmpty()) {

            for (LocalVarDecl lvd : phis) {
                //avoid duplicates
                if(!alreadyAddedPhis.contains(lvd.getName())) {
                    alreadyAddedPhis.add(lvd.getName());

                    LValueVariable phiLValue = new LValueVariable(variable(lvd.getName()));
                    List<Expression> phiOperands = ((ExprPhi) lvd.getValue()).getOperands().map(op -> new ExprVariable(variable(op.getName())));
                    phiStmts.add(new StmtPhi(emptyStmtBlock(), phiLValue, phiOperands));
                }
            }
            if(!phiStmts.isEmpty()) {
                incorporateStmtPhis(stmtLabeled, phiStmts);
            }
        }
        stmtLabeled.setPhiBlockToCreated();
    }


    /**
     * Link the StmtPhi "in parallel" to the Statement they've been created inside of in the CFG
     *
     * @param originalStmtLabeled the StmtLabeled containing the phi functions' definitions
     * @param phis                the phi Statements wrapped in StmtLabeledSSA
     */
    private static void incorporateStmtPhis(StmtLabeledSSA originalStmtLabeled, List<Statement> phis) {

        StmtLabeledSSA stmtLabeledSSA = (originalStmtLabeled.isBufferBlock() && !originalStmtLabeled.hasNoShortCut()) ? originalStmtLabeled.getShortCutToExit() : originalStmtLabeled;
        Statement original = stmtLabeledSSA.getOriginalStmt();
        Statement ssaModified = stmtLabeledSSA.getSsaModified();

        if (original instanceof StmtWhile) {
            StmtWhile ssaWhile = (StmtWhile) ssaModified;
            Statement whileSSA = new StmtWhileSSA(ssaWhile.getCondition(), ssaWhile.getBody(), phis);
            originalStmtLabeled.setSSAStatement(whileSSA);

        } else if (original instanceof StmtIf) {
            StmtIf ssaIf = (StmtIf) ssaModified;
            Statement newSSAIf = new StmtIfSSA(ssaIf.getCondition(), ssaIf.getThenBranch(), ssaIf.getElseBranch(), phis);
            stmtLabeledSSA.setSSAStatement(newSSAIf);

        } else {
            StmtLabeledSSA nearestParent = findStatementLabeled(originalStmtLabeled, Arrays.asList("StmtBlock", "StmtCase", "StmtWhile"), Direction.UP);
            Statement ssaParent = nearestParent.getSsaModified();

            if (ssaParent instanceof StmtBlock) {
                List<Statement> oldBody = new ArrayList<>(((StmtBlock) nearestParent.getOriginalStmt()).getStatements());
                int indexOfOriginal = oldBody.indexOf(original);
                List<Statement> newBody = new ArrayList<>(((StmtBlock) nearestParent.getSsaModified()).getStatements());
                newBody.addAll(indexOfOriginal, phis);
                nearestParent.setSSAStatement(((StmtBlock) ssaParent).withStatements(newBody));
            }

            LinkedList<StmtLabeledSSA> phiLabeled = phis.stream().map(p -> new StmtLabeledSSA(assignLabel(p), p, originalStmtLabeled.loopLevel())).collect(Collectors.toCollection(LinkedList::new));
            phiLabeled.forEach(pl -> {pl.setPhiBlockToCreated(); pl.setHasBeenRebuilt(); pl.setSSAStatement(pl.getOriginalStmt());});
            wireRelations(phiLabeled, originalStmtLabeled.getPredecessors().get(0), originalStmtLabeled.getSuccessors().get(0));
        }

    }


    //-----------------------------------------------------------------------------------------------------//


    //-----------------------------  Rebuild Statement's bodies with SSA form -----------------------------//

    /**
     * Recreates the original Statement with an updated body containing all variables in SSA form
     *
     * @param stmtLabeled the StmtLabeled containing the Statement to rebuild
     * @return the rebuild Statement
     */
    private static Statement rebuildSingleStmt(StmtLabeledSSA stmtLabeled) {
        Statement originalStmt = stmtLabeled.getSsaModified();


        List<LinkedList<Statement>> stmtLists = subStmtCollector.collect(originalStmt);
        List<List<Statement>> newBodies = new LinkedList<>();
        for (List<Statement> l : stmtLists) {
            AtomicInteger order = new AtomicInteger();
            Map<Statement, Integer> oldBody = l.stream().collect(Collectors.toMap(s -> s, s -> order.getAndIncrement()));
            Map<Statement, Integer> newBodyMap = findStmtBody(stmtLabeled, new HashMap<>(), oldBody, 0);
            List<Statement> newBody = new LinkedList<>(newBodyMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)).keySet());
            newBodies.add(newBody);
        }
        stmtLabeled.setHasBeenRebuilt();
        return subStmtCollector.replace(originalStmt, newBodies);
    }


    private static void createAndAddPhiStmts(StmtLabeledSSA stmtLabeledSSA){
        if (!stmtLabeledSSA.havePhiBlocksBeenCreated()) {
            createPhiBlock(stmtLabeledSSA);
        }
        stmtLabeledSSA.getPredecessors().forEach(SsaPhase::createAndAddPhiStmts);
    }

    /**
     * Recursively reconstruct all Statements in the CFG
     *
     * @param stmtLabeled the current StmtLabeled to rebuild
     */
    private static void recRebuildStmts(StmtLabeledSSA stmtLabeled) {

        if (stmtLabeled.isExit() || (stmtLabeled.havePhiBlocksBeenCreated() && stmtLabeled.hasBeenRebuilt())) {
            return;
        }

        if (!stmtLabeled.hasBeenRebuilt() && !stmtLabeled.isBufferBlock() && isNotTerminal(stmtLabeled.getOriginalStmt())) {
            stmtLabeled.setSSAStatement(rebuildSingleStmt(stmtLabeled));
        }

        stmtLabeled.getSuccessors().forEach(SsaPhase::recRebuildStmts);
    }

    /**
     * Recursively updated the mapping from the Statement's old body of to the updated one
     *
     * @param stmtLabeled the current StmtLabeled
     * @param newBody     the Mapping from the old Statements and their position index in the sub Statement's List
     * @param oldBody     the Mapping from the new Statements and their position index in the sub Statement's List
     * @param recLvl      the recursion level
     * @return the updated Mapping
     */
    private static Map<Statement, Integer> findStmtBody(StmtLabeledSSA
                                                                stmtLabeled, Map<Statement, Integer> newBody, Map<Statement, Integer> oldBody, int recLvl) {

        if (stmtLabeled.isExit()) {
            return newBody;
        }

        Statement original = stmtLabeled.getOriginalStmt();
        StmtLabeledSSA next = stmtLabeled;
        StmtLabeledSSA scrutinee = stmtLabeled;

        if (recLvl != 0) {
            if (stmtLabeled.containSubStmts()) {
                if (stmtLabeled.isBufferBlock()) {
                    StmtLabeledSSA wrappedStmt = stmtLabeled.getSuccessors().get(0);
                    scrutinee = wrappedStmt;
                    original = wrappedStmt.getOriginalStmt();
                }

                next = stmtLabeled.getShortCutToExit();
            }
            if (oldBody.keySet().contains(original)) {
                if (isNotTerminal(scrutinee.getOriginalStmt()) && !scrutinee.hasBeenRebuilt()) {
                    scrutinee.setSSAStatement(rebuildSingleStmt(scrutinee));
                }
                newBody.putIfAbsent(scrutinee.getSsaModified(), oldBody.get(original));
                oldBody.remove(original);
            } else {
                //wrong path at a fork
                return newBody;
            }
        }

        if (oldBody.size() != 0) {
            List<StmtLabeledSSA> successors = new LinkedList<>(next.getSuccessors());
            if (recLvl == 0 && stmtLabeled.getOriginalStmt() instanceof StmtWhile) {
                successors.removeIf(StmtLabeledSSA::isBufferBlock);
            }
            successors.forEach(succ -> newBody.putAll(findStmtBody(succ, newBody, oldBody, recLvl + 1)));
        }

        return newBody;
    }


    //-----------------------------------------------------------------------------------------------------//


    //------------------------------  Transform StmtLabeledSSA to StmtLabeled -----------------------------//


    /**
     * The the CFG composed of StmtLabeledSSA and transform it into a CFG made of StmtLabeled
     *
     * @param entryAndExit the pair containing the CFG entry and exit
     * @return the StmtLabeled CFG
     */
    private static StmtLabeled transformIntoStmtLabeled
    (Pair<StmtLabeledSSA, StmtLabeledSSA> entryAndExit, Direction dir) {
        Map<StmtLabeledSSA, StmtLabeled> mapping = collectAllStmtLabeledSSA(entryAndExit.getFirst(), new HashMap<>());

        StmtLabeledSSA exit = entryAndExit.getSecond();
        StmtLabeledSSA entry = entryAndExit.getFirst();

        Map<StmtLabeledSSA, StmtLabeled> updatedMap = updateRelations(exit, mapping, new HashSet<>(), Direction.UP);
        updatedMap = updateRelations(entry, updatedMap, new HashSet<>(), Direction.DOWN);
        return updatedMap.get((dir == Direction.UP) ? entry.getSuccessors().get(0) : exit.getSuccessors().get(0));
    }

    /**
     * Collect all the StmtLabeledSSA in the CFG
     *
     * @param stmtLabeledSSA the current StmtLabeled
     * @param currentMap     The Map containing all StmtLabeledSSA and their corresponding StmtLabeled so far
     * @return the complete Map containing all StmtLabeledSSA and their corresponding StmtLabeled
     */
    private static Map<StmtLabeledSSA, StmtLabeled> collectAllStmtLabeledSSA(StmtLabeledSSA
                                                                                     stmtLabeledSSA, Map<StmtLabeledSSA, StmtLabeled> currentMap) {
        currentMap.putIfAbsent(stmtLabeledSSA, new StmtLabeled(stmtLabeledSSA.getLabel(), stmtLabeledSSA.getOriginalStmt(), stmtLabeledSSA.getSsaModified(), ImmutableList.empty(), ImmutableList.empty()));
        stmtLabeledSSA.getSuccessors().stream().filter(s -> !(currentMap.keySet().contains(s))).forEach(s -> {
            Map<StmtLabeledSSA, StmtLabeled> res = collectAllStmtLabeledSSA(s, currentMap);
            currentMap.forEach(res::putIfAbsent);
        });
        return currentMap;
    }

    /**
     * Recursively generate the successors or predecessors of the newly created StmtLabeled
     *
     * @param stmtLabeledSSA the current StmtLabeled
     * @param elements       the Mapping of StmtLabeledSSA to StmtLabeled
     * @param visited        the Set of visited Statements so far
     * @param dir            indicating whether traversing the CFG from top down or bottom up
     * @return the  Map containing the StmtLabeledSSA and StmtLabeled
     */
    private static Map<StmtLabeledSSA, StmtLabeled> updateRelations(StmtLabeledSSA
                                                                            stmtLabeledSSA, Map<StmtLabeledSSA, StmtLabeled> elements, Set<StmtLabeledSSA> visited, Direction dir) {
        StmtLabeled stmt = elements.get(stmtLabeledSSA);
        Map<StmtLabeledSSA, StmtLabeled> updatedMap = elements;
        List<StmtLabeledSSA> relations = (dir == Direction.UP) ? stmtLabeledSSA.getPredecessors() : stmtLabeledSSA.getSuccessors();
        if (dir == Direction.UP) {
            for (StmtLabeledSSA rel : relations) {
                if (!updatedMap.containsKey(rel) && rel.isBufferBlock()) {
                    continue;
                }
                updatedMap.replace(rel, updatedMap.get(rel).updateSuccs(stmt));
            }
        } else {
            for (StmtLabeledSSA rel : relations) {
                updatedMap.replace(rel, updatedMap.get(rel).updatePreds(stmt));
            }
        }
        visited.add(stmtLabeledSSA);
        if (dir == Direction.UP) {
            for (StmtLabeledSSA rel : relations) {
                if (!visited.contains(rel)) {
                    updatedMap = updateRelations(rel, updatedMap, visited, Direction.UP);
                } else {
                    updatedMap.replace(rel, updatedMap.get(rel).updateSuccs(stmt));
                }
            }
        } else {
            for (StmtLabeledSSA rel : relations) {
                if (!visited.contains(rel)) {
                    updatedMap = updateRelations(rel, updatedMap, visited, Direction.DOWN);
                } else {
                    updatedMap.replace(rel, updatedMap.get(rel).updatePreds(stmt));
                }
            }
        }
        return updatedMap;
    }


    //-----------------------------------------------------------------------------------------------------//


    //--------------------------------------- Local Value Numbering ---------------------------------------//


    /**
     * Keeps track of original variable names with their original declarations
     */
    private static HashMap<String, LocalVarDecl> originalLVD = new HashMap<>();

    /**
     * Keeps track of how many times a variables has been encountered
     */
    private static HashMap<String, Integer> localValueCounter = new HashMap<>();

    /**
     * Keeps track of the variables within the scope of the ExprProcReturn
     */
    private static HashMap<String, VarDecl> functionScopeVars = new HashMap<>();

    /**
     * Add a variable to the scope
     *
     * @param var the variable to add
     */
    private static void addVarInScope(VarDecl var) {
        functionScopeVars.putIfAbsent(var.getOriginalName(), var);
    }

    /**
     * Add a variable definition
     *
     * @param lvd the LocalVarDecl
     */
    private static void addNewLocalVarMapping(LocalVarDecl lvd) {
        originalLVD.put(lvd.getOriginalName(), lvd);
    }

    /**
     * Create a new unique numbered name for a variable
     *
     * @param varName the variable to be renamed
     * @return the new name
     */
    private static String getNewUniqueVarName(String varName) {
        if (localValueCounter.containsKey(varName)) {
            localValueCounter.merge(varName, 1, Integer::sum);
            return varName + "_SSA_" + (localValueCounter.get(varName)).toString();
        } else {
            localValueCounter.put(varName, 0);
            return varName + "_SSA_0";
        }
    }

    /**
     * Create a variable with a new numbered name
     *
     * @param var  the original variable
     * @param expr the value of the variable
     * @return a copy of the variable with a new name
     */
    private static LocalVarDecl createLocalVarDecl(Variable var, Expression expr) {
        String newName = getNewUniqueVarName(var.getOriginalName());
        LocalVarDecl originalDef = originalLVD.get(var.getOriginalName());
        return originalDef.withName(newName).withValue(expr);
    }

    /**
     * Clear all the variable mappings to avoid conflicts between different ExprProcReturn
     */
    private static void clearAllVarMaps() {
        originalLVD.clear();
        localValueCounter.clear();
        functionScopeVars.clear();
        localVarDecl.clear();
        alreadyAddedPhis.clear();
    }


    //-----------------------------------------------------------------------------------------------------//


    //-------------------------------------- Self Reference Handling --------------------------------------//


    /**
     * Find a potential variable self reference in an Expression
     *
     * @param operands        the sub Expressions
     * @param originalVarDecl the variable to find a self reference for
     * @return the self reference if exists
     */
    private static Optional<Variable> findSelfReference(List<Expression> operands, LocalVarDecl originalVarDecl) {
        //find variable corresponding to local var declaration in operands of BinaryExpr or return null
        Optional<Expression> interLockedExpr = operands.stream().filter(o -> o instanceof ExprBinaryOp).findAny();
        Optional<Variable> selfRef =
                operands.stream()
                        .filter(o -> (o instanceof ExprVariable && originalVarDecl.getOriginalName().equals(((ExprVariable) o).getVariable().getOriginalName())))
                        .findAny()
                        .map(expression -> ((ExprVariable) expression).getVariable());
        if (!selfRef.isPresent() && interLockedExpr.isPresent()) {
            return findSelfReference(((ExprBinaryOp) interLockedExpr.get()).getOperands(), originalVarDecl);
        } else {
            return selfRef;
        }

    }

    /**
     * Transform the self reference to restore correctness in the value numbering and avoid having a Lost Copy Problem
     *
     * @param selfAssignedVar the self assigned variable
     * @param var             the self reference
     * @param stmtLabeled     the StmtLabeledSSA containing it
     * @return The updated LocalVarDecl
     */
    private static LocalVarDecl handleSelfReference(LocalVarDecl selfAssignedVar, Variable var, StmtLabeledSSA
            stmtLabeled) {
        return (stmtLabeled.loopLevel() > 0) ? handleSelfRefWithinLoop(selfAssignedVar, var, stmtLabeled) : handleSimpleSelfRef(selfAssignedVar, var, stmtLabeled);
    }

    /**
     * Handle self reference in a Statement outside of a loop
     *
     * @param selfAssignedVar the self assigned variable
     * @param var             the self reference
     * @param stmtLabeled     the StmtLabeledSSA containing it
     * @return The updated LocalVarDecl
     */
    private static LocalVarDecl handleSimpleSelfRef(LocalVarDecl selfAssignedVar, Variable var, StmtLabeledSSA
            stmtLabeled) {

        //if outside loop : a = a + 1 become a_i = a_(i-1) + 1
        Variable replacementVar = deriveOldVarName(selfAssignedVar);
        Expression replacementExpr = replaceVariableInExpr(selfAssignedVar.getValue(), var, replacementVar, stmtLabeled);
        LocalVarDecl updatedVarDecl = selfAssignedVar.withValue(replacementExpr);
        stmtLabeled.addLocalValueNumber(updatedVarDecl, true);
        return updatedVarDecl;
    }

    /**
     * Handle self reference in a Statement within a loop
     *
     * @param selfAssignedVar the self assigned variable
     * @param var             the self reference
     * @param stmtLabeled     the StmtLabeledSSA containing it
     * @return The updated LocalVarDecl
     */
    private static LocalVarDecl handleSelfRefWithinLoop(LocalVarDecl selfAssignedVar, Variable var, StmtLabeledSSA
            stmtLabeled) {

        Variable beforeLoop = variable("u1");
        Variable loopEntry = variable("u0");
        Variable loopExit = variable("x2");
        Variable loopSelfRef = variable("x3");
        Variable loopNextIter = variable("u2");

        LocalVarDecl u1 = selfAssignedVar.withName("u1").withValue(new ExprVariable(variable(findVarPredFromLoop(selfAssignedVar, stmtLabeled).getName())));
        LocalVarDecl u0 = u1.withName("u0").withValue(null); //u0 = null
        LocalVarDecl x2 = u1.withName("x2").withValue(new ExprVariable(loopEntry)); //x2 = u0
        Expression replacementExpr = replaceVariableInExpr(selfAssignedVar.getValue(), var, loopExit, stmtLabeled);
        LocalVarDecl x3 = selfAssignedVar.withValue(replacementExpr);
        LocalVarDecl u2 = u1.withName("u2").withValue(new ExprVariable(loopSelfRef));
        u0 = u0.withValue(new ExprPhi(loopEntry, ImmutableList.of(u1, u2)));

        //Set u2 correctly
        Optional<LocalVarDecl> succWithinLoop = findVarSuccFromLoop(selfAssignedVar, stmtLabeled);
        if (succWithinLoop.isPresent()) {
            u2 = u2.withValue(new ExprVariable(variable(succWithinLoop.get().getName())));
        }

        LocalVarDecl newOriginalVar = x2.withName(selfAssignedVar.getName());
        stmtLabeled.addLocalValueNumber(newOriginalVar, true);


        //Generate new StmtAssignment for all temporary variables
        StmtAssignment beforeLoopStmt = new StmtAssignment(new LValueVariable(beforeLoop), u1.getValue());
        StmtAssignment loopEntryPhiStmt = new StmtAssignment(new LValueVariable(loopEntry), u0.getValue());
        StmtAssignment loopSelfRefStmt = new StmtAssignment(new LValueVariable(loopSelfRef), x3.getValue());
        StmtAssignment loopNextIterStmt = new StmtAssignment(new LValueVariable(loopNextIter), u2.getValue());
        StmtAssignment loopExitSmt = new StmtAssignment(new LValueVariable(loopExit), x2.getValue());

        //label them
        StmtLabeledSSA first = stmtLabeled.withNewOriginal(beforeLoopStmt);
        StmtLabeledSSA second = stmtLabeled.withNewOriginal(loopEntryPhiStmt);
        StmtLabeledSSA third = stmtLabeled.withNewOriginal(loopExitSmt);
        StmtLabeledSSA fourth = stmtLabeled.withNewOriginal(loopSelfRefStmt);
        StmtLabeledSSA fifth = stmtLabeled.withNewOriginal(loopNextIterStmt);

        //reset their relations and their local value numbering
        Stream.of(first, second, third, fourth, fifth).forEach(sl -> {
            sl.setRelations(Collections.emptyList(), Collections.emptyList());
            sl.lostCopyName();
        });

        //save predecessor and successor of original block and resets its relations empty
        StmtLabeledSSA blockPred = stmtLabeled.getPredecessors().get(0);
        StmtLabeledSSA blockSucc = stmtLabeled.getSuccessors().get(0);
        stmtLabeled.setRelations(Collections.emptyList(), Collections.emptyList());

        //rewire original block predecessor and successor to the new blocks
        List<StmtLabeledSSA> predNewSucc = new LinkedList<>(blockPred.getSuccessors());
        List<StmtLabeledSSA> succNewPred = new LinkedList<>(blockSucc.getPredecessors());
        predNewSucc.remove(stmtLabeled);
        succNewPred.remove(stmtLabeled);
        blockPred.setSuccessors(predNewSucc);
        blockSucc.setPredecessors(succNewPred);

        //wire the new blocks between them and to the original successor and predecessor
        wireRelations(new LinkedList<>(Arrays.asList(first, second, third, fourth, fifth)), blockPred, blockSucc);

        return u2;
    }

    /**
     * Replace a variable in an Exprssion
     *
     * @param originalExpr   the original Expression
     * @param varToReplace   the variable to replace
     * @param replacementVar the replacement variable
     * @param stmtLabeled    the StmtLabeledSSA containing the Expression to modify
     * @return the updated Expression
     */
    private static Expression replaceVariableInExpr(Expression originalExpr, Variable varToReplace, Variable
            replacementVar, StmtLabeledSSA stmtLabeled) {
        ExprVariable updatedVariable = new ExprVariable(variable(replacementVar.getName()));
        //TODO make copies
        if (originalExpr instanceof ExprUnaryOp) {
            Expression updatedVar = replaceVariableInExpr(((ExprUnaryOp) originalExpr).getOperand(), varToReplace, replacementVar, stmtLabeled);
            return ((ExprUnaryOp) originalExpr).copy(((ExprUnaryOp) originalExpr).getOperation(), updatedVar);

        } else if (originalExpr instanceof ExprVariable) {
            return (((ExprVariable) originalExpr).getVariable().getOriginalName().equals(varToReplace.getOriginalName())) ? updatedVariable : readLocalVarExpr(originalExpr, stmtLabeled).getFirst();

        } else if (originalExpr instanceof ExprBinaryOp) {
            //Should work for interlocked ExprBinary
            List<Expression> operands = ((ExprBinaryOp) originalExpr).getOperands();
            //find and replace variable looked for, apply algorithm to all other operands
            List<Expression> newOperands = operands.stream()
                    .map(o -> (o instanceof ExprVariable && ((ExprVariable) o).getVariable().getOriginalName().equals(varToReplace.getOriginalName())) ? updatedVariable :
                            Objects.requireNonNull(replaceVariableInExpr(o, varToReplace, replacementVar, stmtLabeled)))
                    .collect(Collectors.toList());
            return new ExprBinaryOp(((ExprBinaryOp) originalExpr).getOperations(), ImmutableList.from(newOperands));

        } else if (originalExpr instanceof ExprIf) {
            List<Expression> operands = new LinkedList<>(Arrays.asList(((ExprIf) originalExpr).getThenExpr(), ((ExprIf) originalExpr).getElseExpr()));
            //find and replace variable looked for, apply algorithm to all other operands
            List<Expression> newOperands = operands.stream()
                    .map(o -> (o instanceof ExprVariable && ((ExprVariable) o).getVariable().getOriginalName().equals(varToReplace.getOriginalName())) ? updatedVariable :
                            Objects.requireNonNull(replaceVariableInExpr(o, varToReplace, replacementVar, stmtLabeled)))
                    .collect(Collectors.toList());
            return new ExprIf(((ExprIf) originalExpr).getCondition(), newOperands.get(0), newOperands.get(1));
        }
        return originalExpr;
    }

    /**
     * Find the previous variable assignment/definition before the self referencing Statement
     *
     * @param varToFind    the variable to look for
     * @param originalStmt the original StmtLabeled
     * @return the previous definition of the variable
     */
    private static LocalVarDecl findVarPredFromLoop(LocalVarDecl varToFind, StmtLabeledSSA originalStmt) {
        StmtLabeledSSA pred = originalStmt.getPredecessors().get(0);
        LocalVarDecl resUntilLoopStart = readVar(pred, variable(varToFind.getOriginalName()), new Pair<>(3, Optional.of(originalStmt))).getFirst();

        if (resUntilLoopStart != null && !(resUntilLoopStart.getValue() instanceof ExprPhi && ((ExprPhi) resUntilLoopStart.getValue()).getOperands().isEmpty())) {
            return resUntilLoopStart;
        } else {
            //Only keep loop entry block
            StmtLabeledSSA loopEntry = findStatementLabeled(originalStmt, Collections.singletonList("StmtWhile"), Direction.DOWN);
            List<StmtLabeledSSA> loopEntryPreds = new LinkedList<>(loopEntry.getPredecessors());
            loopEntryPreds.removeIf(p -> p.loopLevel() > loopEntry.loopLevel());

            //Not outside nestedLoops
            if (loopEntry.loopLevel() != 0) {
                LocalVarDecl outerLoopRes = findVarPredFromLoop(varToFind, loopEntryPreds.get(0));

                Optional<LocalVarDecl> succRes = findVarSuccFromLoop(varToFind, originalStmt);
                if (succRes.isPresent()) {
                    return varToFind.withValue(new ExprPhi(variable(varToFind.getName()), Arrays.asList(outerLoopRes, succRes.get())));
                } else {
                    return outerLoopRes;
                }
            } else {
                // result from outside nested loops
                return readVar(loopEntryPreds.get(0), variable(varToFind.getOriginalName()), noStoppingCond).getFirst();
            }
        }
    }

    /**
     * Find a potential assignment to the self referenced var from the self-ref Statement up to the end of the loop
     *
     * @param varToFind    the variable to look for
     * @param originalStmt the original StmtLabeled
     * @return the previous definition of the variable if it exists
     */
    private static Optional<LocalVarDecl> findVarSuccFromLoop(LocalVarDecl varToFind, StmtLabeledSSA originalStmt) {

        StmtLabeledSSA loopExit = findStatementLabeled(originalStmt, Collections.singletonList("StmtWhile"), Direction.UP);
        List<StmtLabeledSSA> loopExitPredecessors = new LinkedList<>(loopExit.getPredecessors());
        loopExitPredecessors.removeIf(p -> p.loopLevel() <= loopExit.loopLevel());
        StmtLabeledSSA lastLoopStmt = loopExitPredecessors.get(0);

        if (lastLoopStmt.equals(originalStmt)) {
            return Optional.empty();
        }

        LocalVarDecl resFromStmtToLoopEnd = readVar(lastLoopStmt, variable(varToFind.getOriginalName()), new Pair<>(3, Optional.of(originalStmt))).getFirst();
        return (resFromStmtToLoopEnd == null) ? Optional.empty() : Optional.of(resFromStmtToLoopEnd);

    }

    /**
     * Recreate a variable with its previous number according to the value numbering
     *
     * @param oldVar the variable
     * @return the variable with the previous number
     */
    private static Variable deriveOldVarName(LocalVarDecl oldVar) {
        //a_i => a_(i-1)
        String oldName = oldVar.getName();
        //get current variable number
        int oldNb = Integer.parseInt(oldName.substring(oldName.length() - 1));
        //replace name with previous number
        String replacementName = oldName.substring(0, oldName.length() - 1) + String.valueOf((oldNb - 1));
        return variable(replacementName);
    }

    /**
     * Recursively traverse the CFG to find a StmtLabeledSSA
     *
     * @param stmtLabeled the current StmtLabeled
     * @param label       the label of the StmtLabeled to find
     * @param dir         the direction of traversal
     * @return the StmtLabeledSSA with the given label
     */
    private static StmtLabeledSSA findStatementLabeled(StmtLabeledSSA stmtLabeled, List<String> label, Direction
            dir) {
        //if dir is false = successors
        if (label.contains(stmtLabeled.getLabel())) {
            return stmtLabeled;
        } else {
            StmtLabeledSSA related = (dir == Direction.UP) ? stmtLabeled.getPredecessors().get(0) : stmtLabeled.getSuccessors().get(0);
            return findStatementLabeled(related, label, dir);
        }
    }

    /**
     * Simple Enum to avoid using boolean for direction.
     */
    private enum Direction {
        /**
         * Up direction.
         */
        UP,
        /**
         * Down direction.
         */
        DOWN
    }


    //-----------------------------------------------------------------------------------------------------//


    //------------------------------------------- SSA Algorithm -------------------------------------------//


    /**
     * Generate a stopping condition for the algorithm
     *
     * @param stmtLabeled the Statement to evaluate
     * @param stopCond    a pair containing a integer representing a stopping code and a potential StmtLabeledSSA to compare to the current one
     * @return true if the algorithm should stop at that point
     */
    private static boolean generateStopCondition(StmtLabeledSSA
                                                         stmtLabeled, Pair<Integer, Optional<StmtLabeledSSA>> stopCond) {

        boolean res;
        Optional<StmtLabeledSSA> self = stopCond.getSecond();

        switch (stopCond.getFirst()) {
            case 1:
                res = stmtLabeled.getLabel().equals("StmtWhile");
                break;
            case 2:
                res = stmtLabeled.getLabel().equals("StmtWhileExit");
                break;
            case 3:
                if (self.isPresent()) {
                    res = stmtLabeled.equals(self.get());
                    break;
                } else {
                    throw new IllegalArgumentException("if condType is 3, a StatementLabeled must be provided");
                }
            default:
                res = false;
        }
        return res;
    }

    /**
     * Recursively find the last assignment/declaration of a variable in the CFG
     *
     * @param stmt     the current Stmt
     * @param var      the variable to look for
     * @param stopCond a pair containing the elements necessary to generate a stopping condition
     * @return the closest variable definition, can be a phi function
     */
    private static Pair<LocalVarDecl, Boolean> readVar(StmtLabeledSSA stmt, Variable
            var, Pair<Integer, Optional<StmtLabeledSSA>> stopCond) {
        boolean hasToStop = generateStopCondition(stmt, stopCond);
        if (hasToStop) {
            return new Pair<>(null, false);
        }

        //look for self reference
        Optional<LocalVarDecl> matchingLVD = stmt.getLocalValueNumbers().keySet().stream().filter(l -> l.getOriginalName().equals(var.getOriginalName())).findAny();
        if (matchingLVD.isPresent()) {
            //Locally found
            LocalVarDecl lv = matchingLVD.get();
            if (stmt.getOriginalStmt() instanceof StmtAssignment) {
                Expression lvExpr = lv.getValue();
                List<Expression> internalExpr = subExprCollectorOrReplacer.collectInternalExpr(lvExpr);
                if (lvExpr instanceof ExprIf) internalExpr.remove(0);

                Optional<Variable> selfRefVar = findSelfReference(internalExpr, lv);
                if (selfRefVar.isPresent()) {
                    return new Pair<>(handleSelfReference(lv, selfRefVar.get(), stmt), true);
                }
            }
            //no self reference
            return new Pair<>(lv, false);
        } else {
            //No def found in current Statement
            Optional<Pair<LocalVarDecl, Boolean>> recRes = readVarRec(stmt, var, stopCond);
            return recRes.orElseGet(() -> new Pair<>(null, false));
        }
    }

    /**
     * Recursively find the last assignment/declaration of a variable in the CFG if wasn't found in the initial StmtLabeled and creates a phi function (ExprPhi)
     * if predecessor join at the current StmtLabeled.
     *
     * @param stmt     the current Stmt
     * @param var      the variable to look for
     * @param stopCond a pair containing the elements necessary to generate a stopping condition
     * @return the closest variable definition, can be a phi function
     */
    private static Optional<Pair<LocalVarDecl, Boolean>> readVarRec(StmtLabeledSSA stmt, Variable
            var, Pair<Integer, Optional<StmtLabeledSSA>> stopCond) {
        boolean hasToStop = generateStopCondition(stmt, stopCond);
        if (hasToStop) {
            return Optional.empty();
        }
        if (stmt.getPredecessors().size() == 1) {
            return Optional.of(readVar(stmt.getPredecessors().get(0), var, stopCond));
        } else {
            ExprPhi phiExpr = new ExprPhi(var, ImmutableList.empty());
            //Add Phi to Global value numbering
            LocalVarDecl localVarPhi = createLocalVarDecl(var, phiExpr);
            stmt.addLocalValueNumber(localVarPhi, true);
            localVarPhi = addPhiOperands(localVarPhi, var, stmt, stopCond);

            Expression phiResult = localVarPhi.getValue();
            if (phiResult instanceof ExprPhi && !((ExprPhi) phiResult).isUndefined()) {
                stmt.addLocalValueNumber(localVarPhi, true);
            }
            return Optional.of(new Pair<>(localVarPhi, false));
        }
    }

    /**
     * Recursively look for the phi operands of a freshly inserted phi function
     *
     * @param phi         the LocalVarDecl containing the ExprPhi
     * @param var         the variable being looked for
     * @param stmtLabeled the current StmtLabeled
     * @param stopCond    a pair containing the elements necessary to generate a stopping condition
     * @return phi the LocalVarDecl containing the ExprPhi with its operands
     */
    private static LocalVarDecl addPhiOperands(LocalVarDecl phi, Variable var, StmtLabeledSSA
            stmtLabeled, Pair<Integer, Optional<StmtLabeledSSA>> stopCond) {
        LinkedList<LocalVarDecl> phiOperands = new LinkedList<>();

        for (StmtLabeledSSA stmt : stmtLabeled.getPredecessors()) {
            LocalVarDecl lookedUpVar = readVar(stmt, var, stopCond).getFirst();
            phiOperands.add(lookedUpVar);
            //add Phi to list of users of its operands
            if (lookedUpVar != null && lookedUpVar.getValue() instanceof ExprPhi) {
                ((ExprPhi) lookedUpVar.getValue()).addUser(ImmutableList.of(phi));
            }
        }
        ((ExprPhi) phi.getValue()).setOperands(phiOperands);
        return tryRemoveTrivialPhi(phi);
    }

    /**
     * Try to remove a phi function that is dead, meaning unreachable or unused. If it has a single valid operand return this operand
     *
     * @param phi the LocalVarDecl containing the ExprPhi to test
     * @return the LocalVarDecl with a cleaned up ExprPhi
     */
    private static LocalVarDecl tryRemoveTrivialPhi(LocalVarDecl phi) {
        ImmutableList<LocalVarDecl> operands = ((ExprPhi) phi.getValue()).getOperands();
        //Problem in logic if first operand is right, but second is null or a phi
        List<LocalVarDecl> validOperands = new LinkedList<>();
        LocalVarDecl currentOp = null;
        for (LocalVarDecl op : operands) {
            //Unique value or self reference
            if (op == currentOp || op.equals(phi)) {
                continue;
            }
            if (currentOp != null) {
                localVarDecl.add(phi);
                return phi;
            }
            currentOp = op;
            validOperands.add(op);
        }

        if (validOperands.size() == 1) {
            ((ExprPhi) phi.getValue()).setOperands(ImmutableList.of(validOperands.get(0)));
            return validOperands.get(0);
        }

        //TODO make empty block
        ((ExprPhi) phi.getValue()).clearNullArgs();
        if (((ExprPhi) phi.getValue()).getOperands().isEmpty()) {
            ((ExprPhi) phi.getValue()).becomesUndefined();
            //TODO make empty expression?
        }


        LinkedList<LocalVarDecl> phiUsers = ((ExprPhi) phi.getValue()).getUsers();
        phiUsers.remove(phi);

        for (LocalVarDecl userPhi : phiUsers) {
            LinkedList<LocalVarDecl> userPhiUsers = ((ExprPhi) userPhi.getValue()).getUsers();
            userPhiUsers.set(userPhiUsers.indexOf(phi), currentOp);
            ((ExprPhi) userPhi.getValue()).addUser(userPhiUsers);
        }

        for (LocalVarDecl user : phiUsers) {
            tryRemoveTrivialPhi(user);
        }

        return (((ExprPhi) phi.getValue()).isUndefined()) ? phi : currentOp;
    }


    //-----------------------------------------------------------------------------------------------------//


    //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------//
}
