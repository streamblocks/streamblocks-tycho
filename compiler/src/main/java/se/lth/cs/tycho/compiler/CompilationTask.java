package se.lth.cs.tycho.compiler;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.GlobalDecl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;
import se.lth.cs.tycho.attribute.ModuleKey;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class CompilationTask implements IRNode {
    private final ImmutableList<SourceUnit> sourceUnits;
    private final QID identifier;
    private final Network network;
    private final IdentityHashMap<ModuleKey<?>, Object> moduleStore;

    public CompilationTask(List<SourceUnit> sourceUnits, QID identifier, Network network) {
        this.sourceUnits = ImmutableList.from(sourceUnits);
        this.identifier = identifier;
        this.network = network;
        this.moduleStore = new IdentityHashMap<>();
    }

    public <M> M getModule(ModuleKey<M> key) {
        return (M) moduleStore.computeIfAbsent(key, k -> k.createInstance(this));
    }

    public CompilationTask copy(List<SourceUnit> sourceUnits, QID identifier, Network network) {
        if (Lists.sameElements(this.sourceUnits, sourceUnits)
                && Objects.equals(this.identifier, identifier)
                && this.network == network) {
            return this;
        } else {
            return new CompilationTask(sourceUnits, identifier, network);
        }
    }

    public ImmutableList<SourceUnit> getSourceUnits() {
        return sourceUnits;
    }

    public CompilationTask withSourceUnits(List<SourceUnit> sourceUnits) {
        return copy(sourceUnits, identifier, network);
    }

    public QID getIdentifier() {
        return identifier;
    }

    public CompilationTask withIdentifier(QID identifier) {
        return copy(sourceUnits, identifier, network);
    }

    public Network getNetwork() {
        return network;
    }

    public CompilationTask withNetwork(Network network) {
        return copy(sourceUnits, identifier, network);
    }

    public Optional<SourceUnit> getSourceUnit(QID identifier) {
        for (SourceUnit unit : sourceUnits) {
            if (unit.getTree().getQID().isPrefixOf(identifier)) {
                QID last = identifier.getLast();
                for (GlobalEntityDecl decl : unit.getTree().getEntityDecls()) {
                    if (decl.getOriginalName().equals(last.toString())) {
                        return Optional.of(unit);
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        sourceUnits.forEach(action);
        if (network != null) action.accept(network);
    }

    @Override
    public CompilationTask deepClone() {
        return (CompilationTask) IRNode.super.deepClone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompilationTask transformChildren(Transformation transformation) {
        return copy(
                transformation.mapChecked(SourceUnit.class, sourceUnits),
                identifier,
                network == null ? null : transformation.applyChecked(Network.class, network)
        );
    }

    @Override
    public CompilationTask clone() {
        try {
            return (CompilationTask) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
