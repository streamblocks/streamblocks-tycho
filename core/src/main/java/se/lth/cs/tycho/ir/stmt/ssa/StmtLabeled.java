package se.lth.cs.tycho.ir.stmt.ssa;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

/**
 * The type Stmt labeled.
 */
public class StmtLabeled extends Statement {

    private final String label;
    private final Statement originalStmt;
    private final Statement ssaStmt;
    private ImmutableList<StmtLabeled> predecessors;
    private ImmutableList<StmtLabeled> successors;

    /**
     * Instantiates a new Stmt labeled.
     *
     * @param label        the label
     * @param originalStmt the original stmt
     * @param ssaStmt      the ssa stmt
     * @param predecessors the predecessors
     * @param successors   the successors
     */
    public StmtLabeled(String label, Statement originalStmt, Statement ssaStmt, ImmutableList<StmtLabeled> predecessors, ImmutableList<StmtLabeled> successors) {
        this(null, label, originalStmt, ssaStmt, predecessors, successors);
    }

    private StmtLabeled(Statement original, String label, Statement originalStmt, Statement ssaStmt, List<StmtLabeled> predecessors, List<StmtLabeled> successors) {
        super(original);
        this.label = label;
        this.originalStmt = originalStmt;
        this.ssaStmt = ssaStmt == null ? new StmtBlock(ImmutableList.empty(), ImmutableList.empty(), ImmutableList.empty()) : ssaStmt;
        this.predecessors = ImmutableList.from(predecessors);
        this.successors = ImmutableList.from(predecessors);
    }

    /**
     * Copy stmt labeled.
     *
     * @return the stmt labeled
     */
    public StmtLabeled copy() {
        return new StmtLabeled(this.label, this.originalStmt, this.ssaStmt, ImmutableList.from(predecessors), ImmutableList.from(successors));
    }

    private StmtLabeled copy(List<StmtLabeled> predecessors, List<StmtLabeled> successors){
        return new StmtLabeled(this, this.label, this.originalStmt, this.ssaStmt, ImmutableList.from(predecessors), ImmutableList.from(successors));
    }

    /**
     * Equals boolean.
     *
     * @param that the that
     * @return the boolean
     */
    public boolean equals(StmtLabeled that){
        return that.getLabel().equals(this.label) && that.getOrigialStmt().equals(this.originalStmt) && that.getSsaStmt().equals(this.ssaStmt)
                && Lists.equals(that.getPredecessors(), this.predecessors) && Lists.equals(that.getSuccessors(), this.successors);
    }


    /**
     * Update succs stmt labeled.
     *
     * @param successor the successor
     * @return the stmt labeled
     */
    public StmtLabeled updateSuccs(StmtLabeled successor){
        this.successors = (successors.contains(successor)) ? successors : ImmutableList.concat(successors, ImmutableList.of(successor));
        return this;
    }

    /**
     * Update preds stmt labeled.
     *
     * @param predecessor the predecessor
     * @return the stmt labeled
     */
    public StmtLabeled updatePreds(StmtLabeled predecessor){
        this.predecessors = (predecessors.contains(predecessor)) ? predecessors : ImmutableList.concat(predecessors, ImmutableList.of(predecessor));
        return this;
    }

    /**
     * Gets label.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Is buffer block boolean.
     *
     * @return the boolean
     */
    public boolean isBufferBlock() {
        return label.contains("Entry") || label.contains("Exit");
    }

    /**
     * Gets origial stmt.
     *
     * @return the origial stmt
     */
    public Statement getOrigialStmt() {
        return originalStmt;
    }

    /**
     * Gets ssa stmt.
     *
     * @return the ssa stmt
     */
    public Statement getSsaStmt() {
        return ssaStmt;
    }

    /**
     * Gets predecessors.
     *
     * @return the predecessors
     */
    public ImmutableList<StmtLabeled> getPredecessors() {
        return predecessors;
    }

    /**
     * Gets successors.
     *
     * @return the successors
     */
    public ImmutableList<StmtLabeled> getSuccessors() {
        return successors;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        //action.accept(originalStmt);
        action.accept(ssaStmt);
        //predecessors.forEach(action);
        //successors.forEach(action);
    }

    @Override
    public StmtLabeled transformChildren(Transformation transformation) {
        return copy((ImmutableList)predecessors.map(transformation), (ImmutableList)successors.map(transformation));
    }
}