package se.lth.cs.tycho.phase;

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

import java.util.*;

import static se.lth.cs.tycho.ir.Variable.variable;

public class SsaPhase implements Phase {

    @Override
    public String getDescription() {
        return "Applies SsaPhase transformation to ExprProcReturn";
    }


    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        Transformation transformation = MultiJ.from(SsaPhase.Transformation.class)
                .instance();

        return task.transformChildren(transformation);
    }

    @Module
    interface Transformation extends IRNode.Transformation {
        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default IRNode apply(ExprProcReturn proc) {
            //StmtLabeled rootCFG = create_CFG(proc, ReturnNode.ROOT);
            StmtLabeled exitCFG = create_CFG(proc, ReturnNode.EXIT);
            recApplySSA(exitCFG);

            return proc;
        }
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

        StmtLabeled entry = new StmtLabeled("ProgramEntry", null);
        StmtLabeled exit = new StmtLabeled("ProgramExit", null);

        LinkedList<StmtLabeled> sub = iterateSubStmts(stmts);
        wireRelations(sub, entry, exit);

        return (node == ReturnNode.ROOT) ? entry : exit;
    }

    private enum ReturnNode {
        ROOT,
        EXIT
    }

    //TODO define labeling convention
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

    private static LinkedList<StmtLabeled> iterateSubStmts(List<Statement> stmts) {
        LinkedList<StmtLabeled> currentBlocks = new LinkedList<>();

        for (Statement currentStmt : stmts) {

            //TODO add cases
            if (isTerminalStmt(currentStmt)) {
                currentBlocks.add(createTerminalBlock(currentStmt));
            } else if (currentStmt instanceof StmtWhile) {
                currentBlocks.add(createWhileBlock((StmtWhile) currentStmt));
            } else if (currentStmt instanceof StmtIf) {
                currentBlocks.add(createIfBlock((StmtIf) currentStmt));
            } else if (currentStmt instanceof StmtBlock) {
                currentBlocks.add(createStmtBlock((StmtBlock) currentStmt));
            } else if (currentStmt instanceof StmtCase) {
                currentBlocks.add(createCaseBlock((StmtCase) currentStmt));
            } else if (currentStmt instanceof StmtForeach) {
                currentBlocks.add(createForEachBlock((StmtForeach) currentStmt));
            } else if (currentStmt instanceof StmtReturn) {
                //TODO is a return always at the end of a stmtsList?
            } else if (currentStmt instanceof StmtAssignment) {
                currentBlocks.add(createStmtAssignment((StmtAssignment) currentStmt));
            } else throw new NoClassDefFoundError("Unknown Stmt type");
        }
        return currentBlocks;
    }

    //TODO FIX EMPTY CURRENTBLOCKS CASE FOR PRED AND SUCC
    private static void wireRelations(LinkedList<StmtLabeled> currentBlocks, StmtLabeled pred, StmtLabeled succ) {

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
                next = succ;
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

    private static StmtLabeled createIfBlock(StmtIf stmt) {

        StmtLabeled stmtIfLabeled = new StmtLabeled(assignLabel(stmt), stmt);
        StmtLabeled ifExitBuffer = new StmtLabeled(assignBufferLabel(stmt, false), null);

        LinkedList<StmtLabeled> ifBlocks = iterateSubStmts(stmt.getThenBranch());
        LinkedList<StmtLabeled> elseBlocks = iterateSubStmts(stmt.getElseBranch());

        wireRelations(ifBlocks, stmtIfLabeled, ifExitBuffer);
        wireRelations(elseBlocks, stmtIfLabeled, ifExitBuffer);
        stmtIfLabeled.setExit(ifExitBuffer);
        //TODO make immutable
        return stmtIfLabeled;
    }

    private static StmtLabeled createWhileBlock(StmtWhile stmt) {
        ImmutableList<Statement> stmts = stmt.getBody();
        LinkedList<StmtLabeled> currentBlocks = iterateSubStmts(stmts);

        StmtLabeled stmtWhileLabeled = new StmtLabeled(assignLabel(stmt), stmt);

        //Add the while stmt as both predecessors and successor of its body
        wireRelations(currentBlocks, stmtWhileLabeled, stmtWhileLabeled);

        StmtLabeled entryWhile = new StmtLabeled(assignBufferLabel(stmt, true), null);
        StmtLabeled exitWhile = new StmtLabeled(assignBufferLabel(stmt, false), null);

        wireRelations(new LinkedList<>(Collections.singletonList(stmtWhileLabeled)), entryWhile, exitWhile);
        entryWhile.setExit(exitWhile);

        return entryWhile;
    }

    private static StmtLabeled createTerminalBlock(Statement stmt) {
        return new StmtLabeled(assignLabel(stmt), stmt);
    }

    private static StmtLabeled createStmtBlock(StmtBlock stmt) {

        StmtLabeled stmtBlockLabeled = new StmtLabeled(assignLabel(stmt), stmt);

        List<LocalVarDecl> localVarDecls = stmt.getVarDecls();
        localVarDecls.forEach(SsaPhase::createNewLocalVar);
        localVarDecls.forEach(v -> stmtBlockLabeled.addLocalValueNumber(v.withName(getNewLocalValueName(v.getOriginalName()))));

        List<Statement> body = stmt.getStatements();
        LinkedList<StmtLabeled> currentBlocks = iterateSubStmts(body);

        StmtLabeled stmtBlockExit = new StmtLabeled(assignBufferLabel(stmt, false), null);
        wireRelations(currentBlocks, stmtBlockLabeled, stmtBlockExit);
        stmtBlockLabeled.setExit(stmtBlockExit);

        return stmtBlockLabeled;
    }

    private static StmtLabeled createStmtAssignment(StmtAssignment stmt) {
        StmtLabeled stmtAssignLabeled = new StmtLabeled(assignLabel(stmt), stmt);
        LValue v = stmt.getLValue();
        if (v instanceof LValueVariable) {
            String varName = ((LValueVariable) v).getVariable().getOriginalName();
            LocalVarDecl currentVarDecl = originalLVD.get(varName);
            LocalVarDecl newVarDecl = currentVarDecl.withName(getNewLocalValueName(varName)).withValue(stmt.getExpression());
            stmtAssignLabeled.addLocalValueNumber(newVarDecl);
        }
        return stmtAssignLabeled;
    }

    private static StmtLabeled createCaseBlock(StmtCase stmt) {
        StmtLabeled stmtCaseLabeled = new StmtLabeled(assignLabel(stmt), stmt);
        StmtLabeled stmtCaseExit = new StmtLabeled(assignBufferLabel(stmt, false), null);

        for (StmtCase.Alternative alt : stmt.getAlternatives()) {
            LinkedList<StmtLabeled> currentBlocks = iterateSubStmts(alt.getStatements());
            wireRelations(currentBlocks, stmtCaseLabeled, stmtCaseExit);
        }
        stmtCaseLabeled.setExit(stmtCaseExit);
        return stmtCaseLabeled;
    }

    private static StmtLabeled createForEachBlock(StmtForeach stmt) {
        //TODO check
        return createStmtBlock(new StmtBlock(ImmutableList.empty(), ImmutableList.empty(), stmt.getBody()));
    }


    //--------------- SSA Algorithm Application ---------------//

    private static void recApplySSA(StmtLabeled stmtLabeled) {
        //Stop recursion at the top of the cfg
        if (stmtLabeled.getPredecessors().isEmpty() || stmtLabeled.hasBeenVisted()) {
            return;
        }

        //if in Assignment or Block
        LinkedList<LocalVarDecl> lvd = new LinkedList<>(stmtLabeled.getLocalValueNumbers());
        lvd.removeIf(lv -> lv.getValue() instanceof ExprPhi);
        if (!lvd.isEmpty()) {
            lvd.forEach(l -> stmtLabeled.addLocalValueNumber(l.withValue(recReadLocalVarExpr(l.getValue(), stmtLabeled))));
        }
        stmtLabeled.setHasBeenVisted(); //TODO Problem for While EDIT : Fix with whileExitBlock
        stmtLabeled.getPredecessors().forEach(SsaPhase::recApplySSA);

    /*    Statement originalStmt = stmtLabeled.getOriginalStmt();
        if (containsExpression(originalStmt)) {

            //Get expression and verify that it's not an ExprLiteral as they are terminal
            List<Expression> expr = getExpressions(originalStmt);

            removeLiterals(expr);

            //If ExprVar were found, apply SSA
            List<ExprVariable> exprVar = findExprVar(expr);

            if (!exprVar.isEmpty()) {

                List<LocalVarDecl> ssaResult = exprVar.stream().map(ev -> readVar(stmtLabeled, ev.getVariable())).collect(Collectors.toList());

                //TODO create new stmtlabeled with new original stmt containing ssa result
                //stmtLabeled.withNewOriginal(setSsaResult(originalStmt, ssaResult));
                //stmtLabeled.setOriginalStmt(setSsaResult(originalStmt, ssaResult));
            }
        }*/
        //recursively apply ssa for all predecessors
    }


    private static Expression recReadLocalVarExpr(Expression expr, StmtLabeled stmtLabeled) {

        if (expr instanceof ExprLiteral) {
            return expr;

        } else if (expr instanceof ExprVariable) {

            //TODO Handle self reference
            LocalVarDecl result = readVar(stmtLabeled, ((ExprVariable) expr).getVariable());
            //result = result.withValue(new ExprVariable(variable(result.getName())));
            ExprVariable newVar = ((ExprVariable) expr).copy(variable(result.getName()), ((ExprVariable) expr).getOld());
            //replace old value
            //stmtLabeled.addLocalValueNumber(result);
            return newVar;

        } else if (expr instanceof ExprBinaryOp) {
            LinkedList<Expression> newOperands = new LinkedList<>(((ExprBinaryOp) expr).getOperands());
            newOperands.replaceAll(o -> recReadLocalVarExpr(o, stmtLabeled));
            ExprBinaryOp newVar = ((ExprBinaryOp) expr).copy(((ExprBinaryOp) expr).getOperations(), ImmutableList.from(newOperands));
            return newVar;

        } else if (expr instanceof ExprUnaryOp) {
            Expression newOperand = recReadLocalVarExpr(((ExprUnaryOp) expr).getOperand(), stmtLabeled);
            ExprUnaryOp newVar = ((ExprUnaryOp) expr).copy(((ExprUnaryOp) expr).getOperation(), newOperand);
            return newVar;
            //TODO
        } else {
            return null;
        }
    }

    //Respects Statements immutability
    private static Statement setSsaResult(Statement stmt, List<Expression> ssaExpr) {
        if (stmt instanceof StmtAssignment) {
            return ((StmtAssignment) stmt).copy(((StmtAssignment) stmt).getLValue(), ssaExpr.get(0));
        } else if (stmt instanceof StmtCall) {
            return ((StmtCall) stmt).copy(((StmtCall) stmt).getProcedure(), ssaExpr);
        } else if (stmt instanceof StmtForeach) {
            return ((StmtForeach) stmt).copy(((StmtForeach) stmt).getGenerator(), ssaExpr, ((StmtForeach) stmt).getBody());
        } else if (stmt instanceof StmtIf) {
            return ((StmtIf) stmt).copy(ssaExpr.get(0), ((StmtIf) stmt).getThenBranch(), ((StmtIf) stmt).getElseBranch());
        } else if (stmt instanceof StmtReturn) {
            return ((StmtReturn) stmt).copy(ssaExpr.get(0));
        } else if (stmt instanceof StmtWhile) {
            return ((StmtWhile) stmt).copy(ssaExpr.get(0), ((StmtWhile) stmt).getBody());
        } else {
            return stmt;
        }
    }


//TODO check if additions are needed

    private static boolean containsExpression(Statement stmt) {
        return stmt instanceof StmtAssignment ||
                stmt instanceof StmtCall ||
                stmt instanceof StmtForeach ||
                stmt instanceof StmtIf ||
                stmt instanceof StmtReturn ||
                stmt instanceof StmtWhile;
    }

    private static List<Expression> getExpressions(Statement stmt) {
        LinkedList<Expression> expr = new LinkedList<>();
        if (stmt instanceof StmtAssignment) {
            expr.add(((StmtAssignment) stmt).getExpression());
        } else if (stmt instanceof StmtCall) {
            expr.addAll(((StmtCall) stmt).getArgs());
        } else if (stmt instanceof StmtForeach) {
            expr.addAll(((StmtForeach) stmt).getFilters());
        } else if (stmt instanceof StmtIf) {
            expr.add(((StmtIf) stmt).getCondition());
        } else if (stmt instanceof StmtReturn) {
            expr.add(((StmtReturn) stmt).getExpression());
        } else if (stmt instanceof StmtWhile) {
            expr.add(((StmtWhile) stmt).getCondition());
        } else if (stmt instanceof StmtBlock) {
            ((StmtBlock) stmt).getVarDecls().forEach(vd -> expr.add(vd.getValue()));
        }
        return expr;
    }

    //TODO check for all cases if expr are needed or not
//TODO check if potential infinite loops
    private static List<ExprVariable> recFindExprVar(Expression expr) {
        List<ExprVariable> exprVar = new LinkedList<>();
        List<Expression> subExpr = new LinkedList<>();

        if (expr instanceof ExprApplication) {
            subExpr.addAll(((ExprApplication) expr).getArgs());

        } else if (expr instanceof ExprBinaryOp) {
            subExpr.addAll(((ExprBinaryOp) expr).getOperands());

        } else if (expr instanceof ExprCase) {
            subExpr.add(((ExprCase) expr).getScrutinee());
            ((ExprCase) expr).getAlternatives().forEach(a -> subExpr.add(a.getExpression()));

        } else if (expr instanceof ExprComprehension) { //TODO check collection
            subExpr.addAll(((ExprComprehension) expr).getFilters());

        } else if (expr instanceof ExprDeref) {
            subExpr.add(((ExprDeref) expr).getReference());

        } else if (expr instanceof ExprIf) {
            subExpr.addAll(new ArrayList<>(Arrays.asList(((ExprIf) expr).getCondition(), ((ExprIf) expr).getElseExpr(), ((ExprIf) expr).getThenExpr())));

        } else if (expr instanceof ExprLambda) {
            subExpr.add(((ExprLambda) expr).getBody());

        } else if (expr instanceof ExprList) {
            subExpr.addAll(((ExprList) expr).getElements());

        } else if (expr instanceof ExprSet) {
            subExpr.addAll(((ExprSet) expr).getElements());

        } else if (expr instanceof ExprTuple) {
            subExpr.addAll(((ExprTuple) expr).getElements());

        } else if (expr instanceof ExprTypeConstruction) {
            subExpr.addAll(((ExprTypeConstruction) expr).getArgs());

        } else if (expr instanceof ExprUnaryOp) {
            subExpr.add(((ExprUnaryOp) expr).getOperand());

        } else if (expr instanceof ExprVariable) {
            exprVar.add((ExprVariable) expr);
            return exprVar;

        }//TODO check all necessary cases;
        else if (expr instanceof ExprLet) {
            //TODO
        } else if (expr instanceof ExprRef) {
            //TODO
        }
        subExpr.forEach(e -> exprVar.addAll(recFindExprVar(e)));
        return exprVar;
    }

    private static List<ExprVariable> findExprVar(List<Expression> expr) {
        List<ExprVariable> exprVar = new LinkedList<>();
        expr.forEach(e -> exprVar.addAll(recFindExprVar(e)));
        return exprVar;
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


//--------------- SSA Algorithm ---------------//

    private static Expression replaceVariableInExpr(Expression originalExpr, LocalVarDecl varToReplace) {
        return null;
    }

    private static LocalVarDecl handleSelfReference(StmtLabeled stmt, Variable var, LocalVarDecl selfAssignedVar) {

        //TODO check for multiple self references

        LocalVarDecl temp = new LocalVarDecl(selfAssignedVar.getAnnotations(), selfAssignedVar.getType(), "u", null, selfAssignedVar.isConstant());
        createNewLocalVar(temp);

        Variable loopStartVarName = variable(temp.getName());
        LocalVarDecl loopStartVar = temp.withName(getNewLocalValueName(temp.getOriginalName()));  //u0
        LocalVarDecl exitVar = temp.withName(getNewLocalValueName(temp.getName())).withValue(new ExprVariable(variable(loopStartVar.getName()))); //u1

        //replace problematic reference
        Expression oldExpr = selfAssignedVar.getValue();
        Expression newExpr = replaceVariableInExpr(oldExpr, exitVar);
        LocalVarDecl updatedSelfAssignedVar = selfAssignedVar.withValue(newExpr);

        LocalVarDecl loopEndVar = temp.withName(getNewLocalValueName(temp.getName())).withValue(new ExprVariable(variable(updatedSelfAssignedVar.getName()))); //u2

        LocalVarDecl predecessorVariable = readVarRec(stmt, var);
        ExprPhi phiWithSelf = new ExprPhi(loopStartVarName, ImmutableList.from(Arrays.asList(predecessorVariable, loopEndVar)));
        loopStartVar = loopStartVar.withValue(phiWithSelf);

        //add all localVarDeclarations resulting from the operation
        stmt.addLocalValueNumber(Arrays.asList(loopStartVar, loopEndVar, updatedSelfAssignedVar, exitVar));

        //redirect to exitVar
        return exitVar;
    }

    //TODO check which type of "var" to look for. Tried with Variable
    private static LocalVarDecl readVar(StmtLabeled stmt, Variable var) {

        List<LocalVarDecl> localVarDecls = stmt.getLocalValueNumbers();
        localVarDecls.removeIf(l -> !(l.getName().equals(var.getName())));
        //variable assignment contains self reference
        if (!localVarDecls.isEmpty()) {
            return handleSelfReference(stmt, var, localVarDecls.get(0));
        }
            //Locally found
        List<LocalVarDecl> localValueNumbers = stmt.getLocalValueNumbers();
        for (LocalVarDecl lvd : localValueNumbers) {
            if (var.getOriginalName().equals(lvd.getOriginalName())) {
                return lvd;
            }
        }
        //No def found in current Statement
        return readVarRec(stmt, var);
    }

    private static LocalVarDecl readVarRec(StmtLabeled stmt, Variable var) {

        if (stmt.getPredecessors().size() == 1) {
            return readVar(stmt.getPredecessors().get(0), var);
        } else {
            ExprPhi phiExpr = new ExprPhi(var, ImmutableList.empty());
            //Add Phi to Global value numbering
            LocalVarDecl localVarPhi = createLVDWithVNAndExpr(var, phiExpr);
            localVarPhi = addPhiOperands(localVarPhi, var, stmt.getPredecessors());

            Expression phiResult = localVarPhi.getValue();
            if (phiResult instanceof ExprPhi && !((ExprPhi) phiResult).isUndefined()) {
                stmt.addLocalValueNumber(localVarPhi);
            }
            return localVarPhi;
        }
    }

    private static LocalVarDecl addPhiOperands(LocalVarDecl phi, Variable var, List<StmtLabeled> predecessors) {
        LinkedList<LocalVarDecl> phiOperands = new LinkedList<>();

        for (StmtLabeled stmt : predecessors) {
            LocalVarDecl lookedUpVar = readVar(stmt, var);
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
