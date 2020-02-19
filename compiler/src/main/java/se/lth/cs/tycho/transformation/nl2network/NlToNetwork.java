package se.lth.cs.tycho.transformation.nl2network;

import se.lth.cs.tycho.attribute.EntityDeclarations;
import se.lth.cs.tycho.interp.BasicEnvironment;
import se.lth.cs.tycho.interp.Environment;
import se.lth.cs.tycho.interp.Interpreter;
import se.lth.cs.tycho.interp.Memory;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.nl.*;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.network.Connection;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NlToNetwork implements EntityExprVisitor<EntityExpr, Environment>, StructureStmtVisitor<StructureStatement, Environment> {

    private NlNetwork srcNetwork;
    private final Interpreter interpreter;

    private HashMap<String, EntityExpr> entities;
    private ArrayList<StructureStatement> structure;
    private boolean evaluated;
    private Memory mem;

    private EntityDeclarations entityDeclarations;

    public NlToNetwork(NlNetwork network, Interpreter interpreter, EntityDeclarations entityDeclarations) {
        this.srcNetwork = network;
        this.interpreter = interpreter;
        this.evaluated = false;
        this.entityDeclarations = entityDeclarations;
    }

    public void evaluate(ImmutableList<Map.Entry<String, Expression>> parameterAssignments) throws CompilationException {
        entities = new HashMap<String, EntityExpr>();
        structure = new ArrayList<StructureStatement>();
        Environment env = new BasicEnvironment(mem);    // no expressions will read from the ports while instantiating/flattening this network
        ImmutableList<LocalVarDecl> declList = srcNetwork.getVarDecls();

    }

    @Override
    public EntityExpr visitEntityInstanceExpr(EntityInstanceExpr e, Environment environment) {
        return null;
    }

    @Override
    public EntityExpr visitEntityIfExpr(EntityIfExpr e, Environment environment) {
        return null;
    }

    @Override
    public EntityExpr visitEntityListExpr(EntityListExpr e, Environment environment) {
        return null;
    }

    @Override
    public StructureStatement visitStructureConnectionStmt(StructureConnectionStmt stmt, Environment environment) {
        return null;
    }

    @Override
    public StructureStatement visitStructureIfStmt(StructureIfStmt stmt, Environment environment) {
        return null;
    }

    @Override
    public StructureStatement visitStructureForeachStmt(StructureForeachStmt stmt, Environment environment) {
        return null;
    }


    private class NetworkInstanceBuilder implements EntityExprVisitor<Void, String>, StructureStmtVisitor<Void, String> {
        HashMap<String, Instance> nodes = new HashMap<String, Instance>();
        ImmutableList.Builder<Connection> connections = new ImmutableList.Builder<Connection>();

        public ImmutableList<Instance> getNodes() {
            return ImmutableList.copyOf(nodes.values());
        }

        public ImmutableList<Connection> getConnections() {
            return connections.build();
        }

        @Override
        public Void visitEntityInstanceExpr(EntityInstanceExpr e, String s) {
            return null;
        }

        @Override
        public Void visitEntityIfExpr(EntityIfExpr e, String s) {
            return null;
        }

        @Override
        public Void visitEntityListExpr(EntityListExpr e, String s) {
            return null;
        }

        @Override
        public Void visitStructureConnectionStmt(StructureConnectionStmt stmt, String s) {
            return null;
        }

        @Override
        public Void visitStructureIfStmt(StructureIfStmt stmt, String s) {
            return null;
        }

        @Override
        public Void visitStructureForeachStmt(StructureForeachStmt stmt, String s) {
            return null;
        }
    }

}
