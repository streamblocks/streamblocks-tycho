package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.stmt.*;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.ir.stmt.ssa.ExprPhi;
import se.lth.cs.tycho.ir.stmt.ssa.StmtLabeled;
import se.lth.cs.tycho.ir.stmt.ssa.StmtPhi;
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

    public SsaPhase() {
        stmtExprCollector = MultiJ.from(CollectOrReplaceExprInStmt.class).instance();
        subExprCollector = MultiJ.from(CollectExpressions.class).instance();
        exprVarReplacer = MultiJ.from(ReplaceExprVar.class).instance();
        subStmtCollector = MultiJ.from(CollectOrReplaceSubStmts.class).instance();
    }

    @Override
    public String getDescription() {
        return "Applies SsaPhase transformation to ExprProcReturn";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        CollectOrReplaceExprInStmt stmtExprCollector = MultiJ.from(CollectOrReplaceExprInStmt.class).instance();
        CollectExpressions subExprCollector = MultiJ.from(CollectExpressions.class).instance();
        ReplaceExprVar exprVarReplacer = MultiJ.from(ReplaceExprVar.class).instance();
        CollectOrReplaceSubStmts subStmtCollector = MultiJ.from(CollectOrReplaceSubStmts.class).instance();

        Transformation transformation = MultiJ.from(SsaPhase.Transformation.class)
                .bind("stmtExprCollector").to(stmtExprCollector)
                .bind("subExprCollector").to(subExprCollector)
                .bind("exprVarReplacer").to(exprVarReplacer)
                .bind("subStmtCollector").to(subStmtCollector)
                .instance();

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

        //TODO check
        default List<Expression> collectInternalExpr(ExprIndexer indexer) {
            //TODO
            return new LinkedList<>(Arrays.asList(indexer.getStructure(), indexer.getStructure()));
        }

        default List<Expression> collectInternalExpr(ExprLet let) {
            return new LinkedList<>(Collections.singletonList(let.getBody()));
        }

        default List<Expression> collectInternalExpr(ExprMap map) {
            //TODO
            return new LinkedList<>();
        }

        default List<Expression> collectInternalExpr(ExprLiteral lit) {
            return new LinkedList<>();
        }
    }

    @Module
    interface ReplaceExprVar {
        //Expression exprVarReplacer(Expression original, Map<ExprVariable, LocalVarDecl> replacements);

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

        //default Expression exprVarReplacer(ExprCase.Alternative alt, Map<ExprVariable, LocalVarDecl> replacements){}
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
            //TOOD
            return null;
        }
    }

    @Module
    interface Transformation extends IRNode.Transformation {

        @Binding(BindingKind.INJECTED)
        CollectOrReplaceExprInStmt stmtExprCollector();

        @Binding(BindingKind.INJECTED)
        CollectExpressions subExprCollector();

        @Binding(BindingKind.INJECTED)
        ReplaceExprVar exprVarReplacer();

        @Binding(BindingKind.INJECTED)
        CollectOrReplaceSubStmts subStmtCollector();

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default IRNode apply(ExprProcReturn proc) {
            //StmtLabeled rootCFG = create_CFG(proc, ReturnNode.ROOT);
            Pair<StmtLabeled, StmtLabeled> entryAndExit = create_CFG(proc);
            //recApplySSA(exitCFG);
            //recApplySSAComplete(exitCFG);
            applySSa(entryAndExit);

            return proc;
        }
    }


    private static List<ExprVariable> collectExprVar(IRNode node) {
        List<ExprVariable> reads = new ArrayList<>();
        node.forEachChild(child -> reads.addAll(collectExprVar(child)));
        return reads;
    }

    private static List<Statement> collectStmt(IRNode node) {
        List<Statement> reads = new ArrayList<>();
        node.forEachChild(child -> reads.addAll(collectStmt(child)));
        return reads;
    }


    //--------------- CFG Generation ---------------//

    private static Pair<StmtLabeled, StmtLabeled> create_CFG(ExprProcReturn proc) {
        StmtBlock body = (StmtBlock) proc.getBody().get(0);
        ImmutableList<Statement> stmts;
        if (!(body.getVarDecls().isEmpty() && body.getTypeDecls().isEmpty())) {
            StmtBlock startingBlock = new StmtBlock(body.getTypeDecls(), body.getVarDecls(), body.getStatements());
            stmts = ImmutableList.of(startingBlock);
        } else {
            stmts = body.getStatements();
        }

        StmtLabeled entry = new StmtLabeled("ProgramEntry", null, 0);
        StmtLabeled exit = new StmtLabeled("ProgramExit", null, 0);

        LinkedList<StmtLabeled> sub = iterateSubStmts(stmts, exit, 0);
        wireRelations(sub, entry, exit);

        return new Pair<>(entry, exit);
    }

    private static String assignLabel(Statement stmt) {
        return stmt.getClass().toString().substring(30);
    }

    private static String assignBufferLabel(Statement type, boolean isEntry) {
        return assignLabel(type) + ((isEntry) ? "Entry" : "Exit");
    }

    private static boolean isTerminalStmt(Statement stmt) {
        return isSimpleBlock(stmt) || stmt instanceof StmtCall || stmt instanceof StmtAssignment;
    }

    private static boolean isSimpleBlock(Statement stmt) {
        return stmt instanceof StmtConsume || stmt instanceof StmtWrite || stmt instanceof StmtRead;
    }

    private static LinkedList<StmtLabeled> iterateSubStmts(List<Statement> stmts, StmtLabeled exitBlock, int nestedLoopLevel) {
        LinkedList<StmtLabeled> currentBlocks = new LinkedList<>();

        for (Statement currentStmt : stmts) {

            //TODO add cases
            if (isSimpleBlock(currentStmt)) {
                currentBlocks.add(createSimpleBlock(currentStmt, nestedLoopLevel));
            } else if (currentStmt instanceof StmtWhile) {
                currentBlocks.add(createWhileBlock((StmtWhile) currentStmt, exitBlock, nestedLoopLevel));
            } else if (currentStmt instanceof StmtIf) {
                currentBlocks.add(createIfBlock((StmtIf) currentStmt, exitBlock, nestedLoopLevel));
            } else if (currentStmt instanceof StmtBlock) {
                currentBlocks.add(createStmtBlock((StmtBlock) currentStmt, exitBlock, nestedLoopLevel));
            } else if (currentStmt instanceof StmtCase) {
                currentBlocks.add(createCaseBlock((StmtCase) currentStmt, exitBlock, nestedLoopLevel));
            } else if (currentStmt instanceof StmtForeach) {
                currentBlocks.add(createForEachBlock((StmtForeach) currentStmt, exitBlock, nestedLoopLevel));
            } else if (currentStmt instanceof StmtReturn) {
                currentBlocks.add(createReturnBlock((StmtReturn) currentStmt, exitBlock, nestedLoopLevel));
            } else if (currentStmt instanceof StmtAssignment) {
                currentBlocks.add(createStmtAssignment((StmtAssignment) currentStmt, nestedLoopLevel));
            } else if (currentStmt instanceof StmtCall) {
                currentBlocks.add(createStmtCall((StmtCall) currentStmt, exitBlock, nestedLoopLevel));
            } else throw new NoClassDefFoundError("Unknown Stmt type");
        }
        return currentBlocks;
    }

    private static void wireRelations(LinkedList<StmtLabeled> currentBlocks, StmtLabeled pred, StmtLabeled succ) {

        if (currentBlocks.isEmpty()) {
            pred.setSuccessors(ImmutableList.concat(pred.getSuccessors(), ImmutableList.of(succ)));
            succ.setPredecessors(ImmutableList.concat(succ.getPredecessors(), ImmutableList.of(pred)));
        }

        final ListIterator<StmtLabeled> it = currentBlocks.listIterator();

        StmtLabeled prev = pred;
        StmtLabeled current;
        StmtLabeled next;

        while (it.hasNext()) {
            current = it.next();
            if (it.hasNext()) {
                next = it.next();
                it.previous();
            } else {
                //if last stmt is a return stmt, go to the end of the program
                //TODO CHECK LOGIC
                next = (current.getOriginalStmt() instanceof StmtReturn) ? current.getSuccessors().get(0) : succ;
            }
            if (current.lastIsNull()) {
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

    private static StmtLabeled createReturnBlock(StmtReturn stmt, StmtLabeled exitBlock, int nestedLoopLevel) {
        StmtLabeled stmtRet = new StmtLabeled(assignLabel(stmt), stmt, nestedLoopLevel);
        stmtRet.setSuccessors(ImmutableList.of(exitBlock));
        return stmtRet;
    }

    private static StmtLabeled createIfBlock(StmtIf stmt, StmtLabeled exitBlock, int nestedLoopLevel) {

        StmtLabeled stmtIfLabeled = new StmtLabeled(assignLabel(stmt), stmt, nestedLoopLevel);
        StmtLabeled ifExitBuffer = new StmtLabeled(assignBufferLabel(stmt, false), null, nestedLoopLevel);

        LinkedList<StmtLabeled> ifBlocks = iterateSubStmts(stmt.getThenBranch(), exitBlock, nestedLoopLevel);
        LinkedList<StmtLabeled> elseBlocks = iterateSubStmts(stmt.getElseBranch(), exitBlock, nestedLoopLevel);

        wireRelations(ifBlocks, stmtIfLabeled, ifExitBuffer);
        wireRelations(elseBlocks, stmtIfLabeled, ifExitBuffer);
        stmtIfLabeled.setShortCutToExit(ifExitBuffer);

        return stmtIfLabeled;
    }

    private static StmtLabeled createWhileBlock(StmtWhile stmt, StmtLabeled exitBlock, int nestedLoopLevel) {
        ImmutableList<Statement> stmts = stmt.getBody();
        LinkedList<StmtLabeled> currentBlocks = iterateSubStmts(stmts, exitBlock, nestedLoopLevel + 1);

        StmtLabeled stmtWhileLabeled = new StmtLabeled(assignLabel(stmt), stmt, nestedLoopLevel);

        //Add the while stmt as both predecessors and successor of its body
        wireRelations(currentBlocks, stmtWhileLabeled, stmtWhileLabeled);

        StmtLabeled entryWhile = new StmtLabeled(assignBufferLabel(stmt, true), null, nestedLoopLevel);
        StmtLabeled exitWhile = new StmtLabeled(assignBufferLabel(stmt, false), null, nestedLoopLevel);

        wireRelations(new LinkedList<>(Collections.singletonList(stmtWhileLabeled)), entryWhile, exitWhile);
        entryWhile.setShortCutToExit(exitWhile);
        return entryWhile;
    }

    private static StmtLabeled createSimpleBlock(Statement stmt, int nestedLoopLevel) {
        return new StmtLabeled(assignLabel(stmt), stmt, nestedLoopLevel);
    }

    private static StmtLabeled createStmtBlock(StmtBlock stmt, StmtLabeled exitBlock, int nestedLoopLevel) {

        StmtLabeled stmtBlockLabeled = new StmtLabeled(assignLabel(stmt), stmt, nestedLoopLevel);

        List<LocalVarDecl> localVarDecls = stmt.getVarDecls();
        localVarDecls.forEach(SsaPhase::createNewLocalVar);
        localVarDecls.forEach(v -> stmtBlockLabeled.addLocalValueNumber(v.withName(getNewLocalValueName(v.getOriginalName())), false));

        List<Statement> body = stmt.getStatements();
        LinkedList<StmtLabeled> currentBlocks = iterateSubStmts(body, exitBlock, nestedLoopLevel);

        StmtLabeled stmtBlockExit = new StmtLabeled(assignBufferLabel(stmt, false), null, nestedLoopLevel);
        wireRelations(currentBlocks, stmtBlockLabeled, stmtBlockExit);
        stmtBlockLabeled.setShortCutToExit(stmtBlockExit);

        return stmtBlockLabeled;
    }

    private static StmtLabeled createStmtAssignment(StmtAssignment stmt, int nestedLoopLevel) {
        StmtLabeled stmtAssignLabeled = new StmtLabeled(assignLabel(stmt), stmt, nestedLoopLevel);
        LValue v = stmt.getLValue();
        if (v instanceof LValueVariable) {
            String varName = ((LValueVariable) v).getVariable().getOriginalName();
            LocalVarDecl currentVarDecl = originalLVD.get(varName);
            LocalVarDecl newVarDecl = currentVarDecl.withName(getNewLocalValueName(varName)).withValue(stmt.getExpression());
            stmtAssignLabeled.addLocalValueNumber(newVarDecl, false);
        }
        return stmtAssignLabeled;
    }

    private static StmtLabeled createCaseBlock(StmtCase stmt, StmtLabeled exitBlock, int nestedLoopLevel) {
        StmtLabeled stmtCaseLabeled = new StmtLabeled(assignLabel(stmt), stmt, nestedLoopLevel);
        StmtLabeled stmtCaseExit = new StmtLabeled(assignBufferLabel(stmt, false), null, nestedLoopLevel);

        for (StmtCase.Alternative alt : stmt.getAlternatives()) {
            LinkedList<StmtLabeled> currentBlocks = iterateSubStmts(alt.getStatements(), exitBlock, nestedLoopLevel);
            wireRelations(currentBlocks, stmtCaseLabeled, stmtCaseExit);
        }
        stmtCaseLabeled.setShortCutToExit(stmtCaseExit);
        return stmtCaseLabeled;
    }

    private static StmtLabeled createForEachBlock(StmtForeach stmt, StmtLabeled exitBlock, int nestedLoopLevel) {
        StmtLabeled stmtFELabeled = new StmtLabeled(assignLabel(stmt), stmt, nestedLoopLevel);
        StmtLabeled stmtFEExit = new StmtLabeled(assignBufferLabel(stmt, false), null, nestedLoopLevel);

        LinkedList<StmtLabeled> currentBlocks = iterateSubStmts(stmt.getBody(), exitBlock, nestedLoopLevel);
        wireRelations(currentBlocks, stmtFELabeled, stmtFEExit);

        stmtFELabeled.setShortCutToExit(stmtFEExit);
        return stmtFELabeled;
    }

    private static StmtLabeled createStmtCall(StmtCall stmt, StmtLabeled exitBlock, int nestedLoopLevel) {
        StmtLabeled stmtBlockLabeled = new StmtLabeled(assignLabel(stmt), stmt, nestedLoopLevel);

        List<Expression> args = new LinkedList<>(stmt.getArgs());
        args.removeIf(e -> !(e instanceof ExprLet));

        List<LocalVarDecl> lvd = new LinkedList<>();
        args.forEach(e -> lvd.addAll(((ExprLet) e).getVarDecls()));

        lvd.forEach(l -> {
            createNewLocalVar(l);
            stmtBlockLabeled.addLocalValueNumber(l.withName(getNewLocalValueName(l.getOriginalName())), false);
        });

        return stmtBlockLabeled;
    }


    //--------------- SSA Algorithm Application ---------------//

    private static void applySSa(Pair<StmtLabeled, StmtLabeled> cfg) {
        recApplySSAComplete(cfg.getSecond());
        recRebuildStmt(cfg.getFirst());

    }

    private static void recApplySSAComplete(StmtLabeled stmtLabeled) {
        //Stop recursion at the top of the cfg
        if (stmtLabeled.hasNoPredecessors() || stmtLabeled.hasBeenVisted()) {
            return;
        }

        //read variable in declarations and assignments
        LinkedList<LocalVarDecl> lvd = new LinkedList<>(stmtLabeled.getLocalValueNumbers().keySet());
        lvd.removeIf(lv -> lv.getValue() instanceof ExprPhi || stmtLabeled.getLocalValueNumbers().get(lv)); //if ExprPhi or lvd has already been visited
        if (!lvd.isEmpty()) {
            lvd.forEach(l -> stmtLabeled.addLocalValueNumber(l.withValue(recReadLocalVarExpr(l.getValue(), stmtLabeled).getFirst()), true));
        }

        //read variables in expressions
        if (!stmtLabeled.isBufferBlock()) {
            Statement originalStmt = stmtLabeled.getOriginalStmt();
            if (!modifiesVar(originalStmt)) {
                List<Expression> exprInStmt = stmtExprCollector.collect(originalStmt);
                exprInStmt.forEach(e -> readSubExpr(e, stmtLabeled));
            }
        }

        Statement ssaStmt = applySsaToStatements(stmtLabeled);
        stmtLabeled.setNewOriginal(ssaStmt);
        stmtLabeled.setHasBeenVisted();
        stmtLabeled.getPredecessors().forEach(SsaPhase::recApplySSAComplete);

    }

    private static boolean modifiesVar(Statement stmt) {
        return stmt instanceof StmtAssignment || stmt instanceof StmtBlock;
    }

    private static void readSubExpr(Expression expr, StmtLabeled stmtLabeled) {

        List<Expression> subExpr = subExprCollector.collectInternalExpr(expr);
        if (subExpr.isEmpty()) {
            if (expr instanceof ExprVariable && !stmtLabeled.varHasBeenVisited((ExprVariable) expr)) {

                //TODO handle name of variable put in final result to avoid duplicates
                stmtLabeled.addNewLVNPair((ExprVariable) expr, null);
                Pair<LocalVarDecl, Integer> resPair = resolveSSAName(stmtLabeled, (ExprVariable) expr, 0, new HashSet<>());
                if (resPair.getFirst() != null && resPair.getSecond() >= 0) {
                    stmtLabeled.updateLVNPair((ExprVariable) expr, resPair.getFirst());
                } //TODO check if error handling is needed
            }
            //Expression has no sub expressions and is not a variable
        } else {
            //recursively look through each sub expressions
            subExpr.forEach(subE -> readSubExpr(subE, stmtLabeled));
        }
    }

    private static Pair<LocalVarDecl, Integer> resolveSSAName(StmtLabeled stmt, ExprVariable exprVariable, int recLvl, Set<StmtLabeled> visited) {
        //Reaches top without finding definition
        if (stmt.getLabel().equals("ProgramEntry")) {
            return new Pair<>(null, -1);
        }

        //self reference due to a loop
        if (stmt.varHasBeenVisited(exprVariable) && recLvl != 0) {
            return new Pair<>(null, -2);
        }

        String originalVarRef = exprVariable.getVariable().getOriginalName();
        Optional<LocalVarDecl> localVarDecl = stmt.containsVarDef(originalVarRef);

        //found locally
        if (localVarDecl.isPresent()) {
            return new Pair<>(localVarDecl.get(), recLvl);

        } else {
            //TODO handle case where no assignment of a variable happen to a phi situation variable. This means there's no SSA available
            List<Pair<LocalVarDecl, Integer>> prevVarFound = new LinkedList<>();
            stmt.getPredecessors().forEach(pred -> {
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

            //TODO check logic
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
                    LocalVarDecl lvd = readVar(stmt, exprVariable.getVariable(), 0, Optional.empty()).getFirst();
                    stmt.addLocalValueNumber(lvd, true);
                    //stmt.addLVNResult(exprVariable, lvd);
                    return new Pair<>(lvd, resultPair.getSecond());
                } else {
                    //stmt.addLVNResult(exprVariable, resultPair.getKey());
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

    private static LocalVarDecl getLocalVarDecl(String originalVarName, Set<LocalVarDecl> localVarDecls) {
        for (LocalVarDecl lvd : localVarDecls) {
            if (lvd.getOriginalName().equals(originalVarName)) {
                return lvd;
            }
        }
        throw new IllegalStateException("Missing ssa result for given variable");
    }

    private static Pair<Expression, Boolean> recReadLocalVarExpr(Expression expr, StmtLabeled stmtLabeled) {

        if (expr instanceof ExprLiteral) {
            return new Pair<>(expr, false);

        } else if (expr instanceof ExprVariable) {
            Pair<LocalVarDecl, Boolean> result = readVar(stmtLabeled, ((ExprVariable) expr).getVariable(), 0, Optional.empty());
            ExprVariable ret = ((ExprVariable) expr).copy(variable(result.getFirst().getName()), ((ExprVariable) expr).getOld());
            return new Pair<>(ret, result.getSecond());

        } else if (expr instanceof ExprBinaryOp) {

            List<Pair<Expression, Boolean>> results = ((ExprBinaryOp) expr).getOperands().stream().map(o -> recReadLocalVarExpr(o, stmtLabeled)).collect(Collectors.toList());
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

            Expression newOperand = Objects.requireNonNull(recReadLocalVarExpr(((ExprUnaryOp) expr).getOperand(), stmtLabeled)).getFirst();
            return new Pair<>(((ExprUnaryOp) expr).copy(((ExprUnaryOp) expr).getOperation(), newOperand), false);

        } else if (expr instanceof ExprIf) {

            List<Expression> ifOrElse = subExprCollector.collectInternalExpr(expr);
            ifOrElse.remove(0);
            ifOrElse.replaceAll(e -> recReadLocalVarExpr(e, stmtLabeled).getFirst());
            return new Pair<>(((ExprIf) expr).copy(((ExprIf) expr).getCondition(), ifOrElse.get(0), ifOrElse.get(1)), false);
        }
        return new Pair<>(null, false);
    }

    private static void createPhiBlock(StmtLabeled stmtLabeled){
        Statement original = stmtLabeled.getOriginalStmt();
        if(!(original instanceof StmtAssignment) && !(original instanceof StmtBlock)) {
            if (!stmtLabeled.getLocalValueNumbers().isEmpty()) {
                List<StmtPhi> phiStmts = new LinkedList<>();
                for(LocalVarDecl lvd : stmtLabeled.getLocalValueNumbers().keySet()) {
                    LValueVariable phiLValue = new LValueVariable(variable(lvd.getName()));
                    List<Expression> phiOperands = ((ExprPhi)lvd.getValue()).getOperands().map(op -> new ExprVariable(variable(op.getName())));
                    phiStmts.add(new StmtPhi(phiLValue, phiOperands));
                }
                List<StmtLabeled> phiLabeled = phiStmts.stream().map(phi-> new StmtLabeled(assignLabel(phi), phi, stmtLabeled.loopLevel())).collect(Collectors.toList());
                wirePhiStmts(stmtLabeled, phiLabeled);
                stmtLabeled.setPhiBlockToCreated();
            }
        }
    }

    private static void wirePhiStmts(StmtLabeled originalStmtLabeled, List<StmtLabeled> phis){
        String label = originalStmtLabeled.getLabel();
        switch (label){
            case "StmtIfExit":
            case "StmtCaseExit":{
                StmtLabeled originalSucc = originalStmtLabeled.getSuccessors().get(0);
                wireRelations(new LinkedList<>(phis), originalStmtLabeled, originalSucc);
            }
            break;
            case "StmtWhile":{
                List<StmtLabeled> preds = new LinkedList<>(originalStmtLabeled.getPredecessors());
                preds.removeIf(p -> !p.isBufferBlock());
                StmtLabeled whileEntry = preds.get(0);
                wireRelations(new LinkedList<>(phis), whileEntry, originalStmtLabeled);
            }
            break;
            default: {
                wireRelations(new LinkedList<>(phis), originalStmtLabeled.getPredecessors().get(0), originalStmtLabeled.getSuccessors().get(0));
            }
        }
                //StmtWhileExit, StmtIf always has a single predecessor
    }

    private static Statement applySsaToStatements(StmtLabeled stmtLabeled) {
        if (stmtLabeled.getOriginalStmt() == null || stmtLabeled.lvnIsEmpty()) {
            return stmtLabeled;
        } else {
            Statement originalStmt = stmtLabeled.getOriginalStmt();
            Set<LocalVarDecl> ssaLocalVarDecls = stmtLabeled.getLocalValueNumbers().keySet();
            Statement ssaBlock;

            if (originalStmt instanceof StmtBlock) {
                //Replace LocalVarDecls in statement block
                ssaBlock = ((StmtBlock) originalStmt).withVarDecls(new LinkedList<>(ssaLocalVarDecls));

            } else if (originalStmt instanceof StmtAssignment) {
                //Lost copy blocks are already ssa
                if (stmtLabeled.isLostCopyBlock()) {
                    ssaBlock = originalStmt;
                } else {
                    //Replace ssa result to LValue in assignment
                    String assignedVarName = ((LValueVariable) ((StmtAssignment) originalStmt).getLValue()).getVariable().getOriginalName();
                    LocalVarDecl varDecl = getLocalVarDecl(assignedVarName, ssaLocalVarDecls);
                    ssaBlock = ((StmtAssignment) originalStmt).copy(new LValueVariable(variable(varDecl.getName())), varDecl.getValue());
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

    private static void recRebuildStmt(StmtLabeled stmtLabeled) {


        if (stmtLabeled.getLabel().equals("ProgramExit") || (stmtLabeled.havePhiBlocksBeenCreated() && stmtLabeled.hasBeenRebuilt())) {
            return;
        }

        if(!stmtLabeled.havePhiBlocksBeenCreated()){
            createPhiBlock(stmtLabeled);
        }

        if (!stmtLabeled.hasBeenRebuilt() && !stmtLabeled.isBufferBlock() && !isTerminalStmt(stmtLabeled.getOriginalStmt())) {
            stmtLabeled.setNewOriginal(rebuildStatement(stmtLabeled));
        }

        stmtLabeled.getSuccessors().forEach(SsaPhase::recRebuildStmt);
    }

    private static Statement rebuildStatement(StmtLabeled stmtLabeled) {
        Statement originalStmt = stmtLabeled.getSsaModified();

        List<LinkedList<Statement>> stmtLists = subStmtCollector.collect(originalStmt);
        List<List<Statement>> newBodies = new LinkedList<>();
        for (List<Statement> l : stmtLists) {
            AtomicInteger order = new AtomicInteger();
            Map<Statement, Integer> oldBody = l.stream().collect(Collectors.toMap(s -> s, s -> order.getAndIncrement()));
            Map<Statement, Integer> newBodyMap = recFindBody(stmtLabeled, new HashMap<>(), oldBody, 0);
            List<Statement> newBody = new LinkedList<>(newBodyMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)).keySet());
            newBodies.add(newBody);
        }
        stmtLabeled.setHasBeenRebuilt();
        return subStmtCollector.replace(originalStmt, newBodies);
    }

    private static Map<Statement, Integer> recFindBody(StmtLabeled stmt, Map<Statement, Integer> newBody, Map<Statement, Integer> oldBody, int recLvl) {

        if (stmt.getLabel().equals("ProgramExit")) {
            return newBody;
        }

        Statement original = stmt.getOriginalStmt();
        StmtLabeled next = stmt;
        StmtLabeled scrutinee = stmt;

        if (recLvl != 0) {
            if (stmt.containSubStmts()) {
                if (stmt.isBufferBlock()) {
                    StmtLabeled wrappedStmt = stmt.getSuccessors().get(0);
                    scrutinee = wrappedStmt;
                    original = wrappedStmt.getOriginalStmt();
                }

                next = stmt.getShortCutToExit();
            }

            if (oldBody.containsKey(original)) {
                if (!isTerminalStmt(scrutinee.getOriginalStmt()) && !scrutinee.hasBeenRebuilt()) {
                    scrutinee.setNewOriginal(rebuildStatement(scrutinee));
                }
                newBody.putIfAbsent(scrutinee.getSsaModified(), oldBody.get(original));
                oldBody.remove(original);
            } else {
                //wrong path at a fork
                return newBody;
            }
        }

        if (oldBody.size() != 0) {
            List<StmtLabeled> successors = new LinkedList<>(next.getSuccessors());
            if (recLvl == 0 && stmt.getOriginalStmt() instanceof StmtWhile) {
                successors.removeIf(StmtLabeled::isBufferBlock);
            }
            successors.forEach(succ -> newBody.putAll(recFindBody(succ, newBody, oldBody, recLvl + 1)));
        }

        return newBody;
    }


    //--------------- Local Value Numbering ---------------//

    private static HashMap<String, LocalVarDecl> originalLVD = new HashMap<>();
    private static HashMap<String, Integer> localValueCounter = new HashMap<>();

    private static void createNewLocalVar(LocalVarDecl v) {
        originalLVD.put(v.getOriginalName(), v);
    }

    private static String getNewLocalValueName(String var) {
        if (localValueCounter.containsKey(var)) {
            localValueCounter.merge(var, 1, Integer::sum);
            return var + "_SSA_" + (localValueCounter.get(var)).toString();
        } else {
            localValueCounter.put(var, 0);
            return var + "_SSA_0";
        }
    }

    private static LocalVarDecl createLVDWithVNAndExpr(Variable var, Expression expr) {
        String newName = getNewLocalValueName(var.getOriginalName());
        LocalVarDecl originalDef = originalLVD.get(var.getOriginalName());
        return originalDef.withName(newName).withValue(expr);
    }


//--------------- Lost Copy Problem Handling---------------//

    private static Variable findSelfReference(List<Expression> operands, LocalVarDecl originalStmt) {
        //TODO interlocked binaryOps
        //find variable corresponding to local var declaration in operands of BinaryExpr or return null
        return operands.stream()
                .filter(o -> (o instanceof ExprVariable && originalStmt.getOriginalName().equals(((ExprVariable) o).getVariable().getOriginalName())))
                .findAny()
                .map(expression -> ((ExprVariable) expression).getVariable())
                .orElse(null);
    }

    private static LocalVarDecl handleSelfRefWithinLoop(LocalVarDecl selfAssignedVar, Variable var, StmtLabeled
            stmtLabeled) {

        Variable beforeLoop = variable("u1");
        Variable loopEntry = variable("u0");
        Variable loopExit = variable("x2");
        Variable loopSelfRef = variable("x3");
        Variable loopNextIter = variable("u2");


        //TODO handle acquisition of previous variable def
        //LocalVarDecl u1 = selfAssignedVar.withName("u1").withValue(new ExprVariable(deriveOldVarName(selfAssignedVar)));
        //LocalVarDecl u1 = readVarRec(stmtLabeled, var).getFirst();
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
        StmtLabeled first = stmtLabeled.withNewOriginal(beforeLoopStmt);
        StmtLabeled second = stmtLabeled.withNewOriginal(loopEntryPhiStmt);
        StmtLabeled fourth = stmtLabeled.withNewOriginal(loopSelfRefStmt);
        StmtLabeled fifth = stmtLabeled.withNewOriginal(loopNextIterStmt);

        //reset their relations and their local value numbering
        Stream.of(first, second, fourth, fifth).forEach(sl -> {
            sl.setRelations(Collections.emptyList(), Collections.emptyList());
            sl.lostCopyName();
        });

        //save predecessor and successor of original block and resets its relations empty
        StmtLabeled blockPred = stmtLabeled.getPredecessors().get(0);
        StmtLabeled blockSucc = stmtLabeled.getSuccessors().get(0);
        stmtLabeled.setRelations(Collections.emptyList(), Collections.emptyList());

        //rewire original block predecessor and successor to the new blocks
        List<StmtLabeled> predNewSucc = new LinkedList<>(blockPred.getSuccessors());
        List<StmtLabeled> succNewPred = new LinkedList<>(blockSucc.getPredecessors());
        predNewSucc.remove(stmtLabeled);
        succNewPred.remove(stmtLabeled);
        blockPred.setSuccessors(predNewSucc);
        blockSucc.setPredecessors(succNewPred);

        //wire the new blocks between them and to the original successor and predecessor
        wireRelations(new LinkedList<>(Arrays.asList(first, second)), blockPred, stmtLabeled);
        wireRelations(new LinkedList<>(Arrays.asList(fourth, fifth)), stmtLabeled, blockSucc);

        return u2;
    }

    private static LocalVarDecl findVarPredFromLoop(LocalVarDecl varToFind, StmtLabeled originalStmt) {
        StmtLabeled pred = originalStmt.getPredecessors().get(0);
        LocalVarDecl resUntilLoopStart = readVar(pred, variable(varToFind.getOriginalName()), 1, Optional.empty()).getFirst();

        if (resUntilLoopStart != null && !(resUntilLoopStart.getValue() instanceof ExprPhi && ((ExprPhi) resUntilLoopStart.getValue()).getOperands().isEmpty())) {
            return resUntilLoopStart;
        } else {
            //Only keep loop entry block
            StmtLabeled loopEntry = findStatementLabeled(originalStmt, "StmtWhile", Direction.DOWN);
            List<StmtLabeled> loopEntryPreds = new LinkedList<>(loopEntry.getPredecessors());
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

    private static Optional<LocalVarDecl> findVarSuccFromLoop(LocalVarDecl varToFind, StmtLabeled originalStmt) {

        StmtLabeled loopExit = findStatementLabeled(originalStmt, "StmtWhile", Direction.UP);
        List<StmtLabeled> loopExitPredecessors = new LinkedList<>(loopExit.getPredecessors());
        loopExitPredecessors.removeIf(p -> p.loopLevel() <= loopExit.loopLevel());
        StmtLabeled lastLoopStmt = loopExitPredecessors.get(0);

        if (lastLoopStmt.equals(originalStmt)) {
            return Optional.empty();
        }

        LocalVarDecl resFromStmtToLoopEnd = readVar(lastLoopStmt, variable(varToFind.getOriginalName()), 3, Optional.of(originalStmt)).getFirst();
        return (resFromStmtToLoopEnd == null) ? Optional.empty() : Optional.of(resFromStmtToLoopEnd);

     /*   LocalVarDecl resUntilVar = readVar(startStmt, variable(varToFind.getOriginalName()), 2, Optional.empty()).getFirst();
        LocalVarDecl resUntilLoopStart = readVar(startStmt, variable(varToFind.getOriginalName()), 3, Optional.of(originalStmt)).getFirst();
        LocalVarDecl resFromOrigToLoopStart = readVar(originalStmt.getPredecessors().get(0), variable(varToFind.getOriginalName()), 2, Optional.empty()).getFirst();

        if (resUntilVar == null || resUntilVar.equals(varToFind)) {
            if (resUntilLoopStart.equals(varToFind) || resFromOrigToLoopStart.equals(resUntilLoopStart)) {
                //nothing found
                return Optional.empty();
            } else {
                //same level but different branching
                return Optional.of(resUntilLoopStart);
            }
        } else {
            if (!resUntilLoopStart.equals(varToFind) && !resUntilLoopStart.equals(resFromOrigToLoopStart)) {
                return Optional.of(varToFind.withValue(new ExprPhi(variable(varToFind.getName()), Arrays.asList(resUntilLoopStart, resUntilVar))));
            } else {
                return Optional.of(resUntilVar);
            }
        }*/
    }

    private enum Direction {UP, DOWN}

    private static StmtLabeled findStatementLabeled(StmtLabeled stmtLabeled, String label, Direction dir) {
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

    private static Variable deriveOldVarName(LocalVarDecl oldVar) {
        //a_i => a_(i-1)
        String oldName = oldVar.getName();
        //get current variable number
        int oldNb = Integer.parseInt(oldName.substring(oldName.length() - 1));
        //replace name with previous number
        String replacementName = oldName.substring(0, oldName.length() - 1) + String.valueOf((oldNb - 1));
        return variable(replacementName);
    }

    private static LocalVarDecl handleSimpleSelfReference(LocalVarDecl selfAssignedVar, Variable var, StmtLabeled
            stmtLabeled) {

        //if outside loop : a = a + 1 become a_i = a_(i-1) + 1
        Variable replacementVar = deriveOldVarName(selfAssignedVar);
        Expression replacementExpr = replaceVariableInExpr(selfAssignedVar.getValue(), var, replacementVar, stmtLabeled);
        return selfAssignedVar.withValue(replacementExpr);
    }

    private static LocalVarDecl handleSelfReference(LocalVarDecl selfAssignedVar, Variable var, StmtLabeled
            stmtLabeled) {
        return (stmtLabeled.loopLevel() > 0) ? handleSelfRefWithinLoop(selfAssignedVar, var, stmtLabeled) : handleSimpleSelfReference(selfAssignedVar, var, stmtLabeled);
    }

    private static Expression replaceVariableInExpr(Expression originalExpr, Variable
            varToReplace, Variable replacementVar, StmtLabeled stmt) {
        //TODO add cases for other types of Expressions
        //Should work for interlocked ExprBinary
        if (originalExpr instanceof ExprBinaryOp) {
            List<Expression> operands = ((ExprBinaryOp) originalExpr).getOperands();
            ExprVariable updatedVariable = new ExprVariable(variable(replacementVar.getName()));
            //find and replace variable looked for, apply algorithm to all other operands
            List<Expression> newOperands = operands.stream()
                    .map(o -> (o instanceof ExprVariable && ((ExprVariable) o).getVariable().getOriginalName().equals(varToReplace.getOriginalName())) ? updatedVariable : Objects.requireNonNull(recReadLocalVarExpr(o, stmt)).getFirst())
                    .collect(Collectors.toList());
            return new ExprBinaryOp(((ExprBinaryOp) originalExpr).getOperations(), ImmutableList.from(newOperands));
        }
        return null;
    }


//--------------- SSA Algorithm ---------------//

    private static boolean generateStopCondition(StmtLabeled stmtLabeled, int condType, Optional<StmtLabeled> self) {

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

    private static Pair<LocalVarDecl, Boolean> readVar(StmtLabeled stmt, Variable var, int stopCondType, Optional<StmtLabeled> stopStmt) {

        //look for self reference
        Optional<LocalVarDecl> matchingLVD = stmt.getLocalValueNumbers().keySet().stream().filter(l -> l.getOriginalName().equals(var.getOriginalName())).findAny();
        if (matchingLVD.isPresent()) {
            //Locally found
            LocalVarDecl lv = matchingLVD.get();
            Expression lvExpr = lv.getValue();
            if (lvExpr instanceof ExprBinaryOp || lvExpr instanceof ExprUnaryOp || lvExpr instanceof ExprIf) {
                List<Expression> internalExpr = subExprCollector.collectInternalExpr(lvExpr);
                if (lvExpr instanceof ExprIf) internalExpr.remove(0);

                Variable selfRefVar = findSelfReference(internalExpr, lv);
                if (selfRefVar != null) {
                    return new Pair<>(handleSelfReference(lv, selfRefVar, stmt), true);
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

    private static Optional<Pair<LocalVarDecl, Boolean>> readVarRec(StmtLabeled stmt, Variable var,
                                                                    boolean hasToStop) {

        if (hasToStop) {
            return Optional.empty();
        }
        if (stmt.getPredecessors().size() == 1) {
            return Optional.of(readVar(stmt.getPredecessors().get(0), var, 0, Optional.empty()));
        } else {
            ExprPhi phiExpr = new ExprPhi(var, ImmutableList.empty());
            //Add Phi to Global value numbering
            LocalVarDecl localVarPhi = createLVDWithVNAndExpr(var, phiExpr);
            stmt.addLocalValueNumber(localVarPhi, true);
            localVarPhi = addPhiOperands(localVarPhi, var, stmt.getPredecessors());

            Expression phiResult = localVarPhi.getValue();
            if (phiResult instanceof ExprPhi && !((ExprPhi) phiResult).isUndefined()) {
                stmt.addLocalValueNumber(localVarPhi, true);
            }
            return Optional.of(new Pair<>(localVarPhi, false));
        }
    }

    private static LocalVarDecl addPhiOperands(LocalVarDecl phi, Variable var, List<StmtLabeled> predecessors) {
        LinkedList<LocalVarDecl> phiOperands = new LinkedList<>();

        for (StmtLabeled stmt : predecessors) {
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
        LocalVarDecl currentOp = null;
        ImmutableList<LocalVarDecl> operands = ((ExprPhi) phi.getValue()).getOperands();
        for (LocalVarDecl op : operands) {
            //Unique value or self reference
            if (!op.equals(currentOp) && !op.equals(phi)) {
                if (currentOp != null) {
                    return phi;
                }
            }
            currentOp = op;
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
