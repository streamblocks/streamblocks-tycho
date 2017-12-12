package se.lth.cs.tycho.phases.attributes;


import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.stmt.StmtConsume;
import se.lth.cs.tycho.ir.stmt.StmtRead;
import se.lth.cs.tycho.ir.stmt.StmtWrite;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phases.TreeShadow;

import java.util.Optional;

public interface Ports {
    ModuleKey<Ports> key = task -> MultiJ.from(Implementation.class)
            .bind("tree").to(task.getModule(TreeShadow.key))
            .instance();

    PortDecl declaration(Port port);
    boolean isUsedAsInputPort(Port port);
    boolean isUsedAsOutputPort(Port port);

    boolean isInputPort(PortDecl decl);
    boolean isOutputPort(PortDecl decl);

    @Module
    interface Implementation extends Ports {
        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Override
        default PortDecl declaration(Port port) {
            IRNode node = tree().parent(port);
            while (node != null) {
                Optional<ImmutableList<PortDecl>> decls = declarations(node);
                if (decls.isPresent()) {
                    return decls.get().stream()
                            .filter(decl -> decl.getName().equals(port.getName()))
                            .findFirst()
                            .orElse(null);
                }
                node = tree().parent(node);
            }
            return null;
        }

        default Optional<ImmutableList<PortDecl>> declarations(IRNode node) {
            return Optional.empty();
        }

        default Optional<ImmutableList<PortDecl>> declarations(Entity entity) {
            return Optional.of(ImmutableList.concat(entity.getInputPorts(), entity.getOutputPorts()));
        }

        enum PortUse { IN, OUT }

        @Override
        default boolean isUsedAsInputPort(Port port) {
            return portUse(tree().parent(port)) == PortUse.IN;
        }

        default PortUse portUse(IRNode node) {
            throw new AssertionError();
        }

        default PortUse portUse(InputPattern in) {
            return PortUse.IN;
        }

        default PortUse portUse(OutputExpression out) {
            return PortUse.OUT;
        }

        default PortUse portUse(StmtRead read) {
            return PortUse.IN;
        }

        default PortUse portUse(StmtWrite write) {
            return PortUse.OUT;
        }

        default PortUse portUse(StmtConsume consume) {
            return PortUse.IN;
        }

        default PortUse portUse(PortCondition cond) {
            return cond.isInputCondition() ? PortUse.IN : PortUse.OUT;
        }

        @Override
        default boolean isUsedAsOutputPort(Port port) {
            return portUse(tree().parent(port)) == PortUse.OUT;
        }

        @Override
        default boolean isInputPort(PortDecl decl) {
            IRNode parent = tree().parent(decl);
            assert parent instanceof Entity;
            Entity entity = (Entity) parent;
            return entity.getInputPorts().stream().anyMatch(p -> p == decl);
        }

        @Override
        default boolean isOutputPort(PortDecl decl) {
            IRNode parent = tree().parent(decl);
            assert parent instanceof Entity;
            Entity entity = (Entity) parent;
            return entity.getOutputPorts().stream().anyMatch(p -> p == decl);
        }
    }
}
