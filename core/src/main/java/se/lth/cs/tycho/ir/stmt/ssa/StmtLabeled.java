package se.lth.cs.tycho.ir.stmt.ssa;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class StmtLabeled extends Statement {

    private final String label;
    private final Statement originalStmt;
    private final Statement ssaStmt;
    private ImmutableList<StmtLabeled> predecessors;
    private ImmutableList<StmtLabeled> successors;

    public StmtLabeled(String label, Statement originalStmt, Statement ssaStmt, ImmutableList<StmtLabeled> predecessors, ImmutableList<StmtLabeled> successors) {
        this(null, label, originalStmt, ssaStmt, predecessors, successors);
    }

    private StmtLabeled(Statement original, String label, Statement originalStmt, Statement ssaStmt, List<StmtLabeled> predecessors, List<StmtLabeled> successors) {
        super(original);
        this.label = label;
        this.originalStmt = originalStmt;
        this.ssaStmt = ssaStmt;
        this.predecessors = ImmutableList.from(predecessors);
        this.successors = ImmutableList.from(predecessors);
    }

    public StmtLabeled copy() {
        return new StmtLabeled(this.label, this.originalStmt, this.ssaStmt, ImmutableList.from(predecessors), ImmutableList.from(successors));
    }

    private StmtLabeled copy(List<StmtLabeled> predecessors, List<StmtLabeled> successors){
        return new StmtLabeled(this, this.label, this.originalStmt, this.ssaStmt, ImmutableList.from(predecessors), ImmutableList.from(successors));
    }

    public boolean equals(StmtLabeled that){
        return that.getLabel().equals(this.label) && that.getOrigialStmt().equals(this.originalStmt) && that.getSsaStmt().equals(this.ssaStmt)
                && Lists.equals(that.getPredecessors(), this.predecessors) && Lists.equals(that.getSuccessors(), this.successors);
    }

    public StmtLabeled withPredecessors(List<StmtLabeled> predecessors){
        return copy(ImmutableList.from(predecessors), this.successors);
    }

    public StmtLabeled withSuccessors (List<StmtLabeled> successors){
        return copy(this.predecessors, ImmutableList.from(successors));
    }

    public StmtLabeled updateSuccs(StmtLabeled successor){
        this.successors = (successors.contains(successor)) ? successors : ImmutableList.concat(successors, ImmutableList.of(successor));
        return this;
    }

    public StmtLabeled updatePreds(StmtLabeled predecessor){
        this.predecessors = (predecessors.contains(predecessor)) ? predecessors : ImmutableList.concat(predecessors, ImmutableList.of(predecessor));
        return this;
    }

    public String getLabel() {
        return label;
    }

    public boolean isBufferBlock() {
        return label.contains("Entry") || label.contains("Exit");
    }

    public Statement getOrigialStmt() {
        return originalStmt;
    }

    public Statement getSsaStmt() {
        return ssaStmt;
    }

    public ImmutableList<StmtLabeled> getPredecessors() {
        return predecessors;
    }

    public ImmutableList<StmtLabeled> getSuccessors() {
        return successors;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        action.accept(originalStmt);
        action.accept(ssaStmt);
        predecessors.forEach(action);
        successors.forEach(action);
    }

    @Override
    public StmtLabeled transformChildren(Transformation transformation) {
        return copy((ImmutableList)predecessors.map(transformation), (ImmutableList)successors.map(transformation));
    }
}