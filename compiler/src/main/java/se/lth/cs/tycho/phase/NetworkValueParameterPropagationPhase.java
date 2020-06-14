package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.ConstantEvaluator;
import se.lth.cs.tycho.attribute.EntityDeclarations;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.GlobalDeclarations;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceLocal;
import se.lth.cs.tycho.ir.entity.nl.InstanceDecl;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Reporter;

import java.util.*;

public class NetworkValueParameterPropagationPhase implements Phase {

    private Map<ValueParameter, List<ParameterVarDecl>> map;

    @Override
    public String getDescription() {
        return "Constant Folding phase.";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        GlobalEntityDecl entityDecl = GlobalDeclarations.getEntity(task, task.getIdentifier());
        if (entityDecl.getEntity() instanceof NlNetwork) {
            NlNetwork nlNetwork = (NlNetwork) entityDecl.getEntity();
            NetworkVisitor visitor = MultiJ.from(NetworkVisitor.class)
                    .bind("constantEvaluator").to(task.getModule(ConstantEvaluator.key))
                    .bind("parent").to(nlNetwork)
                    .bind("entities").to(task.getModule(EntityDeclarations.key))
                    .bind("reporter").to(context.getReporter())
                    .instance();
            Map<ValueParameter, List<ParameterVarDecl>> map = visitor.visit(nlNetwork);

            Transformation t = MultiJ.from(Transformation.class)
                    .bind("map").to(map)
                    .bind("varDecls").to(task.getModule(VariableDeclarations.key))
                    .instance();

            return task.transformChildren(t);
        }


        return task;
    }


    @Module
    interface NetworkVisitor {

        @Binding(BindingKind.INJECTED)
        ConstantEvaluator constantEvaluator();

        @Binding(BindingKind.INJECTED)
        NlNetwork parent();

        @Binding(BindingKind.INJECTED)
        EntityDeclarations entities();

        @Binding(BindingKind.INJECTED)
        Reporter reporter();

        @Binding(BindingKind.LAZY)
        default Map<ValueParameter, List<ParameterVarDecl>> valueParamMap() {
            return new HashMap<>();
        }

        Map<ValueParameter, List<ParameterVarDecl>> visit(IRNode node);

        default Map<ValueParameter, List<ParameterVarDecl>> visit(NlNetwork nlNetwork) {
            for (InstanceDecl entity : nlNetwork.getEntities()) {
                if (entity.getEntityExpr() instanceof EntityInstanceExpr) {
                    EntityInstanceExpr instanceExpr = (EntityInstanceExpr) entity.getEntityExpr();
                    EntityReferenceLocal ref = ((EntityReferenceLocal) instanceExpr.getEntityName());
                    GlobalEntityDecl globalEntityDecl = entities().declaration(ref);
                    for (ParameterVarDecl parameterVarDecl : globalEntityDecl.getEntity().getValueParameters()) {
                        ValueParameter valueParameter = ((EntityInstanceExpr) entity.getEntityExpr()).getValueParameters()
                                .stream().filter(vp -> vp.getName().equals(parameterVarDecl.getName()))
                                .findAny()
                                .orElse(null);

                        OptionalLong value = constantEvaluator().intValue(valueParameter.getValue());

                        if (value.isPresent()) {
                            if (valueParamMap().containsKey(valueParameter)) {
                                valueParamMap().get(valueParameter).add(parameterVarDecl);
                            } else {
                                List<ParameterVarDecl> l = new ArrayList<>();
                                l.add(parameterVarDecl);
                                valueParamMap().put(valueParameter, l);
                            }
                        } else {
                            if (nlNetwork == parent()) {
                                //reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Network parameter : " + valueParameter.getName() + " has not been initialized."));
                            } else {
                                Optional<ValueParameter> vp = visitExpr(valueParameter.getValue());
                                if (vp.isPresent()) {
                                    valueParamMap().get(vp.get()).add(parameterVarDecl);
                                }
                            }
                        }
                    }

                    // -- Visit Sub Networks
                    if (globalEntityDecl.getEntity() instanceof NlNetwork) {
                        visit(globalEntityDecl.getEntity());
                    }
                }
            }
            return valueParamMap();
        }

        Optional<ValueParameter> visitExpr(Expression expression);

        default Optional<ValueParameter> visitExpr(ExprVariable exprVariable) {
            Optional<ValueParameter> optional = valueParamMap().keySet().stream().filter(vp -> vp.getName().equals(exprVariable.getVariable().getName())).findAny();
            return optional;
        }

    }


    @Module
    interface Transformation extends IRNode.Transformation {

        @Binding(BindingKind.INJECTED)
        Map map();

        @Binding(BindingKind.INJECTED)
        VariableDeclarations varDecls();

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default Expression apply(ExprVariable exprVariable) {
            Expression expr = visit(exprVariable);
            if (expr != exprVariable) {
                return expr.deepClone();
            }
            return exprVariable;
        }

        default Expression visit(Expression expression) {
            return expression;
        }

        default Expression visit(ExprBinaryOp exprOp) {
            Expression op0 = visit(exprOp.getOperands().get(0));
            Expression op1 = visit(exprOp.getOperands().get(1));
            ImmutableList.Builder<Expression> operands = ImmutableList.builder();
            operands.add(op0);
            operands.add(op1);
            return new ExprBinaryOp(exprOp.getOperations(), operands.build());
        }

        default Expression visit(ExprVariable exprVariable) {
            Map<ValueParameter, List<ParameterVarDecl>> m = map();

            VarDecl varDecl = varDecls().declaration(exprVariable);
            if (varDecl instanceof ParameterVarDecl) {
                Optional<ValueParameter> valueParameter = find(m, (ParameterVarDecl) varDecl);

                if (valueParameter.isPresent()) {
                    return valueParameter.get().getValue().deepClone();
                }
            }

            return exprVariable;
        }

        default Optional<ValueParameter> find(Map<ValueParameter, List<ParameterVarDecl>> map, ParameterVarDecl parameterVarDecl) {
            Optional<ValueParameter> vp = Optional.empty();

            for (ValueParameter valueParameter : map.keySet()) {
                List<ParameterVarDecl> list = map.get(valueParameter);
                if (list.stream().filter(p -> p == parameterVarDecl).findAny().isPresent()) {
                    vp = Optional.of(valueParameter);
                }
            }

            return vp;
        }

    }


}
