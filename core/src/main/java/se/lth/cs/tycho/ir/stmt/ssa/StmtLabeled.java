package se.lth.cs.tycho.ir.stmt.ssa;


import javafx.util.Pair;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static se.lth.cs.tycho.ir.Variable.variable;


public class StmtLabeled extends Statement {

    private final String label;
    private Statement originalStmt;
    private ImmutableList<StmtLabeled> predecessors;
    private ImmutableList<StmtLabeled> successors;
    private StmtLabeled exit;
    private final Map<LocalVarDecl, Boolean> valueNumbering;
    private Map<ExprVariable, LocalVarDecl> valueNumberingResult;
    private final Map<ExprVariable, LocalVarDecl> valueNumberingTest;
    private boolean ssaHasBeenVisted;

    private StmtLabeled(Statement original, String label, Statement originalStmt, ImmutableList<StmtLabeled> predecessors, ImmutableList<StmtLabeled> successors, StmtLabeled exit,
                        Map<LocalVarDecl, Boolean> valueNumbering, Map<ExprVariable, LocalVarDecl> valueNumberingResult, Map<ExprVariable, LocalVarDecl> valueNumberingTest, boolean ssaHasBeenVisted) {
        super(original);
        this.label = label;
        this.predecessors = predecessors;
        this.successors = successors;
        this.originalStmt = originalStmt;
        this.exit = exit;
        this.valueNumbering = valueNumbering;
        this.valueNumberingResult = valueNumberingResult;
        this.ssaHasBeenVisted = ssaHasBeenVisted;
        this.valueNumberingTest = valueNumberingTest;
    }

    public StmtLabeled(String label, Statement originalStmt) {
        this(null, label, originalStmt, ImmutableList.empty(), ImmutableList.empty(), null, new HashMap<>(), new HashMap<>(), new HashMap<>(), false);
    }

    private StmtLabeled(String label, Statement originalStmt, ImmutableList<StmtLabeled> predecessors, ImmutableList<StmtLabeled> successors, StmtLabeled exit, Map<LocalVarDecl, Boolean> currentPhiExprs) {
        this(null, label, originalStmt, predecessors, successors, exit, currentPhiExprs, new HashMap<>(), new HashMap<>(), false);
    }

    public StmtLabeled withNewOriginal(Statement originalStmt) {
        return new StmtLabeled(this.label, originalStmt, this.predecessors, this.successors, this.exit, this.valueNumbering);
    }

    public String getLabel() {
        return label;
    }

    public boolean hasBeenVisted() {
        return ssaHasBeenVisted;
    }

    public void setHasBeenVisted() {
        ssaHasBeenVisted = true;
    }

    public void addLocalValueNumber(LocalVarDecl localValueNumber, boolean hasBeenVisited) {
        List<LocalVarDecl> containedVar = valueNumbering.keySet().stream().filter(lv->lv.getName().equals(localValueNumber.getName())).collect(Collectors.toList());
        if(!containedVar.isEmpty()){
            valueNumbering.remove(containedVar.get(0));
        }
        valueNumbering.put(localValueNumber, hasBeenVisited);/*
        valueNumbering.removeIf(v -> v.getName().equals(localValueNumber.getName()));
        valueNumbering.add(localValueNumber);*/
    }

    public void addLVNResult(ExprVariable var, LocalVarDecl ssaResult){
        valueNumberingResult.putIfAbsent(var, ssaResult);
    }

    public void setFinalLVNResults(){
        List<LocalVarDecl> lvd = new LinkedList<>(valueNumbering.keySet());
        lvd.forEach(l -> valueNumberingResult.putIfAbsent(new ExprVariable(variable(l.getOriginalName())), l));
        valueNumbering.clear();
    }

    public void addNewLVNPair(ExprVariable var, LocalVarDecl lvd) {
        valueNumberingTest.putIfAbsent(var, lvd);
    }

    public void updateLVNPair(ExprVariable var, LocalVarDecl lvd){
        valueNumberingTest.remove(var);
        valueNumberingTest.put(var, lvd);
    }

    public boolean varHasBeenVistied(ExprVariable e){
        return valueNumberingTest.containsKey(e) && valueNumberingTest.get(e) == null;
    }

    public boolean hasNoPredecessors(){
        return predecessors.isEmpty();
    }

    public boolean isBufferBlock(){
        return originalStmt == null;
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
