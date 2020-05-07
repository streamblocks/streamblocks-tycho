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

    private final ComprehensionHelper comprehensionHelper;

    private EntityDeclarations entityDeclarations;

    public NlToNetwork(CompilationTask task, NlNetwork network, Interpreter interpreter) {
        this.srcNetwork = network;
        this.interpreter = interpreter;
        this.evaluated = false;
        this.task = task;
        this.comprehensionHelper = new ComprehensionHelper(interpreter);
        this.entityDeclarations = task.getModule(EntityDeclarations.key);
    }

    public void evaluate(ImmutableList<Map.Entry<String, Expression>> parameterAssignments) throws CompilationException {
        entities = new HashMap<InstanceDecl, EntityExpr>();
        structure = new ArrayList<StructureStatement>();
        mem = new BasicMemory();
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
            entry.getValue().accept(nb, entry.getKey().getInstanceName());
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
        final NlToNetwork exprEvaluator = this;

        for (EntityExpr element : e.getEntityList()) {
            builder.add(element.accept(exprEvaluator, environment));
        }

        return e.copy(builder.build());
    }

    @Override
    public EntityExpr visitEntityComprehensionExpr(EntityComprehensionExpr e, Environment environment) {
        final ImmutableList.Builder<EntityExpr> builder = new ImmutableList.Builder<EntityExpr>();
        final NlToNetwork exprEvaluator = this;

        Runnable exec = new Runnable() {
            public void run() {
                builder.add(e.getCollection().accept(exprEvaluator, environment));
            }
        };

        comprehensionHelper.interpret(e.getGenerator(), e.getFilters(), exec, environment);

        return new EntityListExpr(builder.build());
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
        final ImmutableList.Builder<StructureStatement> builder = new ImmutableList.Builder<StructureStatement>();
        final NlToNetwork stmtEvaluateor = this;

        Runnable exec = new Runnable() {
            public void run() {
                for(StructureStatement element : stmt.getStatements()){
                    builder.add(element.accept(stmtEvaluateor, environment));
                }
            }
        };

        comprehensionHelper.interpret(stmt.getGenerator(), stmt.getFilters(), exec, environment);

        return stmt.copy(null, ImmutableList.<Expression>empty(), builder.build());
    }

    private int findPortIndex(String portName, ImmutableList<PortDecl> portList) {
        for (int i = 0; i < portList.size(); i++) {
            if (portList.get(i).getName().equals(portName)) {
                return i;
            }
        }
        return -1;
    }


    private class NetworkInstanceBuilder implements EntityExprVisitor<Void, String>, StructureStmtVisitor<Void, String> {
        HashMap<String, Instance> instances = new HashMap<>();
        ImmutableList.Builder<Connection> connections = new ImmutableList.Builder<Connection>();

        public ImmutableList<Instance> getInstances() {
            return ImmutableList.copyOf(instances.values());
        }

        public ImmutableList<Connection> getConnections() {
            return connections.build();
        }

        @Override
        public Void visitEntityInstanceExpr(EntityInstanceExpr e, String s) {
            QID entityName = ((EntityReferenceGlobal) e.getEntityName()).getGlobalName();
            Instance instance = new Instance(
                    s,
                    entityName,
                    e.getParameterAssignments().map(ValueParameter::deepClone),
                    ImmutableList.empty())
                    .withAttributes(e.getAttributes().map(ToolAttribute::deepClone));
            instances.put(instance.getInstanceName(), instance);
            return null;
        }

        @Override
        public Void visitEntityIfExpr(EntityIfExpr e, String s) {
            return null;
        }

        @Override
        public Void visitEntityListExpr(EntityListExpr e, String s) {
            ImmutableList<EntityExpr> list = e.getEntityList();
            for (int i = 0; i < list.size(); i++) {
                list.get(i).accept(this, s + "_" + i);
            }
            return null;
        }

        @Override
        public Void visitEntityComprehensionExpr(EntityComprehensionExpr e, String s) {
            return null;
        }


        private String makeEntityName(PortReference port){
            StringBuffer name = new StringBuffer(port.getEntityName());
            for(Expression index : port.getEntityIndex()){
                name.append("_");
                name.append(((ExprLiteral)index).getText());
            }
            return name.toString();
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
            String srcEntityName = null;
            String dstEntityName = null;
            ImmutableList<PortDecl> srcPortList, dstPortList;

            if (src.getEntityName() != null) {
                // -- internal instance
                srcEntityName = makeEntityName(src);
                srcInstance = instances.get(srcEntityName);
                if (srcInstance == null) {
                    throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "cannot find entity: " + srcEntityName));
                }
                GlobalEntityDecl entity = GlobalDeclarations.getEntity(task, srcInstance.getEntityName());
                srcEntity = entity.getEntity();
                srcPortList = srcEntity.getOutputPorts();
            } else {
                srcPortList = srcNetwork.getInputPorts();
            }

            if (dst.getEntityName() != null) {
                dstEntityName = makeEntityName(dst);
                dstInstance = instances.get(dstEntityName);
                if (dstInstance == null) {
                    throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "cannot find entity: " + dstEntityName));
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
            Connection.End srcConn = new Connection.End(Optional.ofNullable(srcEntityName), src.getPortName());
            Connection.End dstConn = new Connection.End(Optional.ofNullable(dstEntityName), dst.getPortName());
            Connection conn = new Connection(srcConn, dstConn);
            connections.add(conn);

            return null;
        }

        @Override
        public Void visitStructureIfStmt(StructureIfStmt stmt, String s) {
            return null;
        }

        @Override
        public Void visitStructureForeachStmt(StructureForeachStmt stmt, String s) {

            for (StructureStatement statement : stmt.getStatements()) {
                statement.accept(this, "");
            }
            return null;
        }
    }

}
