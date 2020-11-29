package se.lth.cs.tycho.ir.stmt.ssa;


import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class StmtLabeled extends Statement {

    private final String label;
    private Statement originalStmt;
    private ImmutableList<StmtLabeled> predecessors;
    private ImmutableList<StmtLabeled> successors;
    private StmtLabeled exit;
    private final Map<LocalVarDecl, Boolean> valueNumbering;

    //TODO make immutable
    private boolean ssaHasBeenVisted;

    private StmtLabeled(Statement original, String label, Statement originalStmt, ImmutableList<StmtLabeled> predecessors, ImmutableList<StmtLabeled> successors, StmtLabeled exit, Map<LocalVarDecl, Boolean> valueNumbering, boolean ssaHasBeenVisted) {
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
        this(null, label, originalStmt, ImmutableList.empty(), ImmutableList.empty(), null, new HashMap<>(), false);
    }

    private StmtLabeled(String label, Statement originalStmt, ImmutableList<StmtLabeled> predecessors, ImmutableList<StmtLabeled> successors, StmtLabeled exit, Map<LocalVarDecl, Boolean> currentPhiExprs) {
        this(null, label, originalStmt, predecessors, successors, exit, currentPhiExprs, false);
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

    public boolean hasPredecessors(){
        return !predecessors.isEmpty();
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
        while (i < valueNumbering.size() && !contained) {
            if (lvd.get(i).getOriginalName().equals(originalName)) {
                contained = true;
            }
            ++i;
        }
        return (contained) ? lvd.get(i) : null;
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
