package se.lth.cs.tycho.ir.stmt.ssa;


import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;


public class StmtLabeled extends Statement {

    private final String label;
    private Statement originalStmt;
    private ImmutableList<StmtLabeled> predecessors;
    private ImmutableList<StmtLabeled> successors;
    private StmtLabeled exit;
    private final LinkedList<LocalVarDecl> valueNumbering;

    //TODO make immutable
    private boolean ssaHasBeenVisted;

    private StmtLabeled(Statement original, String label, Statement originalStmt, ImmutableList<StmtLabeled> predecessors, ImmutableList<StmtLabeled> successors, StmtLabeled exit, LinkedList<LocalVarDecl> valueNumbering, boolean ssaHasBeenVisted) {
        super(original);
        this.label = label;
        this.predecessors = predecessors;
        this.successors = successors;
        this.originalStmt = originalStmt;
        this.exit = exit;
        this.valueNumbering = valueNumbering;
        this.ssaHasBeenVisted = ssaHasBeenVisted;
    }

    public StmtLabeled(String label, Statement originalStmt) {
        this(null, label, originalStmt, ImmutableList.empty(), ImmutableList.empty(), null, new LinkedList<>(), false);
    }

    private StmtLabeled(String label, Statement originalStmt, ImmutableList<StmtLabeled> predecessors, ImmutableList<StmtLabeled> successors, StmtLabeled exit, LinkedList<LocalVarDecl> currentPhiExprs){
        this(null, label, originalStmt, predecessors, successors, exit, currentPhiExprs, false);
    }

    public StmtLabeled withNewOriginal(Statement originalStmt){
        return new StmtLabeled(this.label, originalStmt, this.predecessors, this.successors, this.exit, this.valueNumbering);
    }


    public boolean hasBeenVisted() {
        return ssaHasBeenVisted;
    }

    public void setHasBeenVisted() {
        ssaHasBeenVisted = true;
    }

    public void addLocalValueNumber(LocalVarDecl localValueNumber) {
        this.valueNumbering.add(localValueNumber);
    }

    public List<LocalVarDecl> getLocalValueNumbers() {
        return valueNumbering;
    }

    public Statement getOriginalStmt() {
        return originalStmt;
    }


    public void setOriginalStmt(Statement originalStmt) {
        this.originalStmt = originalStmt;
    }

    public void setRelations(List<StmtLabeled> predecessors, List<StmtLabeled> successors) {
        this.setPredecessors(predecessors);
        this.setSuccessors(successors);
    }

    public void setPredecessors(List<StmtLabeled> predecessors) {
        this.predecessors = ImmutableList.from(predecessors);
    }

    public void setSuccessors(List<StmtLabeled> successors) {
        this.successors = ImmutableList.from(successors);
    }

    public ImmutableList<StmtLabeled> getPredecessors() {
        return predecessors;
    }

    public ImmutableList<StmtLabeled> getSuccessors() {
        return successors;
    }

    public boolean lastIsNull() {
        return exit == null;
    }

    public void setExit(StmtLabeled exit) {
        this.exit = exit;
    }

    public StmtLabeled getExitBlock() {
        return (exit != null) ? exit : this;
    }

    //TODO
    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        predecessors.forEach(action);
        successors.forEach(action);
    }

    @Override
    public Statement transformChildren(Transformation transformation) {
        return null;
    }
}
