package se.lth.cs.tycho.phase;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.ExprProcReturn;
import se.lth.cs.tycho.ir.stmt.*;
import se.lth.cs.tycho.ir.stmt.ssa.StmtBlockLabeled;
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
            StmtBlockLabeled CFG = create_CFG(proc);
            return proc;
        }

    }

    //TODO define naming convention
    private static String assignLabel(Statement stmt) {
        return "name";
    }

    private static LinkedList<StmtBlockLabeled> iterateSubStmts(List<Statement> stmts, StmtBlockLabeled predecessor, StmtBlockLabeled successor) {
        LinkedList<StmtBlockLabeled> currentBlocks = new LinkedList<>();

        for (int i = 0; i < stmts.size(); i++) {
            currentBlocks.add(new StmtBlockLabeled("FUCK", null, null));
        }

        final ListIterator<StmtBlockLabeled> it = currentBlocks.listIterator();
        StmtBlockLabeled prev = predecessor;
        StmtBlockLabeled newSBL;
        StmtBlockLabeled next;
        while(it.hasNext()){
            Statement currentStmt = stmts.get(it.nextIndex());
            newSBL = it.next();
            next = (it.hasNext()) ? it.next() : successor;
            if (isTerminalStmtBlock(currentStmt)) {
                it.set(newSBL.copy(assignLabel(currentStmt), currentStmt, ImmutableList.empty()));
            } else {
                if(currentStmt instanceof StmtWhile) {
                    it.set(create_WhileBlock((StmtWhile) currentStmt, prev, next));
                }
                else if(currentStmt instanceof StmtIf){
                    it.set(create_IfBlock((StmtIf)currentStmt, prev, next));
                } else { newSBL = null;} //TODO add cases
            }
        }
        return currentBlocks;
    }

    private static LinkedList<StmtBlockLabeled> rewireRelations(LinkedList<StmtBlockLabeled> currentBlocks, StmtBlockLabeled prev, StmtBlockLabeled last) {
        final ListIterator<StmtBlockLabeled> it = currentBlocks.listIterator();
        StmtBlockLabeled previous = prev;
        StmtBlockLabeled current;
        StmtBlockLabeled next;
        while (it.hasNext()) {
            current = it.next();
            next = (it.hasNext()) ? it.next() : last;
            current.setRelations(ImmutableList.of(previous), ImmutableList.of(next));
            previous = current;
            //it.previous();
        }
        return currentBlocks;
    }

    private static StmtBlockLabeled create_IfBlock(StmtIf stmt, StmtBlockLabeled pred, StmtBlockLabeled succ) {

        LinkedList<StmtBlockLabeled> ifBlocks = iterateSubStmts(stmt.getThenBranch(), pred, succ);
        LinkedList<StmtBlockLabeled> elseBlocks = iterateSubStmts(stmt.getElseBranch(), pred, succ);

        StmtBlockLabeled stmtIfLabeled = new StmtBlockLabeled(assignLabel(stmt), stmt, ImmutableList.empty());
        ifBlocks = rewireRelations(ifBlocks, stmtIfLabeled, succ);
        elseBlocks = rewireRelations(elseBlocks, stmtIfLabeled, succ);
        stmtIfLabeled.setRelations(ImmutableList.of(pred),
                ImmutableList.concat(ImmutableList.of(ifBlocks.getFirst()), ImmutableList.of(elseBlocks.getFirst())));


        pred.setRelations(pred.getPredecessors(), ImmutableList.concat(ImmutableList.of(stmtIfLabeled), pred.getSuccessors()));

        ImmutableList.Builder<StmtBlockLabeled> preds = ImmutableList.builder();
        preds.addAll(succ.getPredecessors()).add(ifBlocks.getLast()).add(elseBlocks.getLast());
        succ.setRelations(preds.build(), succ.getSuccessors());

        return stmtIfLabeled;
    }

    private static StmtBlockLabeled create_WhileBlock(StmtWhile stmt, StmtBlockLabeled pred, StmtBlockLabeled succ) {
        ImmutableList<Statement> stmts = stmt.getBody();
        LinkedList<StmtBlockLabeled> currentBlocks = iterateSubStmts(stmts, pred, succ);


        StmtBlockLabeled stmtWhileLabeled = new StmtBlockLabeled(assignLabel(stmt), stmt, ImmutableList.empty());

        //Add the while stmt as both predecessors and successor of its body
        currentBlocks = rewireRelations(currentBlocks, stmtWhileLabeled, stmtWhileLabeled);


        ImmutableList.Builder<StmtBlockLabeled> preds = ImmutableList.builder();
        ImmutableList.Builder<StmtBlockLabeled> succs = ImmutableList.builder();
        preds.add(pred).add(currentBlocks.getLast());
        succs.add(succ).add(currentBlocks.getFirst());
        stmtWhileLabeled.setRelations(preds.build(), succs.build());

        pred.setRelations(pred.getPredecessors(), ImmutableList.concat(ImmutableList.of(stmtWhileLabeled), pred.getSuccessors()));
        succ.setRelations(ImmutableList.concat(succ.getPredecessors(), ImmutableList.of(stmtWhileLabeled)), succ.getSuccessors());

        return stmtWhileLabeled;


    /*    StmtBlockLabeled entryBuffer = new StmtBlockLabeled("buffer", null, ImmutableList.empty())
                .withRelations(ImmutableList.of(pred), ImmutableList.of(finished));
        StmtBlockLabeled outputBuffer = new StmtBlockLabeled("buffer", null, ImmutableList.empty())
                .withRelations(ImmutableList.of(finished), ImmutableList.of(succ));*/
    }


    private static StmtBlockLabeled create_CFG(ExprProcReturn proc) {
        StmtBlock body = (StmtBlock) proc.getBody().get(0);
        ImmutableList<Statement> stmts = body.getStatements();
        StmtBlockLabeled entry = new StmtBlockLabeled("entry", null, ImmutableList.empty());
        StmtBlockLabeled exit = new StmtBlockLabeled("exit", null, ImmutableList.empty());
        LinkedList<StmtBlockLabeled> sub = iterateSubStmts(stmts, entry, exit);
        sub = rewireRelations(sub, entry, exit);
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
