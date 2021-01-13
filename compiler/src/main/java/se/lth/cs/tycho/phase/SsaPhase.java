package se.lth.cs.tycho.phase;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.AbstractDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.stmt.*;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.ir.stmt.ssa.ExprPhi;
import se.lth.cs.tycho.ir.stmt.ssa.StmtLabeled;
import se.lth.cs.tycho.ir.stmt.ssa.StmtLabeledSSA;
import se.lth.cs.tycho.ir.stmt.ssa.StmtPhi;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.util.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static se.lth.cs.tycho.ir.Variable.variable;

public class SsaPhase implements Phase {

    private static CollectOrReplaceExprInStmt stmtExprCollector = null;
    private static CollectExpressions subExprCollector = null;
    private static ReplaceExprVar exprVarReplacer = null;
    private static CollectOrReplaceSubStmts subStmtCollector = null;
    private static CreateControlFlowGraphBlocks cfgCreator = null;

    public SsaPhase() {
        stmtExprCollector = MultiJ.from(CollectOrReplaceExprInStmt.class).instance();
        subExprCollector = MultiJ.from(CollectExpressions.class).instance();
        exprVarReplacer = MultiJ.from(ReplaceExprVar.class).instance();
        subStmtCollector = MultiJ.from(CollectOrReplaceSubStmts.class).instance();
        cfgCreator = MultiJ.from(CreateControlFlowGraphBlocks.class).instance();
    }

    @Override
    public String getDescription() {
        return "Applies SsaPhase transformation to ExprProcReturn";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        Transformation transformation = MultiJ.from(SsaPhase.Transformation.class).instance();
        return task.transformChildren(transformation);
    }


    //--------------- Utils ---------------//

    @Module
    interface CollectOrReplaceSubStmts {

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


        default Statement replace(Statement s, List<List<Statement>> l) {
            return s;
        }

        default Statement replace(StmtBlock block, List<List<Statement>> newBody) {
            return block.withStatements(newBody.get(0));
        }

        default Statement replace(StmtWhile whilee, List<List<Statement>> newBody) {
            return whilee.withBody(newBody.get(0));
        }

        default Statement replace(StmtForeach foreach, List<List<Statement>> newBody) {
            return foreach.withBody(newBody.get(0));
        }

        default Statement replace(StmtIf iff, List<List<Statement>> newBody) {
            if (newBody.size() < 2) {
                throw new IllegalArgumentException("too few statement list given");
            }
            return iff.withThenBranch(newBody.get(0)).withElseBranch(newBody.get(1));
        }

        default Statement replace(StmtCase casee, List<List<Statement>> newBody) {
            if (newBody.size() != casee.getAlternatives().size()) {
                throw new IllegalArgumentException("too few statement list given");
            }
            AtomicInteger i = new AtomicInteger();
            List<StmtCase.Alternative> newAlts = casee.getAlternatives().stream().map(alt -> alt.copy(alt.getPattern(), alt.getGuards(), newBody.get(i.getAndIncrement()))).collect(Collectors.toList());
            return casee.copy(casee.getScrutinee(), newAlts);
        }

    }

    @Module
    interface CollectOrReplaceExprInStmt {

        Pair<List<? extends IRNode>, Expression> collectMultipleExpr(Statement s);

        default Pair<List<? extends IRNode>, Expression> collectMultipleExpr(StmtCall call) {
            return new Pair<>(call.getArgs(), call.getProcedure());
        }

        List<Expression> collect(Statement s);

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

        Statement replaceSingleExpr(Statement s, Expression e);

        Statement replaceListExpr(Statement s, List<Expression> e);

        Statement replaceListAndSingleExpr(Statement s, Expression e, List<Expression> l);

        default Statement replaceSingleExpr(StmtReturn ret, Expression retVal) {
            return ret.copy(retVal);
        }

        default Statement replaceSingleExpr(StmtWhile whilee, Expression cond) {
            return whilee.withCondition(cond);
        }

        default Statement replaceSingleExpr(StmtIf iff, Expression condition) {
            return iff.withCondition(condition);
        }

        default Statement replaceSingleExpr(StmtCase casee, Expression scrut) {
            return casee.copy(scrut, casee.getAlternatives());
        }

        default Statement replaceListExpr(StmtForeach foreach, List<Expression> filters) {
            return foreach.withFilters(filters);
        }

        default Statement replaceListExpr(StmtCall call, List<Expression> args) {
            return call.copy(call.getProcedure(), args);
        }
    }

    @Module
    interface CollectExpressions {
        List<Expression> collectInternalExpr(Expression e);

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
    }

    @Module
    interface ReplaceExprVar {

        default LocalVarDecl getLocalVarDecl(String originalVarName, Set<LocalVarDecl> localVarDecls) {
            for (LocalVarDecl lvd : localVarDecls) {
                if (lvd.getOriginalName().equals(originalVarName)) {
                    return lvd;
                }
            }
            throw new IllegalStateException("Missing ssa result for given variable");
        }

        default Expression replaceExprVar(Expression original, Map<ExprVariable, LocalVarDecl> replacements) {
            return original;
        }

        default Expression replaceExprVar(ExprVariable var, Map<ExprVariable, LocalVarDecl> replacements) {
            //check that var is contained in the new mapping
            if (replacements.containsKey(var)) {
                ExprVariable res = var.copy(variable(replacements.get(var).getName()), var.getOld());
                return res;
            } else
                throw new IllegalStateException("Local Value Numbering missed this variable or the replacement mapping argument is incomplete");
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
            List<Expression> newOp = subExprCollector.collectInternalExpr(binOp);
            newOp.replaceAll(op -> replaceExprVar(op, replacements));
            return new ExprBinaryOp(binOp.getOperations(), ImmutableList.from(newOp));
        }

        default Expression replaceExprVar(ExprCase casee, Map<ExprVariable, LocalVarDecl> replacements) {
            List<Expression> scrut = subExprCollector.collectInternalExpr(casee);
            Expression newScrut = replaceExprVar(scrut.get(0), replacements);

            List<ExprCase.Alternative> alts = casee.getAlternatives();
            alts.replaceAll(alt -> new ExprCase.Alternative(alt.getPattern(), alt.getGuards(), replaceExprVar(subExprCollector.collectInternalExpr(alt).get(0), replacements)));

            return new ExprCase(newScrut, alts);
        }

        default Expression replaceExprVar(ExprComprehension comp, Map<ExprVariable, LocalVarDecl> replacements) {
            Expression collection = replaceExprVar(comp.getCollection(), replacements);
            List<Expression> filters = comp.getFilters();
            filters.replaceAll(f -> replaceExprVar(f, replacements));
            return comp.copy(comp.getGenerator(), filters, collection);
        }

        default Expression replaceExprVar(ExprDeref deref, Map<ExprVariable, LocalVarDecl> replacements) {
            return deref.withReference(replaceExprVar(deref.getReference(), replacements));
        }

        default Expression replaceExprVar(ExprLambda lambda, Map<ExprVariable, LocalVarDecl> replacements) {
            return lambda.copy(lambda.getValueParameters(), replaceExprVar(lambda.getBody(), replacements), lambda.getReturnType());
        }

        default Expression replaceExprVar(ExprList list, Map<ExprVariable, LocalVarDecl> replacements) {
            List<Expression> elems = list.getElements();
            elems.replaceAll(e -> replaceExprVar(e, replacements));
            return list.withElements(elems);
        }

        default Expression replaceExprVar(ExprSet set, Map<ExprVariable, LocalVarDecl> replacements) {
            List<Expression> elems = set.getElements();
            elems.replaceAll(e -> replaceExprVar(e, replacements));
            return set.withElements(elems);
        }

        default Expression replaceExprVar(ExprTuple tuple, Map<ExprVariable, LocalVarDecl> replacements) {
            List<Expression> elems = tuple.getElements();
            elems.replaceAll(e -> replaceExprVar(e, replacements));
            return tuple.copy(elems);
        }

        default Expression replaceExprVar(ExprTypeConstruction typeConstruction, Map<ExprVariable, LocalVarDecl> replacements) {
            List<Expression> elems = typeConstruction.getArgs();
            elems.replaceAll(e -> replaceExprVar(e, replacements));
            return typeConstruction.copy(typeConstruction.getConstructor(), typeConstruction.getTypeParameters(), typeConstruction.getValueParameters(), elems);
        }

        default Expression replaceExprVar(ExprUnaryOp unOp, Map<ExprVariable, LocalVarDecl> replacements) {
            return unOp.copy(unOp.getOperation(), replaceExprVar(unOp.getOperand(), replacements));
        }

        default Expression replaceExprVar(ExprField field, Map<ExprVariable, LocalVarDecl> replacements) {
            return field.copy(replaceExprVar(field.getStructure(), replacements), field.getField());
        }

        default Expression replaceExprVar(ExprNth nth, Map<ExprVariable, LocalVarDecl> replacements) {
            return nth.copy(replaceExprVar(nth.getStructure(), replacements), nth.getNth());
        }

        default Expression replaceExprVar(ExprTypeAssertion exprTypeAssertion, Map<ExprVariable, LocalVarDecl> replacements) {
            return exprTypeAssertion.copy(replaceExprVar(exprTypeAssertion.getExpression(), replacements), exprTypeAssertion.getType());
        }

        default Expression replaceExprVar(ExprIndexer indexer, Map<ExprVariable, LocalVarDecl> replacements) {
            Expression newStruct = replaceExprVar(indexer.getStructure(), replacements);
            Expression newIndex = replaceExprVar(indexer.getIndex(), replacements);
            return indexer.copy(newStruct, newIndex);
        }

        default Expression replaceExprVarLet(ExprLet let, Map<LocalVarDecl, Boolean> replacements) {
            List<LocalVarDecl> lvd = let.getVarDecls();
            List<LocalVarDecl> newLvd = lvd.stream().map(lv -> getLocalVarDecl(lv.getOriginalName(), replacements.keySet())).collect(Collectors.toList());
            return let.withVarDecls(newLvd);
        }

        default Expression replaceExprVar(ExprMap map, Map<ExprVariable, LocalVarDecl> replacements) {
            ImmutableList<ImmutableEntry<Expression, Expression>> mappings = map.getMappings();
            ImmutableList<ImmutableEntry<Expression, Expression>> newMappings = ImmutableList.empty();

            for (ImmutableEntry<Expression, Expression> entry : mappings) {
                ImmutableEntry<Expression, Expression> newEntry = ImmutableEntry.of(replaceExprVar(entry.getKey(), replacements), replaceExprVar(entry.getValue(), replacements));
                newMappings.add(newEntry);
            }
            return map.withMappings(newMappings);
        }
    }

    @Module
    interface CreateControlFlowGraphBlocks {

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

            StmtLabeledSSA entryWhile = new StmtLabeledSSA(assignBufferLabel(stmt, true), null, nestedLoopLevel);
            StmtLabeledSSA exitWhile = new StmtLabeledSSA(assignBufferLabel(stmt, false), null, nestedLoopLevel);

            wireRelations(new LinkedList<>(Collections.singletonList(stmtWhileLabeled)), entryWhile, exitWhile);
            entryWhile.setShortCutToExit(exitWhile);
            return entryWhile;
        }

        default StmtLabeledSSA createBlock(StmtIf stmt, StmtLabeledSSA exitBlock, int nestedLoopLevel) {

            StmtLabeledSSA stmtIfLabeled = new StmtLabeledSSA(assignLabel(stmt), stmt, nestedLoopLevel);
            StmtLabeledSSA ifExitBuffer = new StmtLabeledSSA(assignBufferLabel(stmt, false), null, nestedLoopLevel);

            List<LinkedList<Statement>> thenElse = subStmtCollector.collect(stmt);
            LinkedList<StmtLabeledSSA> ifBlocks = iterateSubStmts(thenElse.get(0), exitBlock, nestedLoopLevel);
            LinkedList<StmtLabeledSSA> elseBlocks = iterateSubStmts(thenElse.get(1), exitBlock, nestedLoopLevel);

            wireRelations(ifBlocks, stmtIfLabeled, ifExitBuffer);
            wireRelations(elseBlocks, stmtIfLabeled, ifExitBuffer);
            stmtIfLabeled.setShortCutToExit(ifExitBuffer);

            return stmtIfLabeled;
        }

        default StmtLabeledSSA createBlock(StmtForeach stmt, StmtLabeledSSA exitBlock, int nestedLoopLevel) {
            StmtLabeledSSA stmtFELabeled = new StmtLabeledSSA(assignLabel(stmt), stmt, nestedLoopLevel);
            StmtLabeledSSA stmtFEExit = new StmtLabeledSSA(assignBufferLabel(stmt, false), null, nestedLoopLevel);

            List<Statement> body = subStmtCollector.collect(stmt).get(0);
            LinkedList<StmtLabeledSSA> currentBlocks = iterateSubStmts(body, exitBlock, nestedLoopLevel);
            wireRelations(currentBlocks, stmtFELabeled, stmtFEExit);

            stmtFELabeled.setShortCutToExit(stmtFEExit);
            return stmtFELabeled;
        }

        default StmtLabeledSSA createBlock(StmtCase stmt, StmtLabeledSSA exitBlock, int nestedLoopLevel) {
            StmtLabeledSSA stmtCaseLabeled = new StmtLabeledSSA(assignLabel(stmt), stmt, nestedLoopLevel);
            StmtLabeledSSA stmtCaseExit = new StmtLabeledSSA(assignBufferLabel(stmt, false), null, nestedLoopLevel);
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
            localVarDecls.forEach(v -> stmtBlockLabeled.addLocalValueNumber(v.withName(getNewUniqueVarName(v.getOriginalName())), false));

            List<Statement> body = subStmtCollector.collect(stmt).get(0);
            LinkedList<StmtLabeledSSA> currentBlocks = iterateSubStmts(body, exitBlock, nestedLoopLevel);

            StmtLabeledSSA stmtBlockExit = new StmtLabeledSSA(assignBufferLabel(stmt, false), null, nestedLoopLevel);
            wireRelations(currentBlocks, stmtBlockLabeled, stmtBlockExit);
            stmtBlockLabeled.setShortCutToExit(stmtBlockExit);

            return stmtBlockLabeled;
        }

        default StmtLabeledSSA createBlock(StmtAssignment stmt, StmtLabeledSSA exitBlock, int nestedLoopLevel) {
            StmtLabeledSSA stmtAssignLabeled = new StmtLabeledSSA(assignLabel(stmt), stmt, nestedLoopLevel);
            LValue v = stmt.getLValue();
            if (v instanceof LValueVariable) {
                String varName = ((LValueVariable) v).getVariable().getOriginalName();
                LocalVarDecl currentVarDecl = originalLVD.get(varName);
                LocalVarDecl newVarDecl = currentVarDecl.withName(getNewUniqueVarName(varName)).withValue(stmt.getExpression());
                stmtAssignLabeled.addLocalValueNumber(newVarDecl, false);
            }
            return stmtAssignLabeled;
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

            return stmtBlockLabeled;
        }

        default StmtLabeledSSA createBlock(StmtReturn stmt, StmtLabeledSSA exitBlock, int nestedLoopLevel) {
            StmtLabeledSSA stmtRet = new StmtLabeledSSA(assignLabel(stmt), stmt, nestedLoopLevel);
            stmtRet.setSuccessors(ImmutableList.of(exitBlock));
            return stmtRet;
        }

    }

    @Module
    interface Transformation extends IRNode.Transformation {

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default IRNode apply(ExprProcReturn proc) {
            //StmtLabeledSSA rootCFG = generateCFG(proc, ReturnNode.ROOT);
            Pair<StmtLabeledSSA, StmtLabeledSSA> entryAndExit = generateCFG(proc);
            //applySSAToVar(exitCFG);
            //applySSAToVar(exitCFG);
            applySSA(entryAndExit);

            return proc;
        }
    }

    //--------------- CFG Generation Utils ---------------//

    private static Pair<StmtLabeledSSA, StmtLabeledSSA> generateCFG(ExprProcReturn proc) {
        StmtBlock body = (StmtBlock) proc.getBody().get(0);
        ImmutableList<Statement> stmts;
        if (!(body.getVarDecls().isEmpty() && body.getTypeDecls().isEmpty())) {
            StmtBlock startingBlock = new StmtBlock(body.getTypeDecls(), body.getVarDecls(), body.getStatements());
            stmts = ImmutableList.of(startingBlock);
        } else {
            stmts = body.getStatements();
        }

        StmtLabeledSSA entry = new StmtLabeledSSA("ProgramEntry", null, 0);
        StmtLabeledSSA exit = new StmtLabeledSSA("ProgramExit", null, 0);
        entry.setShortCutToExit(exit);

        LinkedList<StmtLabeledSSA> sub = iterateSubStmts(stmts, exit, 0);
        wireRelations(sub, entry, exit);

        return new Pair<>(entry, exit);
    }

    //------ Connect Blocks Together -----//
    private static LinkedList<StmtLabeledSSA> iterateSubStmts(List<Statement> stmts, StmtLabeledSSA exitBlock, int nestedLoopLevel) {
        LinkedList<StmtLabeledSSA> currentBlocks = new LinkedList<>();

        for (Statement currentStmt : stmts) {
            currentBlocks.add(cfgCreator.createBlock(currentStmt, exitBlock, nestedLoopLevel));
        }
        return currentBlocks;
    }


    private static void wireRelations(LinkedList<StmtLabeledSSA> currentBlocks, StmtLabeledSSA pred, StmtLabeledSSA succ) {

        if (currentBlocks.isEmpty()) {
            pred.setSuccessors(ImmutableList.concat(pred.getSuccessors(), ImmutableList.of(succ)));
            succ.setPredecessors(ImmutableList.concat(succ.getPredecessors(), ImmutableList.of(pred)));
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

    //------ Helpers -----//
    private static boolean isNotTerminal(Statement stmt) {
        return !(stmt instanceof StmtConsume) && !(stmt instanceof StmtWrite) && !(stmt instanceof StmtRead) && !(stmt instanceof StmtCall) && !(stmt instanceof StmtAssignment);
    }

    private static String assignLabel(Statement stmt) {
        return stmt.getClass().toString().substring(30);
    }

    private static String assignBufferLabel(Statement type, boolean isEntry) {
        return assignLabel(type) + ((isEntry) ? "Entry" : "Exit");
    }


    //--------------- SSA Algorithm Application ---------------//

    //------ Put everything together -----//
    private static void applySSA(Pair<StmtLabeledSSA, StmtLabeledSSA> cfg) {
        applySSAToVar(cfg.getSecond());
        recRebuildStmts(cfg.getFirst());
        StmtLabeled start = transformIntoStmtLabeled(cfg);
        int a = 0;
    }

    //------ Replace all variables with ssa names in all Statement and Expressions -----//
    private static void applySSAToVar(StmtLabeledSSA stmtLabeled) {
        //Stop recursion at the top of the cfg
        if (stmtLabeled.hasNoPredecessors() || stmtLabeled.hasBeenVisted()) {
            return;
        }

        //read variable in declarations and assignments
        LinkedList<LocalVarDecl> lvd = new LinkedList<>(stmtLabeled.getLocalValueNumbers().keySet());
        lvd.removeIf(lv -> lv.getValue() instanceof ExprPhi || stmtLabeled.getLocalValueNumbers().get(lv)); //if ExprPhi or lvd has already been visited
        if (!lvd.isEmpty()) {
            lvd.forEach(l -> stmtLabeled.addLocalValueNumber(l.withValue(readLocalVarExpr(l.getValue(), stmtLabeled).getFirst()), true));
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
        stmtLabeled.getPredecessors().forEach(SsaPhase::applySSAToVar);

    }

    private static Statement applySSAToStatements(StmtLabeledSSA stmtLabeled) {
        if (stmtLabeled.isBufferBlock()) {
            return null;
        } else if (stmtLabeled.lvnIsEmpty()) {
            return stmtLabeled;
        } else if (stmtLabeled.isLostCopyBlock()) {
            return stmtLabeled.getSsaModified();
        } else {
            Statement originalStmt = stmtLabeled.getOriginalStmt();
            Set<LocalVarDecl> ssaLocalVarDecls = stmtLabeled.getLocalValueNumbers().keySet();
            Statement ssaBlock;

            if (originalStmt instanceof StmtBlock) {
                //Replace LocalVarDecls in statement block
                Set<String> originalVarNames = ssaLocalVarDecls.stream().map(AbstractDecl::getOriginalName).collect(Collectors.toSet());
                List<LocalVarDecl> containedVarDecls = ((StmtBlock) originalStmt).getVarDecls().stream().filter(lvd -> originalVarNames.contains(lvd.getOriginalName())).collect(Collectors.toList());
                ssaBlock = ((StmtBlock) originalStmt).withVarDecls(new LinkedList<>(containedVarDecls));

            } else if (originalStmt instanceof StmtAssignment) {
                //Lost copy blocks are already ssa
                if (stmtLabeled.isLostCopyBlock()) {
                    ssaBlock = originalStmt;
                } else {
                    //Replace ssa result to LValue in assignment
                    String assignedVarName = ((LValueVariable) ((StmtAssignment) originalStmt).getLValue()).getVariable().getOriginalName();
                    Optional<LocalVarDecl> varDecl = stmtLabeled.getVarDefIfExists(assignedVarName);
                    if (varDecl.isPresent()) {
                        ssaBlock = ((StmtAssignment) originalStmt).copy(new LValueVariable(variable(varDecl.get().getName())), varDecl.get().getValue());
                    } else {
                        throw new IllegalArgumentException("variable declarations not present");
                    }
                }

            } else {
                //Collect all expressions in originalStmt
                Map<ExprVariable, LocalVarDecl> ssaLocalValueNumbering = stmtLabeled.getExprValueNumbering();
                List<Expression> stmtExpr = stmtExprCollector.collect(originalStmt);

                Map<Boolean, List<Expression>> exprs = stmtExpr.stream().collect(Collectors.partitioningBy(s -> s instanceof ExprLet));

                //Statement is a StmtCall
                if (!exprs.get(true).isEmpty()) {
                    List<Expression> stmtLetExpr = exprs.get(true);
                    stmtLetExpr.replaceAll(e -> exprVarReplacer.replaceExprVarLet((ExprLet) e, stmtLabeled.getLocalValueNumbers()));
                    ssaBlock = stmtExprCollector.replaceListExpr(originalStmt, stmtLetExpr);

                } else {
                    stmtExpr = exprs.get(false);
                    //Apply ssa result
                    stmtExpr.replaceAll(e -> exprVarReplacer.replaceExprVar(e, ssaLocalValueNumbering));
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

    private static void readSubExpr(Expression expr, StmtLabeledSSA stmtLabeled) {

        List<Expression> subExpr = subExprCollector.collectInternalExpr(expr);
        if (subExpr.isEmpty()) {
            if (expr instanceof ExprVariable && !stmtLabeled.varHasBeenVisited((ExprVariable) expr)) {

                stmtLabeled.addNewLVNPair((ExprVariable) expr, null);
                Pair<LocalVarDecl, Integer> resPair = resolveSSAName(stmtLabeled, (ExprVariable) expr, 0, new HashSet<>());
                if (resPair.getFirst() != null && resPair.getSecond() >= 0) {
                    stmtLabeled.updateLVNPair((ExprVariable) expr, resPair.getFirst());
                }
            }
            //Expression has no sub expressions and is not a variable
        } else {
            //recursively look through each sub expressions
            subExpr.forEach(subE -> readSubExpr(subE, stmtLabeled));
        }
    }

    private static Pair<LocalVarDecl, Integer> resolveSSAName(StmtLabeledSSA stmtLabeled, ExprVariable exprVariable, int recLvl, Set<StmtLabeledSSA> visited) {
        //Reaches top without finding definition
        if (stmtLabeled.getLabel().equals("ProgramEntry")) {
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
                    LocalVarDecl lvd = readVar(stmtLabeled, exprVariable.getVariable(), 0, Optional.empty()).getFirst();
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

    private static Pair<Expression, Boolean> readLocalVarExpr(Expression expr, StmtLabeledSSA stmtLabeled) {

        if (expr instanceof ExprLiteral) {
            return new Pair<>(expr, false);

        } else if (expr instanceof ExprVariable) {
            Pair<LocalVarDecl, Boolean> result = readVar(stmtLabeled, ((ExprVariable) expr).getVariable(), 0, Optional.empty());
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

            List<Expression> ifOrElse = subExprCollector.collectInternalExpr(expr);
            ifOrElse.remove(0);
            ifOrElse.replaceAll(e -> readLocalVarExpr(e, stmtLabeled).getFirst());
            return new Pair<>(((ExprIf) expr).copy(((ExprIf) expr).getCondition(), ifOrElse.get(0), ifOrElse.get(1)), false);
        }
        return new Pair<>(null, false);
    }

    //------ Rebuild Statements with new bodies ------//
    private static void recRebuildStmts(StmtLabeledSSA stmtLabeled) {


        if (stmtLabeled.getLabel().equals("ProgramExit") || (stmtLabeled.havePhiBlocksBeenCreated() && stmtLabeled.hasBeenRebuilt())) {
            return;
        }

        if (!stmtLabeled.havePhiBlocksBeenCreated()) {
            createPhiBlock(stmtLabeled);
        }

        if (!stmtLabeled.hasBeenRebuilt() && !stmtLabeled.isBufferBlock() && isNotTerminal(stmtLabeled.getOriginalStmt())) {
            stmtLabeled.setSSAStatement(rebuildSingleStmt(stmtLabeled));
        }

        stmtLabeled.getSuccessors().forEach(SsaPhase::recRebuildStmts);
    }

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

    private static Map<Statement, Integer> findStmtBody(StmtLabeledSSA stmtLabeled, Map<Statement, Integer> newBody, Map<Statement, Integer> oldBody, int recLvl) {

        if (stmtLabeled.getLabel().equals("ProgramExit")) {
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

            if (oldBody.containsKey(original)) {
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

    //------ Add PhiStmts to the Program ------//
    private static void createPhiBlock(StmtLabeledSSA stmtLabeled) {
        Statement original = stmtLabeled.getOriginalStmt();
        if (!(original instanceof StmtAssignment) && !(original instanceof StmtBlock)) {
            if (!stmtLabeled.getLocalValueNumbers().isEmpty()) {
                List<StmtPhi> phiStmts = new LinkedList<>();
                for (LocalVarDecl lvd : stmtLabeled.getLocalValueNumbers().keySet()) {
                    LValueVariable phiLValue = new LValueVariable(variable(lvd.getName()));
                    List<Expression> phiOperands = ((ExprPhi) lvd.getValue()).getOperands().map(op -> new ExprVariable(variable(op.getName())));
                    phiStmts.add(new StmtPhi(phiLValue, phiOperands));
                }
                List<StmtLabeledSSA> phiLabeled = phiStmts.stream().map(phi -> new StmtLabeledSSA(assignLabel(phi), phi, stmtLabeled.loopLevel())).collect(Collectors.toList());
                wirePhiStmts(stmtLabeled, phiLabeled);
                stmtLabeled.setPhiBlockToCreated();
            }
        }
    }

    private static void wirePhiStmts(StmtLabeledSSA originalStmtLabeled, List<StmtLabeledSSA> phis) {
        String label = originalStmtLabeled.getLabel();
        switch (label) {
            case "StmtIfExit":
            case "StmtCaseExit": {
                StmtLabeledSSA originalSucc = originalStmtLabeled.getSuccessors().get(0);
                wireRelations(new LinkedList<>(phis), originalStmtLabeled, originalSucc);
            }
            break;
            case "StmtWhile": {
                List<StmtLabeledSSA> preds = new LinkedList<>(originalStmtLabeled.getPredecessors());
                preds.removeIf(p -> !p.isBufferBlock());
                StmtLabeledSSA whileEntry = preds.get(0);
                wireRelations(new LinkedList<>(phis), whileEntry, originalStmtLabeled);
            }
            break;
            default: {
                wireRelations(new LinkedList<>(phis), originalStmtLabeled.getPredecessors().get(0), originalStmtLabeled.getSuccessors().get(0));
            }
        }
        //StmtWhileExit, StmtIf always has a single predecessor
    }

    //------ Transform to StmtLabeled ------//
    private static StmtLabeled transformIntoStmtLabeled(Pair<StmtLabeledSSA, StmtLabeledSSA> entryAndExit) {
        Map<StmtLabeledSSA, StmtLabeled> mapping = collectAllStmtLabeledSSA(entryAndExit.getFirst(), new HashMap<>());
        //Map<StmtLabeledSSA, Pair<List<StmtLabeledSSA>, List<StmtLabeledSSA>>> allMappings = findPredAndSucc(mapping.keySet());
        StmtLabeledSSA exit = entryAndExit.getSecond();
        StmtLabeledSSA entry = entryAndExit.getFirst();
        Map<StmtLabeledSSA, StmtLabeled> updatedMap = updateRelations(exit, mapping, new HashSet<>(), Direction.UP);
        updatedMap = updateRelations(entry, updatedMap, new HashSet<>(), Direction.DOWN);
        StmtLabeled res = updatedMap.get(exit);
        return res;
    }

    private static Map<StmtLabeledSSA, StmtLabeled> collectAllStmtLabeledSSA(StmtLabeledSSA stmtLabeledSSA, Map<StmtLabeledSSA, StmtLabeled> currentMap) {
        currentMap.putIfAbsent(stmtLabeledSSA, new StmtLabeled(stmtLabeledSSA.getLabel(), stmtLabeledSSA.getOriginalStmt(), stmtLabeledSSA.getSsaModified(), ImmutableList.empty(), ImmutableList.empty()));
        stmtLabeledSSA.getSuccessors().stream().filter(s -> !(currentMap.keySet().contains(s))).forEach(s -> {
            Map<StmtLabeledSSA, StmtLabeled> res = collectAllStmtLabeledSSA(s, currentMap);
            currentMap.forEach(res::putIfAbsent);
        });
        return currentMap;
    }

    private static Map<StmtLabeledSSA, StmtLabeled> updateRelations(StmtLabeledSSA stmtLabeledSSA, Map<StmtLabeledSSA, StmtLabeled> elements, Set<StmtLabeledSSA> visited, Direction dir) {
        StmtLabeled stmt = elements.get(stmtLabeledSSA);
        Map<StmtLabeledSSA, StmtLabeled> updatedMap = elements;
        List<StmtLabeledSSA> relations = (dir == Direction.UP) ? stmtLabeledSSA.getPredecessors() : stmtLabeledSSA.getSuccessors();
        if (dir == Direction.UP) {
            for (StmtLabeledSSA pred : relations) {
                updatedMap.replace(pred, updatedMap.get(pred).updateSuccs(stmt));
            }
        } else {
            for (StmtLabeledSSA pred : relations) {
                updatedMap.replace(pred, updatedMap.get(pred).updatePreds(stmt));
            }
        }
        visited.add(stmtLabeledSSA);
        if (dir == Direction.UP) {
            for (StmtLabeledSSA pred : relations) {
                if (!visited.contains(pred)) {
                    updatedMap = updateRelations(pred, updatedMap, visited, Direction.UP);
                } else {
                    updatedMap.replace(pred, updatedMap.get(pred).updateSuccs(stmt));
                }
            }
        } else {
            for (StmtLabeledSSA pred : relations) {
                if (!visited.contains(pred)) {
                    updatedMap = updateRelations(pred, updatedMap, visited, Direction.DOWN);
                } else {
                    updatedMap.replace(pred, updatedMap.get(pred).updateSuccs(stmt));
                }
            }
        }
        return updatedMap;
    }

    //------ Helper -----//
    private static boolean doesModifyVar(Statement stmt) {
        return stmt instanceof StmtAssignment || stmt instanceof StmtBlock;
    }


    //--------------- Local Value Numbering ---------------//

    //Keeps track of original variable names with their new declarations
    private static HashMap<String, LocalVarDecl> originalLVD = new HashMap<>();
    //Keeps track of how many times a variables has been renamed
    private static HashMap<String, Integer> localValueCounter = new HashMap<>();

    private static void addNewLocalVarMapping(LocalVarDecl lvd) {
        originalLVD.put(lvd.getOriginalName(), lvd);
    }

    private static String getNewUniqueVarName(String varName) {
        if (localValueCounter.containsKey(varName)) {
            localValueCounter.merge(varName, 1, Integer::sum);
            return varName + "_SSA_" + (localValueCounter.get(varName)).toString();
        } else {
            localValueCounter.put(varName, 0);
            return varName + "_SSA_0";
        }
    }

    private static LocalVarDecl createLocalVarDecl(Variable var, Expression expr) {
        String newName = getNewUniqueVarName(var.getOriginalName());
        LocalVarDecl originalDef = originalLVD.get(var.getOriginalName());
        return originalDef.withName(newName).withValue(expr);
    }


//--------------- Lost Copy Problem Handling---------------//

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

    private static LocalVarDecl handleSelfReference(LocalVarDecl selfAssignedVar, Variable var, StmtLabeledSSA
            stmtLabeled) {
        return (stmtLabeled.loopLevel() > 0) ? handleSelfRefWithinLoop(selfAssignedVar, var, stmtLabeled) : handleSimpleSelfRef(selfAssignedVar, var, stmtLabeled);
    }

    private static LocalVarDecl handleSimpleSelfRef(LocalVarDecl selfAssignedVar, Variable var, StmtLabeledSSA
            stmtLabeled) {

        //if outside loop : a = a + 1 become a_i = a_(i-1) + 1
        Variable replacementVar = deriveOldVarName(selfAssignedVar);
        Expression replacementExpr = replaceVariableInExpr(selfAssignedVar.getValue(), var, replacementVar, stmtLabeled);
        return selfAssignedVar.withValue(replacementExpr);
    }

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

        //label them
        StmtLabeledSSA first = stmtLabeled.withNewOriginal(beforeLoopStmt);
        StmtLabeledSSA second = stmtLabeled.withNewOriginal(loopEntryPhiStmt);
        StmtLabeledSSA third = stmtLabeled.withNewOriginal(loopSelfRefStmt);
        StmtLabeledSSA fourth = stmtLabeled.withNewOriginal(loopNextIterStmt);

        //reset their relations and their local value numbering
        Stream.of(first, second, third, fourth).forEach(sl -> {
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
        wireRelations(new LinkedList<>(Arrays.asList(first, second, third, fourth)), blockPred, blockSucc);

        return u2;
    }

    private static Expression replaceVariableInExpr(Expression originalExpr, Variable varToReplace, Variable
            replacementVar, StmtLabeledSSA stmt) {
        ExprVariable updatedVariable = new ExprVariable(variable(replacementVar.getName()));
        //TODO make copies
        if (originalExpr instanceof ExprUnaryOp) {
            Expression updatedVar = replaceVariableInExpr(((ExprUnaryOp) originalExpr).getOperand(), varToReplace, replacementVar, stmt);
            return ((ExprUnaryOp) originalExpr).copy(((ExprUnaryOp) originalExpr).getOperation(), updatedVar);

        } else if (originalExpr instanceof ExprVariable) {
            return (((ExprVariable) originalExpr).getVariable().getOriginalName().equals(varToReplace.getOriginalName())) ? updatedVariable : readLocalVarExpr(originalExpr, stmt).getFirst();

        } else if (originalExpr instanceof ExprBinaryOp) {
            //Should work for interlocked ExprBinary
            List<Expression> operands = ((ExprBinaryOp) originalExpr).getOperands();
            //find and replace variable looked for, apply algorithm to all other operands
            List<Expression> newOperands = operands.stream()
                    .map(o -> (o instanceof ExprVariable && ((ExprVariable) o).getVariable().getOriginalName().equals(varToReplace.getOriginalName())) ? updatedVariable :
                            Objects.requireNonNull(replaceVariableInExpr(o, varToReplace, replacementVar, stmt)))
                    .collect(Collectors.toList());
            return new ExprBinaryOp(((ExprBinaryOp) originalExpr).getOperations(), ImmutableList.from(newOperands));

        } else if (originalExpr instanceof ExprIf) {
            List<Expression> operands = new LinkedList<>(Arrays.asList(((ExprIf) originalExpr).getThenExpr(), ((ExprIf) originalExpr).getElseExpr()));
            //find and replace variable looked for, apply algorithm to all other operands
            List<Expression> newOperands = operands.stream()
                    .map(o -> (o instanceof ExprVariable && ((ExprVariable) o).getVariable().getOriginalName().equals(varToReplace.getOriginalName())) ? updatedVariable :
                            Objects.requireNonNull(replaceVariableInExpr(o, varToReplace, replacementVar, stmt)))
                    .collect(Collectors.toList());
            return new ExprIf(((ExprIf) originalExpr).getCondition(), newOperands.get(0), newOperands.get(1));
        }
        return originalExpr;
    }

    private static LocalVarDecl findVarPredFromLoop(LocalVarDecl varToFind, StmtLabeledSSA originalStmt) {
        StmtLabeledSSA pred = originalStmt.getPredecessors().get(0);
        LocalVarDecl resUntilLoopStart = readVar(pred, variable(varToFind.getOriginalName()), 1, Optional.empty()).getFirst();

        if (resUntilLoopStart != null && !(resUntilLoopStart.getValue() instanceof ExprPhi && ((ExprPhi) resUntilLoopStart.getValue()).getOperands().isEmpty())) {
            return resUntilLoopStart;
        } else {
            //Only keep loop entry block
            StmtLabeledSSA loopEntry = findStatementLabeled(originalStmt, "StmtWhile", Direction.DOWN);
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
                return readVar(loopEntryPreds.get(0), variable(varToFind.getOriginalName()), 0, Optional.empty()).getFirst();
            }
        }
    }

    private static Optional<LocalVarDecl> findVarSuccFromLoop(LocalVarDecl varToFind, StmtLabeledSSA originalStmt) {

        StmtLabeledSSA loopExit = findStatementLabeled(originalStmt, "StmtWhile", Direction.UP);
        List<StmtLabeledSSA> loopExitPredecessors = new LinkedList<>(loopExit.getPredecessors());
        loopExitPredecessors.removeIf(p -> p.loopLevel() <= loopExit.loopLevel());
        StmtLabeledSSA lastLoopStmt = loopExitPredecessors.get(0);

        if (lastLoopStmt.equals(originalStmt)) {
            return Optional.empty();
        }

        LocalVarDecl resFromStmtToLoopEnd = readVar(lastLoopStmt, variable(varToFind.getOriginalName()), 3, Optional.of(originalStmt)).getFirst();
        return (resFromStmtToLoopEnd == null) ? Optional.empty() : Optional.of(resFromStmtToLoopEnd);

    }

    //------ Helper -----//
    private static Variable deriveOldVarName(LocalVarDecl oldVar) {
        //a_i => a_(i-1)
        String oldName = oldVar.getName();
        //get current variable number
        int oldNb = Integer.parseInt(oldName.substring(oldName.length() - 1));
        //replace name with previous number
        String replacementName = oldName.substring(0, oldName.length() - 1) + String.valueOf((oldNb - 1));
        return variable(replacementName);
    }

    private enum Direction {
        UP, DOWN
    }

    private static StmtLabeledSSA findStatementLabeled(StmtLabeledSSA stmtLabeled, String label, Direction dir) {
        //if dir is false = successors
        if (stmtLabeled.getLabel().equals(label)) {
            return stmtLabeled;
        } else {
            if (dir == Direction.UP) {
                return findStatementLabeled(stmtLabeled.getPredecessors().get(0), label, dir);
            } else {
                return findStatementLabeled(stmtLabeled.getSuccessors().get(0), label, dir);
            }
        }
    }


//--------------- SSA Algorithm ---------------//

    private static boolean generateStopCondition(StmtLabeledSSA stmtLabeled, int condType, Optional<
            StmtLabeledSSA> self) {

        boolean res;
        switch (condType) {
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

    private static Pair<LocalVarDecl, Boolean> readVar(StmtLabeledSSA stmt, Variable var, int stopCondType, Optional<
            StmtLabeledSSA> stopStmt) {

        //look for self reference
        Optional<LocalVarDecl> matchingLVD = stmt.getLocalValueNumbers().keySet().stream().filter(l -> l.getOriginalName().equals(var.getOriginalName())).findAny();
        if (matchingLVD.isPresent()) {
            //Locally found
            LocalVarDecl lv = matchingLVD.get();
            if(stmt.getOriginalStmt() instanceof StmtAssignment){
                Expression lvExpr = lv.getValue();
                List<Expression> internalExpr = subExprCollector.collectInternalExpr(lvExpr);
                if (lvExpr instanceof ExprIf) internalExpr.remove(0);

                Optional<Variable> selfRefVar = findSelfReference(internalExpr, lv);
                if (selfRefVar.isPresent()) {
                    return new Pair<>(handleSelfReference(lv, selfRefVar.get(), stmt), true);
                }
            }
            //no self reference
            return new Pair<>(lv, false);
        } else {
            boolean hasToStop = generateStopCondition(stmt, stopCondType, stopStmt);
            //No def found in current Statement
            Optional<Pair<LocalVarDecl, Boolean>> recRes = readVarRec(stmt, var, hasToStop);
            return recRes.orElseGet(() -> new Pair<>(null, false));
        }
    }

    private static Optional<Pair<LocalVarDecl, Boolean>> readVarRec(StmtLabeledSSA stmt, Variable var,
                                                                    boolean hasToStop) {

        if (hasToStop) {
            return Optional.empty();
        }
        if (stmt.getPredecessors().size() == 1) {
            return Optional.of(readVar(stmt.getPredecessors().get(0), var, 0, Optional.empty()));
        } else {
            ExprPhi phiExpr = new ExprPhi(var, ImmutableList.empty());
            //Add Phi to Global value numbering
            LocalVarDecl localVarPhi = createLocalVarDecl(var, phiExpr);
            stmt.addLocalValueNumber(localVarPhi, true);
            localVarPhi = addPhiOperands(localVarPhi, var, stmt.getPredecessors());

            Expression phiResult = localVarPhi.getValue();
            if (phiResult instanceof ExprPhi && !((ExprPhi) phiResult).isUndefined()) {
                stmt.addLocalValueNumber(localVarPhi, true);
            }
            return Optional.of(new Pair<>(localVarPhi, false));
        }
    }

    private static LocalVarDecl addPhiOperands(LocalVarDecl phi, Variable var, List<StmtLabeledSSA> predecessors) {
        LinkedList<LocalVarDecl> phiOperands = new LinkedList<>();

        for (StmtLabeledSSA stmt : predecessors) {
            LocalVarDecl lookedUpVar = readVar(stmt, var, 0, Optional.empty()).getFirst();
            phiOperands.add(lookedUpVar);
            //add Phi to list of users of its operands
            if (lookedUpVar.getValue() instanceof ExprPhi) {
                ((ExprPhi) lookedUpVar.getValue()).addUser(ImmutableList.of(phi));
            }
        }
        ((ExprPhi) phi.getValue()).setOperands(phiOperands);
        return tryRemoveTrivialPhi(phi);
    }

    private static LocalVarDecl tryRemoveTrivialPhi(LocalVarDecl phi) {
        ImmutableList<LocalVarDecl> operands = ((ExprPhi) phi.getValue()).getOperands();
        //Problem in logic if first operand is right, but second is null or a phi
        List<LocalVarDecl> validOperands = new LinkedList<>();
        LocalVarDecl currentOp = null;
        for (LocalVarDecl op : operands) {
            //Unique value or self reference
            if (op.equals(currentOp) || op.equals(phi)) {
                continue;
            }
            if (currentOp != null) {
                return phi;
            }
            currentOp = op;
            validOperands.add(op);
        }

        if (validOperands.size() == 1) {
            return validOperands.get(0);
        }

        //TODO make empty block
        if (currentOp == null) {
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
}
