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
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
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
        default List<ValueParameter> parameters(){
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

            parameters().addAll(instance.getValueParameters());


            if(parameters().isEmpty()){
                return actor;
            }

            return actor;
        }

        default  ExprVariable apply(ExprVariable exprVariable) {
            // -- TODO: Apply transformation here
            return exprVariable;
        }


    }
}
