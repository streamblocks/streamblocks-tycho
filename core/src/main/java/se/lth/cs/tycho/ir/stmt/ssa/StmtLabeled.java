package se.lth.cs.tycho.ir.stmt.ssa;


import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class StmtLabeled extends Statement {

    private String label;
    private Statement originalStmt;
    private ImmutableList<StmtLabeled> predecessors;
    private ImmutableList<StmtLabeled> successors;
    private StmtLabeled exit;
    private final Map<LocalVarDecl, Boolean> valueNumbering;
    private final Map<ExprVariable, LocalVarDecl> exprValueNumbering;
    private boolean ssaHasBeenVisted;
    private final int nestedLoopLevel;

    private StmtLabeled(Statement original, String label, Statement originalStmt, ImmutableList<StmtLabeled> predecessors, ImmutableList<StmtLabeled> successors, StmtLabeled exit,
                        Map<LocalVarDecl, Boolean> valueNumbering, Map<ExprVariable, LocalVarDecl> exprValueNumbering, boolean ssaHasBeenVisted, int nestedLoopLevel) {
        super(original);
        this.label = label;
        this.predecessors = predecessors;
        this.successors = successors;
        this.originalStmt = originalStmt;
        this.exit = exit;
        this.valueNumbering = valueNumbering;
        this.exprValueNumbering = exprValueNumbering;
        this.ssaHasBeenVisted = ssaHasBeenVisted;
        this.nestedLoopLevel = nestedLoopLevel;
    }

    public StmtLabeled(String label, Statement originalStmt, int nestedLoopLevel) {
        this(null, label, originalStmt, ImmutableList.empty(), ImmutableList.empty(), null, new HashMap<>(), new HashMap<>(), false, nestedLoopLevel);
    }

    private StmtLabeled(String label, Statement originalStmt, ImmutableList<StmtLabeled> predecessors, ImmutableList<StmtLabeled> successors, StmtLabeled exit,
                        Map<LocalVarDecl, Boolean> currentPhiExprs, Map<ExprVariable, LocalVarDecl> exprValueNumbering, boolean ssaHasBeenVisted, int nestedLoopLevel) {
        this(null, label, originalStmt, predecessors, successors, exit, currentPhiExprs, exprValueNumbering, ssaHasBeenVisted, nestedLoopLevel);
    }

    public StmtLabeled withNewOriginal(Statement originalStmt) {
        return new StmtLabeled(this.label, originalStmt, this.predecessors, this.successors, this.exit, this.valueNumbering, this.exprValueNumbering, this.ssaHasBeenVisted, this.nestedLoopLevel);
    }

    public void lostCopyName(){
        this.label += "_lostCopyVar";
    }

    public boolean isLostCopyBlock(){
        return label.endsWith("_lostCopyVar");
    }

    public void resetLVN(){
        this.valueNumbering.clear();
    }

    public String getLabel() {
        return label;
    }

    public int loopLevel() {
        return nestedLoopLevel;
    }

    public boolean hasBeenVisted() {
        return ssaHasBeenVisted;
    }

    public void setHasBeenVisted() {
        ssaHasBeenVisted = true;
    }

    public Map<ExprVariable, LocalVarDecl> getExprValueNumbering() {
        return exprValueNumbering;
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
        return exprValueNumbering.containsKey(e) && exprValueNumbering.get(e) == null;
    }

    public boolean hasNoPredecessors(){
        return predecessors.isEmpty();
    }

    public boolean isBufferBlock(){
        return originalStmt == null;
    }

    public void setAllLVNToVisited(){
        valueNumbering.forEach((k, v)-> valueNumbering.replace(k, true));
    }

    public void addLocalValueNumber(Map<LocalVarDecl, Boolean> lvnList){
        lvnList.forEach(this::addLocalValueNumber);
    }

    public Map<LocalVarDecl, Boolean> getLocalValueNumbers() {

        return new HashMap<>(valueNumbering);
    }

    public LocalVarDecl containsVarDef(String originalName){
        boolean contained = false;
        int i = 0;
        List<LocalVarDecl> lvd = new LinkedList<>(valueNumbering.keySet());
        while (i < lvd.size() && !contained) {
            if (lvd.get(i).getOriginalName().equals(originalName)) {
                contained = true;
            }
            ++i;
        }
        return (contained) ? lvd.get(--i) : null;
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
