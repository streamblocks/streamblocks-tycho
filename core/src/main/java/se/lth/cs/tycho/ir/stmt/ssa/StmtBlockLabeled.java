package se.lth.cs.tycho.ir.stmt.ssa;

import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;

public class StmtBlockLabeled extends StmtBlock {

    private final String label;
    private final List<StmtBlockLabeled> predecessors;
    private final List<StmtBlockLabeled> successors;

    public StmtBlockLabeled(String label, List<TypeDecl> typeDecls, List<LocalVarDecl> varDecls, List<Statement> statements) {
        super(typeDecls, varDecls, statements);
        this.label = label;
        this.predecessors = ImmutableList.empty();
        this.successors = ImmutableList.empty();
    }

}