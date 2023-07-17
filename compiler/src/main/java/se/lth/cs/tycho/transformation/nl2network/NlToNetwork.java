package se.lth.cs.tycho.transformation.nl2network;

import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.ConstantEvaluator;
import se.lth.cs.tycho.attribute.EntityDeclarations;
import se.lth.cs.tycho.attribute.TypeScopes;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.GlobalDeclarations;
import se.lth.cs.tycho.ir.Generator;
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
import se.lth.cs.tycho.meta.interp.Environment;
import se.lth.cs.tycho.meta.interp.Interpreter;
import se.lth.cs.tycho.meta.interp.op.Binary;
import se.lth.cs.tycho.meta.interp.op.Unary;
import se.lth.cs.tycho.meta.interp.value.Value;
import se.lth.cs.tycho.meta.interp.value.ValueBool;
import se.lth.cs.tycho.meta.interp.value.ValueList;
import se.lth.cs.tycho.meta.interp.value.util.Convert;
import se.lth.cs.tycho.phase.PortArrayEnumeration;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;

import java.util.*;

public class NlToNetwork implements EntityExprVisitor<EntityExpr, Environment>, StructureStmtVisitor<StructureStatement, Environment> {

    private NlNetwork srcNetwork;
    private final Interpreter interpreter;
    private final Convert convert;

    // Added by Gareth Callanan. This gives us access to the evaluated constant values for the expressions that are
    // part of the PortReference objects as done for similar operations in the PortArrayEnumerationPhase.
    //
    // NOTE: This class already has a way to access constants using the "interpreter.eval(...)" function, however it
    // did not seem simple to do the same thing in the location of the code I was working with without adjusting its
    // structure. So this method was used instead. I am not commited to this implementation, so making it more
    // consistent in the future would be good.
    private final ConstantEvaluator constants;
    private HashMap<InstanceDecl, EntityExpr> entities;
    private ArrayList<StructureStatement> structure;
    private boolean evaluated;
    private final List<ToolAttribute> attributes;

    private CompilationTask task;

    private EntityDeclarations entityDeclarations;

    public NlToNetwork(CompilationTask task, NlNetwork network, List<ToolAttribute> attributes) {
        this.srcNetwork = network;
        this.attributes = attributes;
        this.evaluated = false;
        this.task = task;
        this.entityDeclarations = task.getModule(EntityDeclarations.key);
        this.interpreter = MultiJ.from(Interpreter.class)
                .bind("variables").to(task.getModule(VariableDeclarations.key))
                .bind("types").to(task.getModule(TypeScopes.key))
                .bind("unary").to(MultiJ.from(Unary.class).instance())
                .bind("binary").to(MultiJ.from(Binary.class).instance())
                .instance();
        this.convert = MultiJ.from(Convert.class)
                .instance();
        this.constants = task.getModule(ConstantEvaluator.key);
    }

    public void evaluate(ImmutableList<ValueParameter> parameterAssignments) throws CompilationException {
        entities = new HashMap<InstanceDecl, EntityExpr>();
        structure = new ArrayList<StructureStatement>();
        Environment env = new Environment();    // no expressions will read from the ports while instantiating/flattening this network

        ImmutableList<LocalVarDecl> declList = srcNetwork.getVarDecls();
        for (LocalVarDecl decl : declList) {
            env.put(decl.getName(), interpreter.eval(decl.getValue(), env));
        }

        ImmutableList<ParameterVarDecl> parList = srcNetwork.getValueParameters();
        for (ValueParameter parameter : parameterAssignments) {
            String name = parameter.getName();
            ParameterVarDecl decl = null;
            boolean found = false;
            for (ParameterVarDecl parameterVarDecl : parList) {
                if (name.equals(parameterVarDecl.getName())) {
                    decl = parameterVarDecl;
                    found = true;
                }
            }
            if (!found) {
                throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR,
                        "unknown parameter name: " + name + " found in parameter assignments while evaluating network "));
            }
            env.put(decl.getName(), interpreter.eval(parameter.getValue(), env));
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
        NetworkInstanceBuilder nb = new NetworkInstanceBuilder(attributes);
        for (Map.Entry<InstanceDecl, EntityExpr> entry : entities.entrySet()) {
            entry.getValue().accept(nb, entry.getKey().getInstanceName());
        }

        for (StructureStatement stmt : structure) {
            stmt.accept(nb, "");
        }

        return new Network(srcNetwork.getAnnotations(), inputPorts, outputPorts, nb.getInstances(), nb.getConnections());
    }

    @Override
    public EntityExpr visitEntityInstanceExpr(EntityInstanceExpr e, Environment environment) {
        ImmutableList.Builder<ValueParameter> builder = new ImmutableList.Builder<>();
        for (ValueParameter valueParameter : e.getValueParameters()) {
            Expression expr = valueParameter.getValue();
            Value value = interpreter.eval(expr, environment);
            builder.add(new ValueParameter(valueParameter.getName(), convert.apply(value)));
        }
        return e.copy(e.getEntityName(), ImmutableList.empty(), builder.build());
    }

    @Override
    public EntityExpr visitEntityIfExpr(EntityIfExpr e, Environment environment) {
        Value condition = interpreter.eval(e.getCondition(), environment);
        if (condition instanceof ValueBool) {
            boolean cond = ((ValueBool) condition).bool();
            if (cond) {
                return e.getTrueEntity().accept(this, environment);
            } else {
                return e.getFalseEntity().accept(this, environment);
            }
        }

        throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "Cannot evaluate EntityIfExpr condition"));
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

        Generator generator = e.getGenerator();

        Value value = interpreter.eval(generator.getCollection(), environment);
        if (!(value instanceof ValueList)) {
            throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "Cannot evaluate EntityComprehensionExpr"));
        }

        ValueList list = (ValueList) value;
        for (int i = 0; i < list.elements().size(); i += generator.getVarDecls().size()) {
            Map<String, Value> bindings = new HashMap<>();
            for (int j = 0; j < generator.getVarDecls().size(); ++j) {
                bindings.put(generator.getVarDecls().get(j).getName(), list.elements().get(i));
            }

            Environment newEnv = environment.with(bindings);
            if (e.getFilters().stream().map(filter -> interpreter.eval(filter, newEnv)).allMatch(v -> (v instanceof ValueBool) && ((ValueBool) v).bool())) {
                EntityExpr entityExpr = e.getCollection().accept(exprEvaluator, newEnv);
                builder.add(entityExpr);
            }
        }

        return new EntityListExpr(builder.build());
    }

    @Override
    public StructureStatement visitStructureConnectionStmt(StructureConnectionStmt stmt, Environment environment) {
        PortReference src = stmt.getSrc();
        // -- src
        ImmutableList.Builder<Expression> srcBuilderEntityIndex = new ImmutableList.Builder<>();
        for (Expression expr : src.getEntityIndex()) {
            Value value = interpreter.eval(expr, environment);
            srcBuilderEntityIndex.add(convert.apply(value));
        }
        ImmutableList.Builder<Expression> srcBuilderArrayIndex = new ImmutableList.Builder<>();
        for (Expression expr : src.getArrayIndexExpr()) {
            Value value = interpreter.eval(expr, environment);
            srcBuilderArrayIndex.add(convert.apply(value));
        }
        PortReference newSrc = src.copy(src.getEntityName(), srcBuilderEntityIndex.build(), src.getPortName(), srcBuilderArrayIndex.build());
        // -- dst
        PortReference dst = stmt.getDst();
        ImmutableList.Builder<Expression> dstBuilder = new ImmutableList.Builder<Expression>();
        for (Expression expr : dst.getEntityIndex()) {
            Value value = interpreter.eval(expr, environment);
            dstBuilder.add(convert.apply(value));
        }
        ImmutableList.Builder<Expression> dstBuilderArrayIndex = new ImmutableList.Builder<>();
        for (Expression expr : dst.getArrayIndexExpr()) {
            Value value = interpreter.eval(expr, environment);
            dstBuilderArrayIndex.add(convert.apply(value));
        }
        PortReference newDst = dst.copy(dst.getEntityName(), dstBuilder.build(), dst.getPortName(), dstBuilderArrayIndex.build());
        return stmt.copy(newSrc, newDst);
    }

    @Override
    public StructureStatement visitStructureIfStmt(StructureIfStmt stmt, Environment environment) {
        Value condition = interpreter.eval(stmt.getCondition(), environment);
        if (condition instanceof ValueBool) {
            ImmutableList.Builder<StructureStatement> builder = new ImmutableList.Builder<StructureStatement>();
            boolean cond = ((ValueBool) condition).bool();
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
        throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "Cannot evaluate StructureIfStmt condition"));
    }

    @Override
    public StructureStatement visitStructureForeachStmt(StructureForeachStmt stmt, Environment environment) {
        final ImmutableList.Builder<StructureStatement> builder = new ImmutableList.Builder<StructureStatement>();
        final NlToNetwork stmtEvaluateor = this;

        Generator generator = stmt.getGenerator();

        Value value = interpreter.eval(generator.getCollection(), environment);
        if (!(value instanceof ValueList)) {
            throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "Cannot evaluate EntityComprehensionExpr"));
        }

        ValueList list = (ValueList) value;
        for (int i = 0; i < list.elements().size(); i += generator.getVarDecls().size()) {
            Map<String, Value> bindings = new HashMap<>();
            for (int j = 0; j < generator.getVarDecls().size(); ++j) {
                bindings.put(generator.getVarDecls().get(j).getName(), list.elements().get(i));
            }

            Environment newEnv = environment.with(bindings);
            if (stmt.getFilters().stream().map(filter -> interpreter.eval(filter, newEnv)).allMatch(v -> (v instanceof ValueBool) && ((ValueBool) v).bool())) {
                for (StructureStatement structureStatement : stmt.getStatements()) {
                    builder.add(structureStatement.accept(stmtEvaluateor, newEnv));
                }
            }
        }

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
        private final HashMap<String, Instance> instances;
        private final ImmutableList.Builder<Connection> connections;

        private ImmutableList<ToolAttribute> attributes;

        public NetworkInstanceBuilder(List<ToolAttribute> attributes ){
            instances = new HashMap<>();
            connections =  new ImmutableList.Builder<Connection>();
            this.attributes = ImmutableList.from(attributes);
        }

        public ImmutableList<Instance> getInstances() {
            return ImmutableList.copyOf(instances.values());
        }

        public ImmutableList<Connection> getConnections() {
            return connections.build();
        }

        @Override
        public Void visitEntityInstanceExpr(EntityInstanceExpr e, String s) {
            QID entityName = ((EntityReferenceGlobal) e.getEntityName()).getGlobalName();
            ImmutableList.Builder<ToolAttribute> attrs = ImmutableList.builder();
            attrs.addAll(attributes.map(ToolAttribute::deepClone));
            attrs.addAll(e.getAttributes().map(ToolAttribute::deepClone));
            Instance instance = new Instance(
                    s,
                    entityName,
                    e.getValueParameters().map(ValueParameter::deepClone),
                    ImmutableList.empty())
                    .withAttributes(attrs.build());
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


        private String makeEntityName(PortReference port) {
            StringBuffer name = new StringBuffer(port.getEntityName());
            for (Expression index : port.getEntityIndex()) {
                name.append("_");
                name.append(((ExprLiteral) index).getText());
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
            String srcEnumeratedPortName = getEnumeratedPortName(src);
            srcPortIndex = findPortIndex(srcEnumeratedPortName, srcPortList);
            if (srcPortIndex < 0) {
                throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "no port with name : " + srcEnumeratedPortName + " exists in: " + src.getEntityName()));
            }

            String dstEnumeratedPortName = getEnumeratedPortName(dst);
            dstPortIndex = findPortIndex(dstEnumeratedPortName, dstPortList);
            if (dstPortIndex < 0) {
                throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "no port with name : " + dstEnumeratedPortName + " exists in : " + dst.getEntityName()));
            }
            //System.out.println(srcEnumeratedPortName + " --> " + dstEnumeratedPortName);
            Connection.End srcConn = new Connection.End(Optional.ofNullable(srcEntityName), srcEnumeratedPortName);
            Connection.End dstConn = new Connection.End(Optional.ofNullable(dstEntityName), dstEnumeratedPortName);
            Connection conn = new Connection(srcConn, dstConn).withAttributes(stmt.getAttributes().map(ToolAttribute::deepClone));
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

        /**
         * @author Gareth Callanan
         *
         * Extracts the port name string from a PortReference object.
         *
         * If the given PortReference object has an arrayIndexExpr then the index of the PortReference
         * needs to be inserted into the name. Eg port X[1] is given the port name X__1__. If the object is just
         * of parent class PortReference, then just the standard name is returned.
         *
         * @param port The port where the port name is extracted from.
         * @return Returns a string representing the port name.
         */
        private String getEnumeratedPortName(PortReference port){
            if(port.getArrayIndexExpr().size() != 0){
                Expression expr = port.getArrayIndexExpr().get(0);
                OptionalLong exprValueOpt = constants.intValue(expr);
                if(!exprValueOpt.isPresent()){
                    throw new RuntimeException("For port array reference '" + port.getPortName() + "' within network entity structure '"+port.getEntityName()+"' - this is not expected and should not happen.");
                }
                long exprValue = exprValueOpt.getAsLong();
                if(exprValue < 0){
                    throw new RuntimeException("For port array reference '" + port.getPortName() + "' within network entity structure '"+port.getEntityName()+"' we got a value of " + exprValue + " for the array index - value must be >= 0");
                }
                return PortArrayEnumeration.generatePortNameWithIndex(port.getPortName(), exprValue);
            }else {
                return port.getPortName();
            }
        }
    }

}
