package se.lth.cs.tycho.phase;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprProcReturn;
import se.lth.cs.tycho.ir.stmt.*;
import se.lth.cs.tycho.ir.stmt.ssa.StmtLabeled;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

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
            //ImmutableList<ParameterVarDecl> paramVarDecl = proc.getValueParameters();
            ImmutableList<Statement> stmts = proc.getBody();
            StmtLabeled rootCFG = create_CFG(proc);
            return proc;
        }

    }

    //TODO define naming convention
    private static String assignLabel(Statement stmt) {
        return stmt.getClass().toString().substring(30);
    }


    //TODO add cases
    private static LinkedList<StmtLabeled> iterateSubStmts(List<Statement> stmts) {
        LinkedList<StmtLabeled> currentBlocks = new LinkedList<>();

        for (Statement currentStmt : stmts) {

            if (isTerminalStmtBlock(currentStmt)) {
                currentBlocks.add(create_SimpleBlock(currentStmt));
            } else if (currentStmt instanceof StmtWhile) {
                currentBlocks.add(create_WhileBlock((StmtWhile) currentStmt));
            } else if (currentStmt instanceof StmtIf) {
                currentBlocks.add(create_IfBlock((StmtIf) currentStmt));
            } else if (currentStmt instanceof StmtBlock) {
            } else if (currentStmt instanceof StmtCase) {
            } else if (currentStmt instanceof StmtForeach) {
            } else if (currentStmt instanceof StmtReturn) {
            }
        }
        return currentBlocks;
    }

    private static StmtLabeled create_SimpleBlock(Statement stmt) {
        StmtLabeled ret = new StmtLabeled(assignLabel(stmt), stmt);
        return ret;
    }

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
                current.setRelations(ImmutableList.of(prev), ImmutableList.of(next));
                prev = current;
            } else {
                current.setPredecessors(ImmutableList.of(prev));
                current.getExitBlock().setSuccessors(ImmutableList.of(next));
                prev = current.getExitBlock();
            }
        }
        //set frontier blocks relations
        pred.setSuccessors(ImmutableList.concat(pred.getSuccessors(), ImmutableList.of(currentBlocks.getFirst())));
        succ.setPredecessors(ImmutableList.concat(succ.getPredecessors(), ImmutableList.of(currentBlocks.getLast().getExitBlock())));
    }

    private static StmtLabeled create_IfBlock(StmtIf stmt) {

        StmtLabeled stmtIfLabeled = new StmtLabeled(assignLabel(stmt), stmt);
        StmtLabeled ifExitBuffer = new StmtLabeled("ExitBuffer", null);

        LinkedList<StmtLabeled> ifBlocks = iterateSubStmts(stmt.getThenBranch());
        LinkedList<StmtLabeled> elseBlocks = iterateSubStmts(stmt.getElseBranch());

        wireRelations(ifBlocks, stmtIfLabeled, ifExitBuffer);
        wireRelations(elseBlocks, stmtIfLabeled, ifExitBuffer);
        stmtIfLabeled.setExit(ifExitBuffer);
        //TODO make immutable
        return stmtIfLabeled;
    }

    private static StmtLabeled create_WhileBlock(StmtWhile stmt) {
        ImmutableList<Statement> stmts = stmt.getBody();
        LinkedList<StmtLabeled> currentBlocks = iterateSubStmts(stmts);

        StmtLabeled stmtWhileLabeled = new StmtLabeled(assignLabel(stmt), stmt);

        //Add the while stmt as both predecessors and successor of its body
        wireRelations(currentBlocks, stmtWhileLabeled, stmtWhileLabeled);

        return stmtWhileLabeled;


    /*    StmtBlockLabeled entryBuffer = new StmtBlockLabeled("buffer", null, ImmutableList.empty())
                .withRelations(ImmutableList.of(pred), ImmutableList.of(finished));
        StmtBlockLabeled outputBuffer = new StmtBlockLabeled("buffer", null, ImmutableList.empty())
                .withRelations(ImmutableList.of(finished), ImmutableList.of(succ));*/
    }


    private static StmtLabeled create_CFG(ExprProcReturn proc) {
        StmtBlock body = (StmtBlock) proc.getBody().get(0);
        ImmutableList<Statement> stmts = body.getStatements();

        StmtLabeled entry = new StmtLabeled("entry", null);
        StmtLabeled exit = new StmtLabeled("exit", null);

        LinkedList<StmtLabeled> sub = iterateSubStmts(stmts);
        wireRelations(sub, entry, exit);

        return entry;
    }

    private static boolean isTerminalStmtBlock(Statement stmt) {
        return stmt instanceof StmtAssignment ||
                stmt instanceof StmtCall ||
                stmt instanceof StmtConsume ||
                stmt instanceof StmtWrite ||
                stmt instanceof StmtRead;
    }


//--------------- SSA Algorithm ---------------//

/*    private Statement readVar(StmtLabeled stmt, VarDecl var) {
        Statement originalStmt = stmt.getOriginalStmt();


    }

    private Statement readVarRec(ImmutableList<ParameterVarDecl> localVars, VarDecl var){

        }*/
}
