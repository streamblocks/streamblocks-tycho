package se.lth.cs.tycho.transformation.nl2network;

import se.lth.cs.tycho.attribute.EntityDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.GlobalDeclarations;
import se.lth.cs.tycho.interp.*;
import se.lth.cs.tycho.interp.values.ExprValue;
import se.lth.cs.tycho.interp.values.RefView;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.ToolAttribute;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.nl.*;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.network.Connection;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NlToNetwork implements EntityExprVisitor<EntityExpr, Environment>, StructureStmtVisitor<StructureStatement, Environment> {

    private NlNetwork srcNetwork;
    private final Interpreter interpreter;

    private HashMap<InstanceDecl, EntityExpr> entities;
    private ArrayList<StructureStatement> structure;
    private boolean evaluated;
    TypeConverter converter = TypeConverter.getInstance();

    private CompilationTask task;

    private Memory mem;

    private EntityDeclarations entityDeclarations;

    public NlToNetwork(CompilationTask task, NlNetwork network, Interpreter interpreter) {
        this.srcNetwork = network;
        this.interpreter = interpreter;
        this.evaluated = false;
        this.task = task;
        this.entityDeclarations = task.getModule(EntityDeclarations.key);
    }

    public void evaluate(ImmutableList<Map.Entry<String, Expression>> parameterAssignments) throws CompilationException {
        entities = new HashMap<InstanceDecl, EntityExpr>();
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
            entities.put(decl, unrolled);
        }

        for (StructureStatement stmt : srcNetwork.getStructure()) {
            structure.add(stmt.accept(this, env));
        }
        evaluated = true;

    }

    public Network getNetwork() {
        assert evaluated;
        ImmutableList<PortDecl> inputPorts = srcNetwork.getInputPorts().map(PortDecl::deepClone);
        ImmutableList<PortDecl> outputPorts = srcNetwork.getOutputPorts().map(PortDecl::deepClone);
        NetworkInstanceBuilder nb = new NetworkInstanceBuilder();
        for (Map.Entry<InstanceDecl, EntityExpr> entry : entities.entrySet()) {
            entry.getValue().accept(nb, entry.getKey());
        }

        for (StructureStatement stmt : structure) {
            stmt.accept(nb, "");
        }

        return new Network(inputPorts, outputPorts, nb.getInstances(), nb.getConnections());
    }

    @Override
    public EntityExpr visitEntityInstanceExpr(EntityInstanceExpr e, Environment environment) {
        ImmutableList.Builder<ValueParameter> builder = new ImmutableList.Builder<>();
        for (ValueParameter valueParameter : e.getParameterAssignments()) {
            Expression expr = valueParameter.getValue();
            RefView value = interpreter.evaluate(expr, environment);
            builder.add(new ValueParameter(valueParameter.getName(), new ExprValue(new ExprLiteral(ExprLiteral.Kind.Integer, value.toString()))));
        }
        return e.copy(e.getEntityName(), builder.build());
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
        ImmutableList.Builder<Expression> srcBuilder = new ImmutableList.Builder<>();
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

    private int findPortIndex(String portName, ImmutableList<PortDecl> portList) {
        for (int i = 0; i < portList.size(); i++) {
            if (portList.get(i).getName().equals(portName)) {
                return i;
            }
        }
        return -1;
    }


    private class NetworkInstanceBuilder implements EntityExprVisitor<Void, InstanceDecl>, StructureStmtVisitor<Void, String> {
        HashMap<String, Instance> instances = new HashMap<>();
        ImmutableList.Builder<Connection> connections = new ImmutableList.Builder<Connection>();

        public ImmutableList<Instance> getInstances() {
            return ImmutableList.copyOf(instances.values());
        }

        public ImmutableList<Connection> getConnections() {
            return connections.build();
        }

        @Override
        public Void visitEntityInstanceExpr(EntityInstanceExpr e, InstanceDecl decl) {
            assert decl.getEntityExpr() instanceof EntityInstanceExpr;
            EntityInstanceExpr expr = (EntityInstanceExpr) decl.getEntityExpr();
            assert expr.getEntityName() instanceof EntityReferenceGlobal;

            EntityDeclarations declarations = task.getModule(EntityDeclarations.key);
            GlobalEntityDecl entityDecl = declarations.declaration(e.getEntityName());

            if(entityDecl.getEntity() instanceof NlNetwork){

            }

            QID entityName = ((EntityReferenceGlobal) expr.getEntityName()).getGlobalName();
            Instance instance = new Instance(
                    decl.getInstanceName(),
                    entityName,
                    expr.getParameterAssignments().map(ValueParameter::deepClone),
                    ImmutableList.empty())
                    .withAttributes(expr.getAttributes().map(ToolAttribute::deepClone));
            instances.put(instance.getInstanceName(), instance);
            return null;
        }

        @Override
        public Void visitEntityIfExpr(EntityIfExpr e, InstanceDecl decl) {
            return null;
        }

        @Override
        public Void visitEntityListExpr(EntityListExpr e, InstanceDecl decl) {
            return null;
        }

        @Override
        public Void visitStructureConnectionStmt(StructureConnectionStmt stmt, String s) {
            PortReference src = stmt.getSrc();
            PortReference dst = stmt.getDst();
            Instance srcInstance = null;
            Instance dstInstance = null;
            Entity srcEntity;
            Entity dstEntity;
            int srcPortIndex = -1;
            int dstPortIndex = -1;
            ImmutableList<PortDecl> srcPortList, dstPortList;

            if (src.getEntityName() != null) {
                // -- internal instance
                String entityName = src.getEntityName();
                srcInstance = instances.get(entityName);
                if (srcInstance == null) {
                    throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "cannot find entity: " + entityName));
                }
                GlobalEntityDecl entity = GlobalDeclarations.getEntity(task, srcInstance.getEntityName());
                srcEntity = entity.getEntity();
                srcPortList = srcEntity.getOutputPorts();
            } else {
                srcPortList = srcNetwork.getInputPorts();
            }

            if (dst.getEntityName() != null) {
                String entityName = dst.getEntityName();
                dstInstance = instances.get(entityName);
                if (dstInstance == null) {
                    throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "cannot find entity: " + entityName));
                }
                GlobalEntityDecl entity = GlobalDeclarations.getEntity(task, dstInstance.getEntityName());
                dstEntity = entity.getEntity();
                dstPortList = dstEntity.getInputPorts();
            } else {
                dstPortList = srcNetwork.getOutputPorts();
            }
            // -- verify that the ports exists
            srcPortIndex = findPortIndex(src.getPortName(), srcPortList);
            if (srcPortIndex < 0) {
                throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "no port with name : " + src.getPortName() + " exists in: " + src.getEntityName()));
            }
            dstPortIndex = findPortIndex(dst.getPortName(), dstPortList);
            if (dstPortIndex < 0) {
                throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "no port with name : " + dst.getPortName() + " exists in : " + dst.getEntityName()));
            }
            Connection.End srcConn = new Connection.End(Optional.ofNullable(src.getEntityName()), src.getPortName());
            Connection.End dstConn = new Connection.End(Optional.ofNullable(src.getEntityName()), dst.getPortName());
            Connection conn = new Connection(srcConn,dstConn);
            connections.add(conn);

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
