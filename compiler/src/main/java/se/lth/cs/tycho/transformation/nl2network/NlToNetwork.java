package se.lth.cs.tycho.transformation.nl2network;

import se.lth.cs.tycho.attribute.EntityDeclarations;
import se.lth.cs.tycho.interp.*;
import se.lth.cs.tycho.interp.values.ExprValue;
import se.lth.cs.tycho.interp.values.RefView;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.entity.nl.*;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.network.Connection;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NlToNetwork implements EntityExprVisitor<EntityExpr, Environment>, StructureStmtVisitor<StructureStatement, Environment> {

    private NlNetwork srcNetwork;
    private final Interpreter interpreter;

    private HashMap<String, EntityExpr> entities;
    private ArrayList<StructureStatement> structure;
    private boolean evaluated;
    TypeConverter converter = TypeConverter.getInstance();

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
        for (LocalVarDecl decl : declList) {
            mem.declareGlobal(decl);
            RefView value = interpreter.evaluate(decl.getValue(), env);
            value.assignTo(mem.getGlobal(decl));
        }

        ImmutableList<ParameterVarDecl> parList = srcNetwork.getValueParameters();
        for (Map.Entry<String, Expression> assignment : parameterAssignments) {
            String name = assignment.getKey();
            ParameterVarDecl decl = null;
            boolean found = false;
            for (int i = 0; i < parList.size(); i++) {
                if (name.equals(parList.get(i).getName())) {
                    decl = parList.get(i);
                    found = true;
                }
            }
            if (!found) {
                throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR,
                        "unknown parameter name: " + name + " found in parameter assignments while evaluating network "));
            }
            RefView value = interpreter.evaluate(assignment.getValue(), env);
            mem.declareLocal(decl);
            value.assignTo(mem.getLocal(decl));
        }

        for (InstanceDecl decl : srcNetwork.getEntities()) {
            EntityExpr unrolled = decl.getEntityExpr().accept(this, env);
            entities.put(decl.getInstanceName(), unrolled);
        }

        for (StructureStatement stmt : srcNetwork.getStructure()) {
            structure.add(stmt.accept(this, env));
        }
        evaluated = true;

    }

    @Override
    public EntityExpr visitEntityInstanceExpr(EntityInstanceExpr e, Environment environment) {
        return null;
    }

    @Override
    public EntityExpr visitEntityIfExpr(EntityIfExpr e, Environment environment) {
        RefView condRef = interpreter.evaluate(e.getCondition(), environment);
        boolean cond = converter.getBoolean(condRef);
        if (cond) {
            return e.getTrueEntity().accept(this, environment);
        } else {
            return e.getFalseEntity().accept(this, environment);
        }
    }

    @Override
    public EntityExpr visitEntityListExpr(EntityListExpr e, Environment environment) {
        final ImmutableList.Builder<EntityExpr> builder = new ImmutableList.Builder<EntityExpr>();
        final NlToNetwork exprEvaluateor = this;
        Runnable execStmt = new Runnable() {
            public void run() {
                for (EntityExpr element : e.getEntityList()) {
                    builder.add(element.accept(exprEvaluateor, environment));
                }
            }
        };
       
        return null;
    }

    @Override
    public StructureStatement visitStructureConnectionStmt(StructureConnectionStmt stmt, Environment environment) {
        PortReference src = stmt.getSrc();
        // -- src
        ImmutableList.Builder<Expression> srcBuilder = new ImmutableList.Builder<Expression>();
        for (Expression expr : src.getEntityIndex()) {
            RefView value = interpreter.evaluate(expr, environment);
            srcBuilder.add(new ExprValue(expr, ExprLiteral.Kind.Integer, value.toString(), value));
        }
        PortReference newSrc = src.copy(src.getEntityName(), srcBuilder.build(), src.getPortName());
        // -- dst
        PortReference dst = stmt.getDst();
        ImmutableList.Builder<Expression> dstBuilder = new ImmutableList.Builder<Expression>();
        for (Expression expr : dst.getEntityIndex()) {
            RefView value = interpreter.evaluate(expr, environment);
            dstBuilder.add(new ExprValue(expr, ExprLiteral.Kind.Integer, value.toString(), value));
        }
        PortReference newDst = dst.copy(dst.getEntityName(), dstBuilder.build(), dst.getPortName());
        return stmt.copy(newSrc, newDst);
    }

    @Override
    public StructureStatement visitStructureIfStmt(StructureIfStmt stmt, Environment environment) {
        ImmutableList.Builder<StructureStatement> builder = new ImmutableList.Builder<StructureStatement>();
        RefView condRef = interpreter.evaluate(stmt.getCondition(), environment);
        boolean cond = converter.getBoolean(condRef);
        if (cond) {
            for (StructureStatement s : stmt.getTrueStmt()) {
                builder.add(s.accept(this, environment));
            }
        } else {
            for (StructureStatement s : stmt.getFalseStmt()) {
                builder.add(s.accept(this, environment));
            }
        }
        return new StructureForeachStmt(null, ImmutableList.<Expression>empty(), builder.build());
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
