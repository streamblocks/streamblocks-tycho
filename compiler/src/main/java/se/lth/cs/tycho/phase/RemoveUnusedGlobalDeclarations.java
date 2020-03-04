package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.TypeNamespaces;
import se.lth.cs.tycho.attribute.TypeScopes;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.*;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceGlobal;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;
import se.lth.cs.tycho.ir.expr.ExprTypeConstruction;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstructor;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RemoveUnusedGlobalDeclarations implements Phase {
    @Override
    public String getDescription() {
        return "Removes global declarations that are never used.";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        Collector collector = MultiJ.from(Collector.class)
                .bind("tree").to(task.getModule(TreeShadow.key))
                .bind("task").to(task)
                .bind("namespace").to(task.getModule(TypeNamespaces.key))
                .bind("typeScopes").to(task.getModule(TypeScopes.key))
                .instance();
        collector.collect();
        return task.withSourceUnits(task.getSourceUnits().map(unit -> transformUnit(unit, collector.usedDecls())));
    }

    private SourceUnit transformUnit(SourceUnit unit, Set<Decl> included) {
        return unit.withTree(transformNsDecl(unit.getTree(), included));
    }

    private NamespaceDecl transformNsDecl(NamespaceDecl decl, Set<Decl> included) {
        return decl.withEntityDecls(filter(decl.getEntityDecls(), included::contains))
                .withVarDecls(filter(decl.getVarDecls(), included::contains))
                .withTypeDecls(filter(decl.getTypeDecls(), included::contains));
    }

    private <T> ImmutableList<T> filter(ImmutableList<T> list, Predicate<T> predicate) {
        return list.stream().filter(predicate).collect(ImmutableList.collector());
    }


    public static enum NameKind {
        VAR, ENTITY, TYPE, VAR_NS, ENTITY_NS, TYPE_NS;

        public boolean isNamespace() {
            switch (this) {
                case VAR_NS:
                case ENTITY_NS:
                case TYPE_NS:
                    return true;
                default:
                    return false;
            }
        }
    }

    public static class Name {
        private final QID name;
        private final NameKind kind;

        public Name(QID name, NameKind kind) {
            this.name = name;
            this.kind = kind;
        }

        public QID getName() {
            return name;
        }

        public NameKind getKind() {
            return kind;
        }

        public QID getNamespacePart() {
            return kind.isNamespace() ? name : name.getButLast();
        }

        public boolean isEntity() {
            return kind == NameKind.ENTITY || kind == NameKind.ENTITY_NS;
        }

        public boolean isVariable() {
            return kind == NameKind.VAR || kind == NameKind.VAR_NS;
        }

        public boolean isType() {
            return kind == NameKind.TYPE || kind == NameKind.TYPE_NS;
        }

        public boolean isInNamespace(QID namespace) {
            return getNamespacePart().equals(namespace);
        }

        public boolean includesLocalName(String name) {
            return kind.isNamespace() || this.name.getLast().toString().equals(name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, kind);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Name) {
                Name that = (Name) obj;
                return this.kind == that.kind && Objects.equals(this.name, that.name);
            } else {
                return false;
            }
        }
    }


    @Module
    interface Collector {
        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Binding(BindingKind.INJECTED)
        CompilationTask task();

        @Binding(BindingKind.INJECTED)
        TypeNamespaces namespace();

        @Binding(BindingKind.INJECTED)
        TypeScopes typeScopes();

        @Binding(BindingKind.LAZY)
        default Set<Name> visitedNames() {
            return new HashSet<>();
        }

        @Binding(BindingKind.LAZY)
        default Queue<Name> nameQueue() {
            return new ArrayDeque<>();
        }

        @Binding(BindingKind.LAZY)
        default Set<Decl> usedDecls() {
            return new HashSet<>();
        }

        default void addName(QID name, NameKind kind) {
            Name n = new Name(name, kind);
            if (visitedNames().add(n)) {
                nameQueue().add(n);
            }
        }

        default void collect() {
            addName(task().getIdentifier(), NameKind.ENTITY);
            task().getNetwork().getInstances().forEach(instance -> addName(instance.getEntityName(), NameKind.ENTITY));
            while (!nameQueue().isEmpty()) {
                Name name = nameQueue().remove();
                getDeclarations(name).forEach(decl -> {
                    if (usedDecls().add(decl)) {
                        scan(decl);
                    }
                });
            }
        }

        default Stream<Decl> getDeclarations(Name name) {
            return task().getSourceUnits().stream()
                    .map(SourceUnit::getTree)
                    .flatMap(ns -> ns.getAllDecls().stream().filter(decl -> includesDecl(name, ns, decl)));
        }

        boolean includesDecl(Name name, NamespaceDecl ns, Decl decl);

        default boolean includesDecl(Name name, NamespaceDecl ns, GlobalEntityDecl entity) {
            return name.isEntity() && name.isInNamespace(ns.getQID()) && name.includesLocalName(entity.getName());
        }

        default boolean includesDecl(Name name, NamespaceDecl ns, GlobalVarDecl var) {
            return name.isVariable() && name.isInNamespace(ns.getQID()) && name.includesLocalName(var.getName());
        }

        default boolean includesDecl(Name name, NamespaceDecl ns, GlobalTypeDecl type) {
            return name.isType() && name.isInNamespace(ns.getQID()) && name.includesLocalName(type.getName());
        }

        default void scan(IRNode node) {
            add(node);
            node.forEachChild(this::scan);
        }

        default void add(IRNode node) {}

        default void add(SingleImport imp) {
            switch (imp.getKind()) {
                case VAR: addName(imp.getGlobalName(), NameKind.VAR); break;
                case ENTITY: addName(imp.getGlobalName(), NameKind.ENTITY); break;
                case TYPE: addName(imp.getGlobalName(), NameKind.TYPE); break;
            }
        }

        default void add(GroupImport imp) {
            switch (imp.getKind()) {
                case VAR: addName(imp.getGlobalName(), NameKind.VAR_NS); break;
                case ENTITY: addName(imp.getGlobalName(), NameKind.ENTITY_NS); break;
                case TYPE: addName(imp.getGlobalName(), NameKind.TYPE_NS); break;
            }
        }

        default void add(ExprGlobalVariable var) {
            addName(var.getGlobalName(), NameKind.VAR);
        }

        default void add(EntityReferenceGlobal ref) {
            addName(ref.getGlobalName(), NameKind.ENTITY);
        }

        default void add(Instance instance) {
            addName(instance.getEntityName(), NameKind.ENTITY);
        }

        default void add(NominalTypeExpr type) {
            namespace().declaration(type).ifPresent(decl -> addName(QID.parse(String.format("%s.%s", decl.getQID().toString(), type.getName())), NameKind.TYPE));
        }

        default void add(ExprTypeConstruction construction) {
            typeScopes().construction(construction)
                    .ifPresent(type -> addName(QID.parse(String.format("%s.%s", sourceUnit(type).getTree().getQID().toString(), type.getName())), NameKind.TYPE));
        }

        default void add(PatternDeconstructor deconstructor) {
            typeScopes().construction(deconstructor)
                    .ifPresent(type -> addName(QID.parse(String.format("%s.%s", sourceUnit(type).getTree().getQID().toString(), type.getName())), NameKind.TYPE));
        }

        default SourceUnit sourceUnit(IRNode node) {
            return sourceUnit(tree().parent(node));
        }

        default SourceUnit sourceUnit(SourceUnit unit) {
            return unit;
        }
    }
}
