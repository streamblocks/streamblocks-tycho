package se.lth.cs.tycho.ir.stmt.ssa;

import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;
import java.util.Objects;

public class StmtBlockLabeled extends StmtBlock {

    private final String label;

    private ImmutableList<StmtBlockLabeled> predecessors;
    private ImmutableList<StmtBlockLabeled> successors;
    private final List<Statement> statements;
    private final Statement originalStmt;
    private final List<TypeDecl> typeDecls;
    private final List<LocalVarDecl> varDecls;

    public StmtBlockLabeled(String label, Statement originalStmt, List<Statement> statements){
        this(label, originalStmt, ImmutableList.empty(), ImmutableList.empty(), statements, ImmutableList.empty(), ImmutableList.empty());
    }

    private StmtBlockLabeled(String label, Statement originalStmt, List<TypeDecl> typeDecls, List<LocalVarDecl> varDecls, List<Statement> statements, List<StmtBlockLabeled> preds, List<StmtBlockLabeled> succs){
        super(typeDecls, varDecls, statements);
        this.label = label;
        this.originalStmt = originalStmt;
        this.predecessors = ImmutableList.from(preds);
        this.successors = ImmutableList.from(succs);
        this.statements = ImmutableList.from(statements);
        this.varDecls = ImmutableList.from(varDecls);
        this.typeDecls = ImmutableList.from(typeDecls);
    }

    private StmtBlockLabeled copy(Statement originalStmt, List<TypeDecl> typeDecls, List<LocalVarDecl> varDecls, List<Statement> statements,  List<StmtBlockLabeled> preds, List<StmtBlockLabeled> succs) {
        if (Objects.equals(this.originalStmt, originalStmt) && Objects.equals(this.getTypeDecls(), typeDecls)
                && Objects.equals(this.getVarDecls(), varDecls) && Objects.equals(this.statements, statements)
                && Objects.equals(this.predecessors, preds) && Objects.equals(this.successors, succs))
        {
            return this;
        }
        return new StmtBlockLabeled(this.label, originalStmt, typeDecls, varDecls, statements, preds, succs);
    }

    public StmtBlockLabeled copy(String label, Statement originalStmt, List<Statement> statements){
        return new StmtBlockLabeled(label, originalStmt, statements);
    }

    public StmtBlockLabeled withRelations(List<StmtBlockLabeled> preds, List<StmtBlockLabeled> succs){
        return copy(originalStmt, typeDecls, varDecls, statements, preds, succs);
    }

    public void setRelations(List<StmtBlockLabeled> preds, List<StmtBlockLabeled> succs){
        this.predecessors = ImmutableList.from(preds);
        this.successors = ImmutableList.from(succs);

    }

    public void setPredecessors(List<StmtBlockLabeled> predecessors){
        this.predecessors = ImmutableList.from(predecessors);
    }

    public void setSuccessors(List<StmtBlockLabeled> successors) {
        this.successors = ImmutableList.from(successors);
    }


    public ImmutableList<StmtBlockLabeled> getPredecessors() {
        return predecessors;
    }

    public ImmutableList<StmtBlockLabeled> getSuccessors() {
        return successors;
    }

}