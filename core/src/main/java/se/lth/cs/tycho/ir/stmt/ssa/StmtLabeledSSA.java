package se.lth.cs.tycho.ir.stmt.ssa;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The type Stmt labeled ssa.
 */
public class StmtLabeledSSA extends Statement {

    private String label;
    //cfg
    private Statement originalStmt;
    private ImmutableList<StmtLabeledSSA> predecessors;
    private ImmutableList<StmtLabeledSSA> successors;
    private StmtLabeledSSA shortCutToExit;
    private final int nestedLoopLevel;
    private final String uniqueID = UUID.randomUUID().toString();
    //ssa
    private final Map<LocalVarDecl, Boolean> valueNumbering;
    private final Map<ExprVariable, LocalVarDecl> exprValueNumbering;
    private boolean ssaHasBeenVisited;
    private Statement ssaModifiedStmt;
    //reconstruction
    private boolean hasBeenRebuilt = false;
    private boolean phiBlockCreated = false;

    private StmtLabeledSSA(Statement original, String label, Statement originalStmt, ImmutableList<StmtLabeledSSA> predecessors, ImmutableList<StmtLabeledSSA> successors, StmtLabeledSSA exit,
                           Map<LocalVarDecl, Boolean> valueNumbering, Map<ExprVariable, LocalVarDecl> exprValueNumbering, boolean ssaHasBeenVisted, int nestedLoopLevel, Statement ssaModifiedStmt) {
        super(original);
        this.label = label;
        this.predecessors = predecessors;
        this.successors = successors;
        this.originalStmt = originalStmt;
        this.shortCutToExit = exit;
        this.valueNumbering = valueNumbering;
        this.exprValueNumbering = exprValueNumbering;
        this.ssaHasBeenVisited = ssaHasBeenVisted;
        this.nestedLoopLevel = nestedLoopLevel;
        this.ssaModifiedStmt = ssaModifiedStmt;
    }

    /**
     * Instantiates a new Stmt labeled ssa.
     *
     * @param label           the label
     * @param originalStmt    the original stmt
     * @param nestedLoopLevel the nested loop level
     */
    public StmtLabeledSSA(String label, Statement originalStmt, int nestedLoopLevel) {
        this(null, label, originalStmt, ImmutableList.empty(), ImmutableList.empty(), null, new HashMap<>(), new HashMap<>(), false, nestedLoopLevel, null);
    }

    private StmtLabeledSSA(String label, Statement originalStmt, ImmutableList<StmtLabeledSSA> predecessors, ImmutableList<StmtLabeledSSA> successors, StmtLabeledSSA exit,
                           Map<LocalVarDecl, Boolean> currentPhiExprs, Map<ExprVariable, LocalVarDecl> exprValueNumbering, boolean ssaHasBeenVisted, int nestedLoopLevel, Statement ssaModifiedStmt) {
        this(null, label, originalStmt, predecessors, successors, exit, currentPhiExprs, exprValueNumbering, ssaHasBeenVisted, nestedLoopLevel, ssaModifiedStmt);
    }

    /**
     * With new original stmt labeled ssa.
     *
     * @param ssaModifiedStmt the ssa modified stmt
     * @return the stmt labeled ssa
     */
    public StmtLabeledSSA withNewOriginal(Statement ssaModifiedStmt) {
        return new StmtLabeledSSA(this.label, this.originalStmt, this.predecessors, this.successors, this.shortCutToExit, this.valueNumbering, this.exprValueNumbering, this.ssaHasBeenVisited, this.nestedLoopLevel, ssaModifiedStmt);
    }

    /**
     * Have phi blocks been created boolean.
     *
     * @return the boolean
     */
    public boolean havePhiBlocksBeenCreated() {
        return phiBlockCreated;
    }

    /**
     * Sets phi block to created.
     */
    public void setPhiBlockToCreated() {
        this.phiBlockCreated = true;
    }

    /**
     * Lost copy name.
     */
    public void lostCopyName() {
        this.label += "_lostCopyVar";
    }

    /**
     * Is lost copy block boolean.
     *
     * @return the boolean
     */
    public boolean isLostCopyBlock() {
        return label.endsWith("_lostCopyVar");
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
     * Loop level int.
     *
     * @return the int
     */
    public int loopLevel() {
        return nestedLoopLevel;
    }

    /**
     * Has been visted boolean.
     *
     * @return the boolean
     */
    public boolean hasBeenVisted() {
        return ssaHasBeenVisited;
    }

    /**
     * Sets has been visted.
     */
    public void setHasBeenVisted() {
        ssaHasBeenVisited = true;
    }

    /**
     * Gets expr value numbering.
     *
     * @return the expr value numbering
     */
    public Map<ExprVariable, LocalVarDecl> getExprValueNumbering() {
        return new HashMap<>(exprValueNumbering);
    }

    /**
     * Lvn is empty boolean.
     *
     * @return the boolean
     */
    public boolean lvnIsEmpty() {
        return exprValueNumbering.isEmpty() && valueNumbering.isEmpty();
    }

    /**
     * Add local value number.
     *
     * @param localValueNumber the local value number
     * @param hasBeenVisited   the has been visited
     */
    public void addLocalValueNumber(LocalVarDecl localValueNumber, boolean hasBeenVisited) {
        List<LocalVarDecl> containedVar = valueNumbering.keySet().stream().filter(lv -> lv.getName().equals(localValueNumber.getName())).collect(Collectors.toList());
        if (!containedVar.isEmpty()) {
            valueNumbering.remove(containedVar.get(0));
        }
        valueNumbering.put(localValueNumber, hasBeenVisited);
    }

    /**
     * Add new lvn pair.
     *
     * @param var the var
     * @param lvd the lvd
     */
    public void addNewLVNPair(ExprVariable var, LocalVarDecl lvd) {
        exprValueNumbering.putIfAbsent(var, lvd);
    }

    /**
     * Update lvn pair.
     *
     * @param var the var
     * @param lvd the lvd
     */
    public void updateLVNPair(ExprVariable var, LocalVarDecl lvd) {
        exprValueNumbering.remove(var);
        exprValueNumbering.put(var, lvd);
    }

    /**
     * Var has been visited boolean.
     *
     * @param e the e
     * @return the boolean
     */
    public boolean varHasBeenVisited(ExprVariable e) {
        return exprValueNumbering.containsKey(e) && exprValueNumbering.get(e) != null; //TODO revert to == ?
    }

    /**
     * Has no predecessors boolean.
     *
     * @return the boolean
     */
    public boolean hasNoPredecessors() {
        return predecessors.isEmpty();
    }

    /**
     * Is buffer block boolean.
     *
     * @return the boolean
     */
    public boolean isBufferBlock() {
        return originalStmt instanceof StmtBlock &&
                ((StmtBlock) originalStmt).getVarDecls().isEmpty() &&
                ((StmtBlock) originalStmt).getTypeDecls().isEmpty() &&
                ((StmtBlock) originalStmt).getStatements().isEmpty();
    }

    /**
     * Is entry boolean.
     *
     * @return the boolean
     */
    public boolean isEntry() {
        return label.equals("Entry");
    }

    /**
     * Is exit boolean.
     *
     * @return the boolean
     */
    public boolean isExit() {
        return label.equals("Exit");
    }

    /**
     * Contain sub stmts boolean.
     *
     * @return the boolean
     */
    public boolean containSubStmts() {
        return shortCutToExit != null;
    }

    /**
     * Gets short cut to exit.
     *
     * @return the short cut to exit
     */
    public StmtLabeledSSA getShortCutToExit() {
        return shortCutToExit;
    }

    /**
     * Gets local value numbers.
     *
     * @return the local value numbers
     */
    public Map<LocalVarDecl, Boolean> getLocalValueNumbers() {
        return new HashMap<>(valueNumbering);
    }

    /**
     * Get var def if exists optional.
     *
     * @param originalName the original name
     * @return the optional
     */
    public Optional<LocalVarDecl> getVarDefIfExists(String originalName) {
        boolean contained = false;
        int i = 0;
        List<LocalVarDecl> lvd = new LinkedList<>(valueNumbering.keySet());
        while (i < lvd.size() && !contained) {
            if (lvd.get(i).getOriginalName().equals(originalName)) {
                contained = true;
            }
            ++i;
        }
        return (contained) ? Optional.of(lvd.get(--i)) : Optional.empty();
    }

    /**
     * Gets original stmt.
     *
     * @return the original stmt
     */
    public Statement getOriginalStmt() {
        return originalStmt;
    }

    /**
     * Sets ssa statement.
     *
     * @param ssaModifiedStmt the ssa modified stmt
     */
    public void setSSAStatement(Statement ssaModifiedStmt) {
        this.ssaModifiedStmt = ssaModifiedStmt;
    }

    /**
     * Has been rebuilt boolean.
     *
     * @return the boolean
     */
    public boolean hasBeenRebuilt() {
        return hasBeenRebuilt;
    }

    /**
     * Sets has been rebuilt.
     */
    public void setHasBeenRebuilt() {
        hasBeenRebuilt = true;
    }

    /**
     * Get ssa modified statement.
     *
     * @return the statement
     */
    public Statement getSsaModified() {
        return ssaModifiedStmt;
    }

    /**
     * Sets relations.
     *
     * @param predecessors the predecessors
     * @param successors   the successors
     */
    public void setRelations(List<StmtLabeledSSA> predecessors, List<StmtLabeledSSA> successors) {
        this.setPredecessors(predecessors);
        this.setSuccessors(successors);
    }

    /**
     * Sets predecessors.
     *
     * @param predecessors the predecessors
     */
    public void setPredecessors(List<StmtLabeledSSA> predecessors) {
        List<StmtLabeledSSA> preds = predecessors.stream().distinct().collect(Collectors.toList());
        this.predecessors = ImmutableList.from(preds);
    }

    /**
     * Sets successors.
     *
     * @param successors the successors
     */
    public void setSuccessors(List<StmtLabeledSSA> successors) {
        List<StmtLabeledSSA> succs = successors.stream().distinct().collect(Collectors.toList());
        this.successors = ImmutableList.from(succs);
    }

    /**
     * Gets predecessors.
     *
     * @return the predecessors
     */
    public ImmutableList<StmtLabeledSSA> getPredecessors() {
        return predecessors;
    }

    /**
     * Gets successors.
     *
     * @return the successors
     */
    public ImmutableList<StmtLabeledSSA> getSuccessors() {
        return successors;
    }

    /**
     * Has no short cut boolean.
     *
     * @return the boolean
     */
    public boolean hasNoShortCut() {
        return shortCutToExit == null;
    }

    /**
     * Sets short cut to exit.
     *
     * @param shortCutToExit the short cut to exit
     */
    public void setShortCutToExit(StmtLabeledSSA shortCutToExit) {
        this.shortCutToExit = shortCutToExit;
    }

    /**
     * Gets exit block.
     *
     * @return the exit block
     */
    public StmtLabeledSSA getExitBlock() {
        return (shortCutToExit != null) ? shortCutToExit : this;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        predecessors.forEach(action);
        successors.forEach(action);
    }

    @Override
    public Statement transformChildren(Transformation transformation) {
        return null;
    }

    public String getUniqueID() {
        return uniqueID;
    }
}
