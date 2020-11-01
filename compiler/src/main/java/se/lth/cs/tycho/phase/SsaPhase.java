package se.lth.cs.tycho.phase;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
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
            StmtLabeled CFG = create_CFG(proc);
            return proc;
        }

    }

    //TODO define naming convention
    private static String assignLabel(Statement stmt) {
        return "name";
    }

    private static LinkedList<StmtLabeled> iterateSubStmts(List<Statement> stmts) {
        LinkedList<StmtLabeled> currentBlocks = new LinkedList<>();

        for (Statement  currentStmt : stmts) {

            if (isTerminalStmtBlock(currentStmt)) {
                currentBlocks.add(create_SimpleBlock(currentStmt));
            } else {

                if(currentStmt instanceof StmtWhile) {
                    currentBlocks.add(create_WhileBlock((StmtWhile)currentStmt));
                }
                if(currentStmt instanceof StmtIf){
                    currentBlocks.add(create_IfBlock((StmtIf)currentStmt));
                }
                //TODO add cases
            }
        }

        return currentBlocks;
    }

    private static StmtLabeled create_SimpleBlock(Statement stmt){
        StmtLabeled ret = new StmtLabeled(assignLabel(stmt), stmt);
        return ret;
    }

    private static LinkedList<StmtLabeled> rewireRelations(LinkedList<StmtLabeled> currentBlocks, StmtLabeled prev, StmtLabeled succ) {

        final ListIterator<StmtLabeled> it = currentBlocks.listIterator();

        StmtLabeled pred = prev;
        StmtLabeled current;
        StmtLabeled next;

        while (it.hasNext()) {
            current = it.next();
            if(it.hasNext()) {
                next = it.next();
                it.previous();
            } else {
                next = succ;
            }
            if(current.lastIsNull()) {
                current.setRelations(ImmutableList.of(pred), ImmutableList.of(next));
                pred = current;
            } else {
                current.setPredecessors(ImmutableList.of(pred));
                current.getLast().setSuccessors(ImmutableList.of(next));
                pred = current.getLast();
            }
        }
        //set frontier blocks relations
        prev.setSuccessors(ImmutableList.concat(prev.getSuccessors(), ImmutableList.of(currentBlocks.getFirst())));
        succ.setPredecessors(ImmutableList.concat(succ.getPredecessors(), ImmutableList.of(currentBlocks.getLast().getLast())));
        return currentBlocks;
    }

    private static StmtLabeled create_IfBlock(StmtIf stmt) {

        StmtLabeled stmtIfLabeled = new StmtLabeled(assignLabel(stmt), stmt);
        StmtLabeled ifExitBuffer = new StmtLabeled("ExitBuffer", null);

        LinkedList<StmtLabeled> ifBlocks = iterateSubStmts(stmt.getThenBranch());
        LinkedList<StmtLabeled> elseBlocks = iterateSubStmts(stmt.getElseBranch());

        rewireRelations(ifBlocks, stmtIfLabeled, ifExitBuffer);
        rewireRelations(elseBlocks, stmtIfLabeled, ifExitBuffer);
        stmtIfLabeled.setLast(ifExitBuffer);
        //TODO make immutable
        return stmtIfLabeled;
    }

    private static StmtLabeled create_WhileBlock(StmtWhile stmt) {
        ImmutableList<Statement> stmts = stmt.getBody();

        StmtLabeled stmtWhileLabeled = new StmtLabeled(assignLabel(stmt), stmt);
        LinkedList<StmtLabeled> currentBlocks = iterateSubStmts(stmts);

        //Add the while stmt as both predecessors and successor of its body
        rewireRelations(currentBlocks, stmtWhileLabeled, stmtWhileLabeled);

        return stmtWhileLabeled;

    }


    private static StmtLabeled create_CFG(ExprProcReturn proc) {
        StmtBlock body = (StmtBlock) proc.getBody().get(0);
        ImmutableList<Statement> stmts = body.getStatements();
        StmtLabeled entry = new StmtLabeled("entry", null);
        StmtLabeled exit = new StmtLabeled("exit", null);
        LinkedList<StmtLabeled> sub = iterateSubStmts(stmts);
        rewireRelations(sub, entry, exit);
        //StmtBlockLabeled root = new StmtBlockLabeled("entry", entry.getTypeDecls(), entry.getVarDecls(), entry.getStatements(), ImmutableList.empty(),   )
        return entry;
    }

    private static boolean isTerminalStmtBlock(Statement stmt) {
        return  stmt instanceof StmtAssignment ||
                stmt instanceof StmtCall ||
                stmt instanceof StmtConsume ||
                stmt instanceof StmtWrite ||
                stmt instanceof StmtRead;
    }

/*
    private Statement readVar(ImmutableList<LocalVarDecl> localVars, VarDecl var) {
        if(var instanceof ParameterVarDecl && localVars.contains(var)){
            StmtAssignment varValue = new StmtAssignment(new LValueVariable(variable(var.getName())), localVars.get(localVars.indexOf(var)).getDefaultValue());
            return new ExprProcReturn();
        } else {
          return readVarRec(localVars, var);
        }
    }*/

  /*  private Statement readVarRec(ImmutableList<ParameterVarDecl> localVars, VarDecl var){

        }*/
}
