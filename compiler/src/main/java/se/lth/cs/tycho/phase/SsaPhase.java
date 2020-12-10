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
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static se.lth.cs.tycho.ir.Variable.variable;

public class SsaPhase implements Phase {

    private static CollectOrReplaceExprInStmt stmtCollector = null;
    private static CollectExpressions exprCollector = null;
    private static ReplaceExprVar replaceExprVar = null;

    public SsaPhase() {
        stmtCollector = MultiJ.from(CollectOrReplaceExprInStmt.class).instance();
        exprCollector = MultiJ.from(CollectExpressions.class).instance();
        replaceExprVar = MultiJ.from(ReplaceExprVar.class).instance();
    }

    @Override
    public String getDescription() {
        return "Applies SsaPhase transformation to ExprProcReturn";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        CollectOrReplaceExprInStmt stmtCollector = MultiJ.from(CollectOrReplaceExprInStmt.class).instance();
        CollectExpressions exprCollector = MultiJ.from(CollectExpressions.class).instance();
        ReplaceExprVar replaceExprVar = MultiJ.from(ReplaceExprVar.class).instance();

        Transformation transformation = MultiJ.from(SsaPhase.Transformation.class)
                .bind("stmtCollector").to(stmtCollector)
                .bind("exprCollector").to(exprCollector)
                .bind("replaceExprVar").to(replaceExprVar)
                .instance();

        return task.transformChildren(transformation);
    }


    //--------------- Utils ---------------//

    @Module
    interface CollectOrReplaceExprInStmt {

        Pair<List<? extends IRNode>, Expression> collectMultipleExpr(Statement s);

        //TODO check if procedure can contain variable
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

        default Statement replaceSingelExpr(StmtCase casee, Expression scrut) {
            return casee.copy(scrut, casee.getAlternatives());
        }

        default Statement replaceListExpr(StmtForeach foreach, List<Expression> filters) {
            return foreach.withFilters(filters);
        }

        default Statement replaceListAndSingleExpr(StmtCall call, Expression procedure, List<Expression> args) {
            return call.copy(procedure, args);
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
            //TODO
            return new LinkedList<>();
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
        //Expression replaceExprVar(Expression original, Map<ExprVariable, LocalVarDecl> replacements);

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
            List<Expression> newOp = exprCollector.collectInternalExpr(binOp);
            newOp.replaceAll(op -> replaceExprVar(op, replacements));
            return new ExprBinaryOp(binOp.getOperations(), ImmutableList.from(newOp));
        }

        default Expression replaceExprVar(ExprCase casee, Map<ExprVariable, LocalVarDecl> replacements) {
            List<Expression> scrut = exprCollector.collectInternalExpr(casee);
            Expression newScrut = replaceExprVar(scrut.get(0), replacements);

            List<ExprCase.Alternative> alts = casee.getAlternatives();
            alts.replaceAll(alt -> new ExprCase.Alternative(alt.getPattern(), alt.getGuards(), replaceExprVar(exprCollector.collectInternalExpr(alt).get(0), replacements)));

            return new ExprCase(newScrut, alts);
        }

        //default Expression replaceExprVar(ExprCase.Alternative alt, Map<ExprVariable, LocalVarDecl> replacements){}
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

        default Expression replaceExprVar(ExprLet let, Map<ExprVariable, LocalVarDecl> replacements) {
            //TODO
            List<LocalVarDecl> lvd = let.getVarDecls();
            return null;
        }

        default Expression replaceExprVar(ExprMap map, Map<ExprVariable, LocalVarDecl> replacements) {
            //TOOD
            return null;
        }
    }

    @Module
    interface Transformation extends IRNode.Transformation {

        @Binding(BindingKind.INJECTED)
        CollectOrReplaceExprInStmt stmtCollector();

        @Binding(BindingKind.INJECTED)
        CollectExpressions exprCollector();

        @Binding(BindingKind.INJECTED)
        ReplaceExprVar replaceExprVar();

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default IRNode apply(ExprProcReturn proc) {
            //StmtLabeled rootCFG = create_CFG(proc, ReturnNode.ROOT);
            StmtLabeled exitCFG = create_CFG(proc, ReturnNode.EXIT);
            //recApplySSA(exitCFG);
            recApplySSAComplete(exitCFG);

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

    private static StmtLabeled create_CFG(ExprProcReturn proc, ReturnNode node) {
        StmtBlock body = (StmtBlock) proc.getBody().get(0);
        ImmutableList<Statement> stmts;
        if (!(body.getVarDecls().isEmpty() && body.getTypeDecls().isEmpty())) {
            StmtBlock startingBlock = new StmtBlock(body.getTypeDecls(), body.getVarDecls(), body.getStatements());
            stmts = ImmutableList.of(startingBlock);
        } else {
            stmts = body.getStatements();
        }

        StmtLabeled entry = new StmtLabeled("ProgramEntry", null, false);
        StmtLabeled exit = new StmtLabeled("ProgramExit", null, false);

        LinkedList<StmtLabeled> sub = iterateSubStmts(stmts, exit, false);
        wireRelations(sub, entry, exit);

        return (node == ReturnNode.ROOT) ? entry : exit;
    }

    private enum ReturnNode {
        ROOT,
        EXIT
    }

    private static String assignLabel(Statement stmt) {
        return stmt.getClass().toString().substring(30);
    }

    private static String assignBufferLabel(Statement type, boolean isEntry) {
        return assignLabel(type) + ((isEntry) ? "Entry" : "Exit");
    }

    private static boolean isTerminalStmt(Statement stmt) {
        return stmt instanceof StmtCall ||
                stmt instanceof StmtConsume ||
                stmt instanceof StmtWrite ||
                stmt instanceof StmtRead;
    }

    private static LinkedList<StmtLabeled> iterateSubStmts(List<Statement> stmts, StmtLabeled exitBlock, boolean areWithinLoop) {
        LinkedList<StmtLabeled> currentBlocks = new LinkedList<>();

        for (Statement currentStmt : stmts) {

            //TODO add cases
            if (isTerminalStmt(currentStmt)) {
                currentBlocks.add(createTerminalBlock(currentStmt, areWithinLoop));
            } else if (currentStmt instanceof StmtWhile) {
                currentBlocks.add(createWhileBlock((StmtWhile) currentStmt, exitBlock, areWithinLoop));
            } else if (currentStmt instanceof StmtIf) {
                currentBlocks.add(createIfBlock((StmtIf) currentStmt, exitBlock, areWithinLoop));
            } else if (currentStmt instanceof StmtBlock) {
                currentBlocks.add(createStmtBlock((StmtBlock) currentStmt, exitBlock, areWithinLoop));
            } else if (currentStmt instanceof StmtCase) {
                currentBlocks.add(createCaseBlock((StmtCase) currentStmt, exitBlock, areWithinLoop));
            } else if (currentStmt instanceof StmtForeach) {
                currentBlocks.add(createForEachBlock((StmtForeach) currentStmt, exitBlock, areWithinLoop));
            } else if (currentStmt instanceof StmtReturn) {
                currentBlocks.add(createReturnBlock((StmtReturn) currentStmt, exitBlock, areWithinLoop));
            } else if (currentStmt instanceof StmtAssignment) {
                currentBlocks.add(createStmtAssignment((StmtAssignment) currentStmt, areWithinLoop));
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

    private static StmtLabeled createReturnBlock(StmtReturn stmt, StmtLabeled exitBlock, boolean isWithinLoop) {
        StmtLabeled stmtRet = new StmtLabeled(assignLabel(stmt), stmt, isWithinLoop);
        stmtRet.setSuccessors(ImmutableList.of(exitBlock));
        return stmtRet;
    }

    private static StmtLabeled createIfBlock(StmtIf stmt, StmtLabeled exitBlock, boolean isWithinLoop) {

        StmtLabeled stmtIfLabeled = new StmtLabeled(assignLabel(stmt), stmt, isWithinLoop);
        StmtLabeled ifExitBuffer = new StmtLabeled(assignBufferLabel(stmt, false), null, isWithinLoop);

        LinkedList<StmtLabeled> ifBlocks = iterateSubStmts(stmt.getThenBranch(), exitBlock, isWithinLoop);
        LinkedList<StmtLabeled> elseBlocks = iterateSubStmts(stmt.getElseBranch(), exitBlock, isWithinLoop);

        wireRelations(ifBlocks, stmtIfLabeled, ifExitBuffer);
        wireRelations(elseBlocks, stmtIfLabeled, ifExitBuffer);
        stmtIfLabeled.setExit(ifExitBuffer);

        return stmtIfLabeled;
    }

    private static StmtLabeled createWhileBlock(StmtWhile stmt, StmtLabeled exitBlock, boolean isWithinLoop) {
        ImmutableList<Statement> stmts = stmt.getBody();
        LinkedList<StmtLabeled> currentBlocks = iterateSubStmts(stmts, exitBlock, true);

        StmtLabeled stmtWhileLabeled = new StmtLabeled(assignLabel(stmt), stmt, isWithinLoop);

        //Add the while stmt as both predecessors and successor of its body
        wireRelations(currentBlocks, stmtWhileLabeled, stmtWhileLabeled);

        StmtLabeled entryWhile = new StmtLabeled(assignBufferLabel(stmt, true), null, isWithinLoop);
        StmtLabeled exitWhile = new StmtLabeled(assignBufferLabel(stmt, false), null, isWithinLoop);

        wireRelations(new LinkedList<>(Collections.singletonList(stmtWhileLabeled)), entryWhile, exitWhile);
        entryWhile.setExit(exitWhile);

        return entryWhile;
    }

    private static StmtLabeled createTerminalBlock(Statement stmt, boolean isWithinLoop) {
        return new StmtLabeled(assignLabel(stmt), stmt, isWithinLoop);
    }

    private static StmtLabeled createStmtBlock(StmtBlock stmt, StmtLabeled exitBlock, boolean isWithinLoop) {

        StmtLabeled stmtBlockLabeled = new StmtLabeled(assignLabel(stmt), stmt, isWithinLoop);

        List<LocalVarDecl> localVarDecls = stmt.getVarDecls();
        localVarDecls.forEach(SsaPhase::createNewLocalVar);
        localVarDecls.forEach(v -> stmtBlockLabeled.addLocalValueNumber(v.withName(getNewLocalValueName(v.getOriginalName())), false));

        List<Statement> body = stmt.getStatements();
        LinkedList<StmtLabeled> currentBlocks = iterateSubStmts(body, exitBlock, isWithinLoop);

        StmtLabeled stmtBlockExit = new StmtLabeled(assignBufferLabel(stmt, false), null, isWithinLoop);
        wireRelations(currentBlocks, stmtBlockLabeled, stmtBlockExit);
        stmtBlockLabeled.setExit(stmtBlockExit);

        return stmtBlockLabeled;
    }

    private static StmtLabeled createStmtAssignment(StmtAssignment stmt, boolean isWithinLoop) {
        StmtLabeled stmtAssignLabeled = new StmtLabeled(assignLabel(stmt), stmt, isWithinLoop);
        LValue v = stmt.getLValue();
        if (v instanceof LValueVariable) {
            String varName = ((LValueVariable) v).getVariable().getOriginalName();
            LocalVarDecl currentVarDecl = originalLVD.get(varName);
            LocalVarDecl newVarDecl = currentVarDecl.withName(getNewLocalValueName(varName)).withValue(stmt.getExpression());
            stmtAssignLabeled.addLocalValueNumber(newVarDecl, false);
        }
        return stmtAssignLabeled;
    }

    private static StmtLabeled createCaseBlock(StmtCase stmt, StmtLabeled exitBlock, boolean isWithinLoop) {
        StmtLabeled stmtCaseLabeled = new StmtLabeled(assignLabel(stmt), stmt, isWithinLoop);
        StmtLabeled stmtCaseExit = new StmtLabeled(assignBufferLabel(stmt, false), null, isWithinLoop);

        for (StmtCase.Alternative alt : stmt.getAlternatives()) {
            LinkedList<StmtLabeled> currentBlocks = iterateSubStmts(alt.getStatements(), exitBlock, isWithinLoop);
            wireRelations(currentBlocks, stmtCaseLabeled, stmtCaseExit);
        }
        stmtCaseLabeled.setExit(stmtCaseExit);
        return stmtCaseLabeled;
    }

    private static StmtLabeled createForEachBlock(StmtForeach stmt, StmtLabeled exitBlock, boolean isWithinLoop) {
        StmtLabeled stmtFELabeled = new StmtLabeled(assignLabel(stmt), stmt, isWithinLoop);
        StmtLabeled stmtFEExit = new StmtLabeled(assignBufferLabel(stmt, false), null, isWithinLoop);

        LinkedList<StmtLabeled> currentBlocks = iterateSubStmts(stmt.getBody(), exitBlock, isWithinLoop);
        wireRelations(currentBlocks, stmtFELabeled, stmtFEExit);

        stmtFELabeled.setExit(stmtFEExit);
        return stmtFELabeled;
    }


    //--------------- SSA Algorithm Application ---------------//

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
                List<Expression> exprInStmt = stmtCollector.collect(originalStmt);
                exprInStmt.forEach(e -> readSubExpr(e, stmtLabeled));
            }
        }

        Statement ssaStmt = applySsaToStatements(stmtLabeled);
        stmtLabeled.setOriginalStmt(ssaStmt);
        stmtLabeled.setHasBeenVisted();
        stmtLabeled.getPredecessors().forEach(SsaPhase::recApplySSAComplete);
    }

    private static boolean modifiesVar(Statement stmt) {
        return stmt instanceof StmtAssignment || stmt instanceof StmtBlock;
    }

    private static void readSubExpr(Expression expr, StmtLabeled stmtLabeled) {

        List<Expression> subExpr = exprCollector.collectInternalExpr(expr);
        if (subExpr.isEmpty()) {
            if (expr instanceof ExprVariable && !stmtLabeled.varHasBeenVisited((ExprVariable) expr)) {

                //TODO handle name of variable put in final result to avoid duplicates
                stmtLabeled.addNewLVNPair((ExprVariable) expr, null);
                Pair<LocalVarDecl, Integer> resPair = resolveSSAName(stmtLabeled, (ExprVariable) expr, 0);
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

    private static Pair<LocalVarDecl, Integer> resolveSSAName(StmtLabeled stmt, ExprVariable exprVariable, int recLvl) {
        //Reaches top without finding definition
        if (stmt.getLabel().equals("ProgramEntry")) {
            return new Pair<>(null, -1);
        }

        //self reference due to a loop
        if (stmt.varHasBeenVisited(exprVariable) && recLvl != 0) {
            return new Pair<>(null, -2);
        }

        String originalVarRef = exprVariable.getVariable().getOriginalName();
        LocalVarDecl localVarDecl = stmt.containsVarDef(originalVarRef);

        //found locally
        if (localVarDecl != null) {
            return new Pair<>(localVarDecl, recLvl);

        } else {
            //TODO handle case where no assignment of a variable happen to a phi situation variable. This means there's no SSA available
            List<Pair<LocalVarDecl, Integer>> prevVarFound = new LinkedList<>();
            stmt.getPredecessors().forEach(pred -> prevVarFound.add(resolveSSAName(pred, exprVariable, recLvl + 1)));

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
                    LocalVarDecl lvd = readVar(stmt, exprVariable.getVariable()).getFirst();
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
            Pair<LocalVarDecl, Boolean> result = readVar(stmtLabeled, ((ExprVariable) expr).getVariable());
            ExprVariable ret = ((ExprVariable) expr).copy(variable(result.getFirst().getName()), ((ExprVariable) expr).getOld());
            return new Pair<>(ret, result.getSecond()) ;

        } else if (expr instanceof ExprBinaryOp) {

            List<Pair<Expression, Boolean>> results = ((ExprBinaryOp) expr).getOperands().stream().map(o -> recReadLocalVarExpr(o, stmtLabeled)).collect(Collectors.toList());
            List<Pair<Expression, Boolean>> newOperands = new LinkedList<>(results);

            Expression selfRef = results.stream().filter(Pair::getSecond).findAny().map(Pair::getFirst).orElse(null);
            //self ref exist and binaryOp must be replaced
            if(selfRef != null){
                return new Pair<>(selfRef, true);
            } else {
                List<Expression> newOps = newOperands.stream().map(Pair::getFirst).collect(Collectors.toList());
                return new Pair<>(((ExprBinaryOp) expr).copy(((ExprBinaryOp) expr).getOperations(), ImmutableList.from(newOps)), false);
            }

        } else if (expr instanceof ExprUnaryOp) {

            Expression newOperand = Objects.requireNonNull(recReadLocalVarExpr(((ExprUnaryOp) expr).getOperand(), stmtLabeled)).getFirst();
            return new Pair<>(((ExprUnaryOp) expr).copy(((ExprUnaryOp) expr).getOperation(), newOperand), false);

        } else if (expr instanceof ExprIf) {

         List<Expression> ifOrElse = exprCollector.collectInternalExpr(expr);
            ifOrElse.remove(0);
            ifOrElse.replaceAll(e -> recReadLocalVarExpr(e, stmtLabeled).getFirst());
            return new Pair<>(((ExprIf) expr).copy(((ExprIf) expr).getCondition(), ifOrElse.get(0), ifOrElse.get(1)), false);
        }
        return new Pair<>(null, false);
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
                List<Expression> stmtExpr = stmtCollector.collect(originalStmt);
                //Apply ssa result
                stmtExpr.replaceAll(e -> replaceExprVar.replaceExprVar(e, ssaLocalValueNumbering));
                //Write the results back
                if (stmtExpr.size() == 1) {
                    ssaBlock = stmtCollector.replaceSingleExpr(originalStmt, stmtExpr.get(0));
                } else {
                    ssaBlock = stmtCollector.replaceListExpr(originalStmt, stmtExpr);
                }
            }
            //Return statement with renamed variable
            return ssaBlock;
        }
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

    private static LocalVarDecl handleSelfRefWithinLoop(LocalVarDecl selfAssignedVar, Variable var, StmtLabeled stmtLabeled) {
        /*u1*/
        Variable beforeLoop = variable("u1");
        /*u0*/
        Variable loopEntry = variable("u0");
        /*x2*/
        Variable loopExit = variable("x2");
        /*u2*/
        Variable loopNextIter = variable("u2");
        Variable loopSelfRef = variable("x3");


        //TODO handle acquisition of previous variable def
        LocalVarDecl u1 = selfAssignedVar.withName("u1").withValue(new ExprVariable(deriveOldVarName(selfAssignedVar)));
        //LocalVarDecl u1 = readVarRec(stmtLabeled, var);
        LocalVarDecl u0 = u1.withName("u0").withValue(null); //u0 = null
        LocalVarDecl x2 = u1.withName("x2").withValue(new ExprVariable(loopEntry)); //x2 = u0
        Expression replacementExpr = replaceVariableInExpr(selfAssignedVar.getValue(), var, loopExit, stmtLabeled);
        LocalVarDecl x3 = selfAssignedVar.withValue(replacementExpr);
        LocalVarDecl u2 = u1.withName("u2").withValue(new ExprVariable(loopSelfRef));
        u0 = u0.withValue(new ExprPhi(loopEntry, ImmutableList.of(u1, u2)));
        //stmtLabeled.addLocalValueNumber(Stream.of(u1, u0, x2, x3, u2).collect(Collectors.toMap(lv -> lv, lv -> true)));
        LocalVarDecl newOriginalVar = x2.withName(selfAssignedVar.getName());
        stmtLabeled.addLocalValueNumber(newOriginalVar, true);
        //stmtLabeled.withNewOriginal(new StmtAssignment(new LValueVariable(var), newOriginalVar.getValue()));


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

    private static Variable deriveOldVarName(LocalVarDecl oldVar) {
        //a_i => a_(i-1)
        String oldName = oldVar.getName();
        //get current variable number
        int oldNb = Integer.parseInt(oldName.substring(oldName.length() - 1));
        //replace name with previous number
        String replacementName = oldName.substring(0, oldName.length() - 1) + String.valueOf((oldNb - 1));
        return variable(replacementName);
    }

    private static LocalVarDecl handleSimpleSelfReference(LocalVarDecl selfAssignedVar, Variable var, StmtLabeled stmtLabeled) {

        //if outside loop : a = a + 1 become a_i = a_(i-1) + 1
        Variable replacementVar = deriveOldVarName(selfAssignedVar);
        Expression replacementExpr = replaceVariableInExpr(selfAssignedVar.getValue(), var, replacementVar, stmtLabeled);
        return selfAssignedVar.withValue(replacementExpr);
    }

    private static LocalVarDecl handleSelfReference(LocalVarDecl selfAssignedVar, Variable var, StmtLabeled stmtLabeled) {
        return (stmtLabeled.isWithinLoop()) ? handleSelfRefWithinLoop(selfAssignedVar, var, stmtLabeled) : handleSimpleSelfReference(selfAssignedVar, var, stmtLabeled);
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

    private static Pair<LocalVarDecl, Boolean> readVar(StmtLabeled stmt, Variable var) {

        //look for self reference
        Optional<LocalVarDecl> matchingLVD = stmt.getLocalValueNumbers().keySet().stream().filter(l -> l.getOriginalName().equals(var.getOriginalName())).findAny();
        if (matchingLVD.isPresent()) {
            //Locally found
            LocalVarDecl lv = matchingLVD.get();
            Expression lvExpr = lv.getValue();
            if (lvExpr instanceof ExprBinaryOp || lvExpr instanceof ExprUnaryOp || lvExpr instanceof ExprIf) {
                List<Expression> internalExpr = exprCollector.collectInternalExpr(lvExpr);
                if (lvExpr instanceof ExprIf) internalExpr.remove(0);

                Variable selfRefVar = findSelfReference(internalExpr, lv);
                if (selfRefVar != null) {
                    return new Pair<>(handleSelfReference(lv, selfRefVar, stmt), true);
                }
            }
            //no self reference
            return new Pair<>(lv,false);
        } else {
            //No def found in current Statement
            return readVarRec(stmt, var);
        }
    }

    private static Pair<LocalVarDecl, Boolean> readVarRec(StmtLabeled stmt, Variable var) {

        if (stmt.getPredecessors().size() == 1) {
            return readVar(stmt.getPredecessors().get(0), var);
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
            return new Pair<>(localVarPhi, false);
        }
    }

    private static LocalVarDecl addPhiOperands(LocalVarDecl phi, Variable var, List<StmtLabeled> predecessors) {
        LinkedList<LocalVarDecl> phiOperands = new LinkedList<>();

        for (StmtLabeled stmt : predecessors) {
            LocalVarDecl lookedUpVar = readVar(stmt, var).getFirst();
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
