package se.lth.cs.tycho.transformation.composition;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.stmt.StmtConsume;
import se.lth.cs.tycho.ir.stmt.StmtWrite;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.CompositionPhase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Composer {
    private final List<ActorMachine> actorMachines;
    private final List<Connection> connections;
    private final boolean eagerTest;

    public Composer(List<ActorMachine> actorMachines, List<Connection> connections, Context context) {
        this.actorMachines = actorMachines;
        this.connections = connections;
        this.eagerTest = context.getConfiguration().get(CompositionPhase.eagerTestSetting);
    }

    public ActorMachine compose() {
        Map<String, String> inputPortNames = new HashMap<>();
        Map<String, String> outputPortNames = new HashMap<>();

        ImmutableList.Builder<PortDecl> inputPorts = ImmutableList.builder();
        ImmutableList.Builder<PortDecl> outputPorts = ImmutableList.builder();

        int index = 0;
        for (ActorMachine actorMachine : actorMachines) {
            for (PortDecl port : actorMachine.getInputPorts()) {
                String name = port.getName() + "_" + index;
                index++;
                inputPortNames.put(port.getName(), name);
                inputPorts.add(port.copy(name, port.getType()));
            }
            for (PortDecl port : actorMachine.getOutputPorts()) {
                String name = port.getName() + "_" + index;
                index++;
                outputPortNames.put(port.getName(), name);
                outputPorts.add(port.copy(name, port.getType()));
            }
        }

        IRNode.Transformation rename = MultiJ.from(PortRename.class)
                .bind("inputPorts").to(inputPortNames)
                .bind("outputPorts").to(outputPortNames)
                .instance();

        return new ActorMachine(
                collectAll(ActorMachine::getAnnotations),
                inputPorts.build(),
                outputPorts.build(),
                collectAll(ActorMachine::getTypeParameters),
                collectAll(ActorMachine::getValueParameters),
                rename.mapChecked(Scope.class, collectAll(ActorMachine::getScopes)),
                new CompositionController(actorMachines, connections, eagerTest),
                rename.mapChecked(Transition.class, collectAll(ActorMachine::getTransitions)),
                rename.mapChecked(Condition.class, collectAll(ActorMachine::getConditions)));
    }

    private <E> ImmutableList<E> collectAll(Function<ActorMachine, List<E>> getList) {
        return actorMachines.stream()
                .map(getList)
                .flatMap(List::stream)
                .collect(ImmutableList.collector());
    }


    @Module
    interface PortRename extends IRNode.Transformation {
        @Binding(BindingKind.INJECTED)
        Map inputPorts();

        @Binding(BindingKind.INJECTED)
        Map outputPorts();

        default Port inputPort(Port port) {
            return port.withName((String) inputPorts().get(port.getName()));
        }

        default Port outputPort(Port port) {
            return port.withName((String) outputPorts().get(port.getName()));
        }

        @Override
        default IRNode apply(IRNode node) {
            return rename(node.transformChildren(this));
        }

        default IRNode rename(IRNode node) {
            return node;
        }

        default IRNode rename(PortCondition cond) {
            if (cond.isInputCondition()) {
                return cond.copy(inputPort(cond.getPortName()), cond.N(), cond.isInputCondition());
            } else {
                return cond.copy(outputPort(cond.getPortName()), cond.N(), cond.isInputCondition());
            }
        }

        default IRNode rename(ExprInput input) {
            if (input.hasRepeat()) {
                return input.copy(inputPort(input.getPort()), input.getOffset(), input.getRepeat(), input.getPatternLength());
            } else {
                return input.copy(inputPort(input.getPort()), input.getOffset());
            }
        }

        default IRNode rename(StmtConsume consume) {
            return consume.copy(inputPort(consume.getPort()), consume.getNumberOfTokens());
        }

        default IRNode rename(StmtWrite write) {
            return write.copy(outputPort(write.getPort()), write.getValues(), write.getRepeatExpression());
        }

        default IRNode rename(Transition transition) {
            Map<Port, Integer> inputRates = transition.getInputRates().entrySet().stream().collect(Collectors.toMap(entry -> inputPort(entry.getKey()), Map.Entry::getValue));
            Map<Port, Integer> outputRates = transition.getOutputRates().entrySet().stream().collect(Collectors.toMap(entry -> outputPort(entry.getKey()), Map.Entry::getValue));
            return transition.copy(transition.getAnnotations(), inputRates, outputRates, transition.getScopesToKill(), transition.getBody());
        }
    }
}
