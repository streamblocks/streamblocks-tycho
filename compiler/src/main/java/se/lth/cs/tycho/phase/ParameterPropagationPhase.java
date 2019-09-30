package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.GlobalDeclarations;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprUnaryOp;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;

import java.util.*;

import static org.multij.BindingKind.LAZY;

public class ParameterPropagationPhase implements Phase {
    @Override
    public String getDescription() {
        return "Propagate parameters.";
    }

    private Map<Instance, CalActor> instanceCalActorMap;

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        instanceCalActorMap = new HashMap<>();
        Network network = task.getNetwork();
        for (Instance instance : network.getInstances()) {
            GlobalEntityDecl entity = GlobalDeclarations.getEntity(task, instance.getEntityName());
            if (entity.getEntity() instanceof CalActor) {
                CalActor actor = (CalActor) entity.getEntity();
                instanceCalActorMap.put(instance, actor);
            }
        }
        Transformation t = MultiJ.from(Transformation.class)
                .bind("instanceCalActorMap").to(instanceCalActorMap)
                .instance();

        return task.transformChildren(t);
    }

    @Override
    public Set<Class<? extends Phase>> dependencies() {
        return Collections.singleton(ElaborateNetworkPhase.class);
    }


    @Module
    interface Transformation extends IRNode.Transformation {

        @Binding(BindingKind.INJECTED)
        Map instanceCalActorMap();

        @Binding(LAZY)
        default List<ValueParameter> parameters() {
            return new ArrayList<>();
        }

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default CalActor apply(CalActor actor) {
            Map<Instance, CalActor> map = instanceCalActorMap();

            Instance instance = map.entrySet().stream()
                    .filter(i -> i.getValue().equals(actor))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);

            if (instance != null) {
                if (instance.getValueParameters() != null)
                    parameters().addAll(instance.getValueParameters());

                if (parameters().isEmpty()) {
                    return actor;
                }
            }

            return actor.transformChildren(this);
        }

        default ValueParameter apply(ValueParameter valueParameter) {
            List<ValueParameter> params = parameters();

            if (!params.isEmpty()) {
                Expression expr = visit(valueParameter.getValue());
                return new ValueParameter(valueParameter.getName(), expr.deepClone());
            }

            return valueParameter;
        }

        default Expression visit(Expression expr) {
            return expr;
        }

        default Expression visit(ExprBinaryOp exprOp) {
            if (!parameters().isEmpty()) {
                Expression op0 = visit(exprOp.getOperands().get(0));
                Expression op1 = visit(exprOp.getOperands().get(1));
                ImmutableList.Builder<Expression> operands = ImmutableList.builder();
                operands.add(op0);
                operands.add(op1);
                return new ExprBinaryOp(exprOp.getOperations(), operands.build());
            }
            return exprOp;
        }

        default Expression visit(ExprVariable exprVariable) {
            List<ValueParameter> params = parameters();
            Expression expr = params.stream()
                    .filter(p -> p.getName().equals(exprVariable.getVariable().getName()))
                    .map(ValueParameter::getValue)
                    .findFirst()
                    .orElse(null);
            if (expr != null) {
                return expr;
            }

            return exprVariable;
        }

        default ExprUnaryOp visit(ExprUnaryOp exprUnaryOp){
            if(!parameters().isEmpty()){
                Expression expr = visit(exprUnaryOp.getOperand());
                return new ExprUnaryOp(exprUnaryOp.getOperation(), expr);
            }

            return exprUnaryOp;
        }

    }
}
