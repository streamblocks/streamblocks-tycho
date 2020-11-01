package se.lth.cs.tycho.ir.stmt.ssa;


import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;
import java.util.function.Consumer;


public class StmtLabeled extends Statement {

private final String label;
private ImmutableList<StmtLabeled> predecessors;
private ImmutableList<StmtLabeled> successors;
private final Statement originalStmt;
private StmtLabeled last;

    public StmtLabeled(String label, Statement originalStmt){
        this(null, label, originalStmt, ImmutableList.empty(), ImmutableList.empty(), null);
    }

    public void setRelations(List<StmtLabeled> predecessors, List<StmtLabeled> successors){
        setPredecessors(predecessors);
        setSuccessors(successors);
    }

    public void setPredecessors(List<StmtLabeled> predecessors) {
        this.predecessors = ImmutableList.from(predecessors);
    }

    public void setSuccessors(List<StmtLabeled> successors){
        this.successors = ImmutableList.from(successors);
    }

    public ImmutableList<StmtLabeled> getPredecessors(){
        return predecessors;
    }

    public ImmutableList<StmtLabeled> getSuccessors(){
        return successors;
    }

    public boolean lastIsNull(){
        return last == null;
    }

    public void setLast(StmtLabeled last){
        this.last = last;
    }

    public StmtLabeled getLast(){
        return (last != null) ? last : this;
    }

    private StmtLabeled(Statement original, String label, Statement originalStmt, ImmutableList<StmtLabeled> predecessors, ImmutableList<StmtLabeled> successors, StmtLabeled last) {
        super(original);
        this.label = label;
        this.predecessors = predecessors;
        this.successors = successors;
        this.originalStmt = originalStmt;
        this.last = last;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {

    }

    @Override
    public Statement transformChildren(Transformation transformation) {
        return null;
    }
}
