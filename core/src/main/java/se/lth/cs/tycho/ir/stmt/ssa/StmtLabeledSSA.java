package se.lth.cs.tycho.ir.stmt.ssa;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class StmtLabeledSSA extends Statement {

    private String label;
    //cfg
    private Statement originalStmt;
    private ImmutableList<StmtLabeledSSA> predecessors;
    private ImmutableList<StmtLabeledSSA> successors;
    private StmtLabeledSSA shortCutToExit;
    private final int nestedLoopLevel;
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

    public StmtLabeledSSA(String label, Statement originalStmt, int nestedLoopLevel) {
        this(null, label, originalStmt, ImmutableList.empty(), ImmutableList.empty(), null, new HashMap<>(), new HashMap<>(), false, nestedLoopLevel, null);
    }

    private StmtLabeledSSA(String label, Statement originalStmt, ImmutableList<StmtLabeledSSA> predecessors, ImmutableList<StmtLabeledSSA> successors, StmtLabeledSSA exit,
                           Map<LocalVarDecl, Boolean> currentPhiExprs, Map<ExprVariable, LocalVarDecl> exprValueNumbering, boolean ssaHasBeenVisted, int nestedLoopLevel, Statement ssaModifiedStmt) {
        this(null, label, originalStmt, predecessors, successors, exit, currentPhiExprs, exprValueNumbering, ssaHasBeenVisted, nestedLoopLevel, ssaModifiedStmt);
    }

    public StmtLabeledSSA withNewOriginal(Statement ssaModifiedStmt) {
        return new StmtLabeledSSA(this.label, this.originalStmt, this.predecessors, this.successors, this.shortCutToExit, this.valueNumbering, this.exprValueNumbering, this.ssaHasBeenVisited, this.nestedLoopLevel, ssaModifiedStmt);
    }

    public boolean havePhiBlocksBeenCreated() {
        return phiBlockCreated;
    }

    public void setPhiBlockToCreated() {
        this.phiBlockCreated = true;
    }

    public void lostCopyName(){
        this.label += "_lostCopyVar";
    }

    public boolean isLostCopyBlock(){
        return label.endsWith("_lostCopyVar");
    }

    public String getLabel() {
        return label;
    }

    public int loopLevel() {
        return nestedLoopLevel;
    }

    public boolean hasBeenVisted() {
        return ssaHasBeenVisited;
    }

    public void setHasBeenVisted() {
        ssaHasBeenVisited = true;
    }

    public Map<ExprVariable, LocalVarDecl> getExprValueNumbering() {
        return new HashMap<>(exprValueNumbering);
    }

    public boolean lvnIsEmpty(){
        return exprValueNumbering.isEmpty() && valueNumbering.isEmpty();
    }

    public void addLocalValueNumber(LocalVarDecl localValueNumber, boolean hasBeenVisited) {
        List<LocalVarDecl> containedVar = valueNumbering.keySet().stream().filter(lv->lv.getName().equals(localValueNumber.getName())).collect(Collectors.toList());
        if(!containedVar.isEmpty()){
            valueNumbering.remove(containedVar.get(0));
        }
        valueNumbering.put(localValueNumber, hasBeenVisited);
    }

    public void addNewLVNPair(ExprVariable var, LocalVarDecl lvd) {
        exprValueNumbering.putIfAbsent(var, lvd);
    }

    public void updateLVNPair(ExprVariable var, LocalVarDecl lvd){
        exprValueNumbering.remove(var);
        exprValueNumbering.put(var, lvd);
    }

    public boolean varHasBeenVisited(ExprVariable e){
        return exprValueNumbering.containsKey(e) && exprValueNumbering.get(e) != null; //TODO revert to == ?
    }

    public boolean hasNoPredecessors(){
        return predecessors.isEmpty();
    }

    public boolean isBufferBlock(){
        return originalStmt == null;
    }

    public boolean containSubStmts(){
        return shortCutToExit != null;
    }

    public StmtLabeledSSA getShortCutToExit() {
        return shortCutToExit;
    }

    public Map<LocalVarDecl, Boolean> getLocalValueNumbers() {
        return new HashMap<>(valueNumbering);
    }

    public Optional<LocalVarDecl> getVarDefIfExists(String originalName){
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

    public Statement getOriginalStmt() {
        return originalStmt;
    }

    public void setSSAStatement(Statement ssaModifiedStmt) {
        this.ssaModifiedStmt = ssaModifiedStmt;
    }

    public boolean hasBeenRebuilt() {
        return hasBeenRebuilt;
    }

    public void setHasBeenRebuilt() {
        hasBeenRebuilt = true;
    }

    public Statement getSsaModified(){
        return ssaModifiedStmt;
    }

    public void setRelations(List<StmtLabeledSSA> predecessors, List<StmtLabeledSSA> successors) {
        this.setPredecessors(predecessors);
        this.setSuccessors(successors);
    }

    public void setPredecessors(List<StmtLabeledSSA> predecessors) {
        List<StmtLabeledSSA> preds = predecessors.stream().distinct().collect(Collectors.toList());
        this.predecessors = ImmutableList.from(preds);
    }

    public void setSuccessors(List<StmtLabeledSSA> successors) {
        List<StmtLabeledSSA> succs = successors.stream().distinct().collect(Collectors.toList());
        this.successors = ImmutableList.from(succs);
    }

    public ImmutableList<StmtLabeledSSA> getPredecessors() {
        return predecessors;
    }

    public ImmutableList<StmtLabeledSSA> getSuccessors() {
        return successors;
    }

    public boolean hasNoShortCut() {
        return shortCutToExit == null;
    }

    public void setShortCutToExit(StmtLabeledSSA shortCutToExit) {
        this.shortCutToExit = shortCutToExit;
    }

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
}
