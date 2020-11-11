package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.EntityDeclarations;
import se.lth.cs.tycho.attribute.GlobalNames;
import se.lth.cs.tycho.attribute.ModuleKey;
import se.lth.cs.tycho.attribute.TypeScopes;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.GlobalDeclarations;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.compiler.UniqueNumbers;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.AlgebraicTypeDecl;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.ir.decl.Import;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.SingleImport;
import se.lth.cs.tycho.ir.decl.SumTypeDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.nl.EntityReference;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceGlobal;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceLocal;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.meta.core.Meta;
import se.lth.cs.tycho.meta.core.MetaArgument;
import se.lth.cs.tycho.meta.core.MetaArgumentType;
import se.lth.cs.tycho.meta.core.MetaArgumentValue;
import se.lth.cs.tycho.meta.core.MetaParameter;
import se.lth.cs.tycho.meta.interp.Interpreter;
import se.lth.cs.tycho.meta.interp.op.Binary;
import se.lth.cs.tycho.meta.interp.op.Unary;
import se.lth.cs.tycho.meta.interp.value.Value;
import se.lth.cs.tycho.meta.interp.value.ValueUndefined;
import se.lth.cs.tycho.meta.interp.value.util.Convert;
import se.lth.cs.tycho.meta.ir.decl.MetaAlgebraicTypeDecl;
import se.lth.cs.tycho.meta.ir.decl.MetaGlobalEntityDecl;
import se.lth.cs.tycho.meta.ir.decl.MetaGlobalTypeDecl;
import se.lth.cs.tycho.meta.ir.entity.nl.MetaEntityInstanceExpr;
import se.lth.cs.tycho.meta.ir.expr.MetaExprTypeConstruction;
import se.lth.cs.tycho.meta.ir.expr.pattern.MetaPatternDeconstruction;
import se.lth.cs.tycho.meta.ir.type.MetaNominalTypeExpr;
import se.lth.cs.tycho.meta.ir.type.MetaTypeExpr;
import se.lth.cs.tycho.reporting.CompilationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemplateInstantiationPhase implements Phase {

    @Override
    public String getDescription() {
        return "Instantiates parameterizable constructs.";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        Staging staging = new Staging();

        do {

            Interpreter interpreter = MultiJ.from(Interpreter.class)
                    .bind("variables").to(task.getModule(VariableDeclarations.key))
                    .bind("types").to(task.getModule(TypeScopes.key))
                    .bind("unary").to(MultiJ.from(Unary.class).instance())
                    .bind("binary").to(MultiJ.from(Binary.class).instance())
                    .instance();

            Instantiate instantiate = MultiJ.from(Instantiate.class)
                    .bind("staging").to(staging)
                    .bind("types").to(task.getModule(TypeScopes.key))
                    .bind("interpreter").to(interpreter)
                    .bind("numbers").to(context.getUniqueNumbers())
                    .bind("declarations").to(task.getModule(VariableDeclarations.key))
                    .bind("entities").to(task.getModule(EntityDeclarations.key))
                    .bind("tree").to(task.getModule(TreeShadow.key))
                    .bind("utils").to(task.getModule(Utils.key))
                    .instance();

            Transform transform = MultiJ.from(Transform.class)
                    .bind("instantiate").to(instantiate)
                    .bind("task").to(task)
                    .bind("globalNames").to(task.getModule(GlobalNames.key))
                    .instance();

            Convert convert = MultiJ.from(Convert.class)
                    .instance();


            staging.setChanged(false);
            task = task.transformChildren(transform);
            if (staging.isChanged()) {
                Merge merge = MultiJ.from(Merge.class)
                        .bind("staging").to(staging)
                        .bind("tree").to(task.getModule(TreeShadow.key))
                        .bind("convert").to(convert)
                        .bind("utils").to(task.getModule(Utils.key))
                        .instance();
                task = task.transformChildren(merge);
                // -- clear the named interpreted values because they
                // -- they have been assigned to namespaces as GlobalVarDecl and we do not need
                // -- to keep them anymore.
                staging.interpretedValues().clear();
                staging.typeInstances().clear();
                staging.entityInstances().clear();
            }
        } while (staging.isChanged());

        Replace replace = MultiJ.from(Replace.class)
                .bind("staging").to(staging)
                .bind("tree").to(task.getModule(TreeShadow.key))
                .instance();

        CompilationTask newTask = task.transformChildren(replace);
        return newTask;
    }

    @Module
    interface Transform extends IRNode.Transformation {

        @Binding(BindingKind.INJECTED)
        Instantiate instantiate();


        @Binding(BindingKind.INJECTED)
        CompilationTask task();

        @Binding(BindingKind.INJECTED)
        GlobalNames globalNames();

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default IRNode apply(Meta meta) {
            if (!instantiate().test(meta)) {
                IRNode node = meta.transformChildren(this);
                // -- FIXME : is this correct ?
                // -- Correct but unnecessary since meta.transformChildren(this) calls apply
                // -- which eventually calls instantiate().apply(meta).transformChildren(this)
                //if (node instanceof Meta) {
                //    return instantiate().apply((Meta) node).transformChildren(this);
                //}
                return node;
            }
            return instantiate().apply(meta).transformChildren(this);
        }

/*
        default IRNode apply(MetaGlobalEntityDecl decl) {
            QID identifier = task().getIdentifier();

            Optional<QID> qid = globalNames().globalName(decl);
            if (qid.isPresent()) {
                if (qid.get().equals(identifier)) {
                   // return instantiate().apply(decl).transformChildren(this);
                }
            }
            return decl.transformChildren(this);
        }*/
    }

    @Module
    interface Instantiate {

        @Binding(BindingKind.INJECTED)
        Staging staging();

        @Binding(BindingKind.INJECTED)
        TypeScopes types();

        @Binding(BindingKind.INJECTED)
        Interpreter interpreter();

        @Binding(BindingKind.INJECTED)
        UniqueNumbers numbers();

        @Binding(BindingKind.INJECTED)
        VariableDeclarations declarations();

        @Binding(BindingKind.INJECTED)
        EntityDeclarations entities();

        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Binding(BindingKind.INJECTED)
        Utils utils();

        default boolean test(Meta meta) {
            return meta.getArguments().stream().allMatch(this::test);
        }

        default boolean test(MetaArgument arg) {
            return false;
        }

        default boolean test(MetaArgumentType arg) {
            return !(arg.getType() instanceof MetaTypeExpr);
        }

        default boolean test(MetaArgumentValue arg) {
            if(arg.isExternal()){
                return true;
            }
            return interpreter().apply(arg.getValue()) != ValueUndefined.undefined();
        }

        default IRNode apply(Meta meta) {
            // Update staging context: mark changed
            boolean changed = staging().isChanged();
            staging().setChanged(true);

            // Check existing instance
            if (staging().typeDeclarations().containsKey(nameOf(meta))) {
                return renameOf(meta, staging().typeDeclarations().get(nameOf(meta)).getName());
            }

            // Find declaration
            Optional<TypeDecl> decl = declarationOf(meta);
            if (!(decl.isPresent())) {
                staging().setChanged(changed);
                return meta;
            }

            MetaAlgebraicTypeDecl metaDecl = (MetaAlgebraicTypeDecl) decl.get();

            // Instantiate declaration
            AlgebraicTypeDecl template = metaDecl.getAlgebraicTypeDecl().withTypeParameters(ImmutableList.empty()).withValueParameters(ImmutableList.empty());
            AlgebraicTypeDecl instance = (AlgebraicTypeDecl) template.clone();

            // Specialize instance
            String name = nameOf(meta);

            Rename rename = MultiJ.from(Rename.class)
                    .bind("name").to(name)
                    .instance();

            instance = (AlgebraicTypeDecl) rename.apply(instance);

            // Substitute meta parameters
            List<MetaParameter> metaParameters = metaDecl.getParameters().stream().sorted(Comparator.comparing(MetaParameter::getName)).collect(Collectors.toList());
            List<MetaArgument> metArguments = meta.getArguments().stream().sorted(Comparator.comparing(MetaArgument::getName)).collect(Collectors.toList());
            Map<String, String> tpes = new HashMap<>();
            Map<VarDecl, String> vals = new HashMap<>();
            for (int i = 0; i < metArguments.size(); ++i) {
                MetaParameter metaParam = metaParameters.get(i);
                MetaArgument metaArg = metArguments.get(i);
                if (metaArg instanceof MetaArgumentType) {
                    tpes.put(metaParam.getName(), ((NominalTypeExpr) ((MetaArgumentType) metaArg).getType()).getName());
                } else {
                    Value value = interpreter().apply(((MetaArgumentValue) metaArg).getValue());

                    String valueName = setValueName(sourceUnit(metaDecl), value);

                    ParameterVarDecl declaration = metaDecl.getAlgebraicTypeDecl().getValueParameters().stream().filter(param -> param.getName().equals(metaArg.getName())).findAny().get();
                    vals.put(declaration, valueName);

                }
            }

            SubstituteType substituteType = MultiJ.from(SubstituteType.class)
                    .bind("substitution").to(new TypeSubstitution(tpes))
                    .instance();

            instance = (AlgebraicTypeDecl) substituteType.apply(instance);
            Utils.Routines utils = MultiJ.from(Utils.Routines.class).bind("tree").to(tree()).instance();
            SubstituteValue substituteValue = MultiJ.from(SubstituteValue.class)
                    .bind("substitution").to(new ValueSubstitution(vals))
                    .bind("declarations").to(declarations())
                    .bind("tree").to(tree())
                    .bind("utils").to(utils)
                    .instance();

            instance = (AlgebraicTypeDecl) substituteValue.apply(instance);

            // Fork template instance
            instance = (AlgebraicTypeDecl) instance.withTypeParameters(ImmutableList.empty()).withValueParameters(ImmutableList.empty()).deepClone();

            // Rename node
            IRNode node = renameOf(meta, nameOf(meta));

            // Update staging context: save instance
            staging().typeDeclarations().put(instance.getName(), instance);

            if (instance instanceof SumTypeDecl) {
                for (SumTypeDecl.VariantDecl variantDecl : ((SumTypeDecl) instance).getVariants()) {
                    staging().typeDeclarations().put(variantDecl.getName(), instance);
                }
            } else {
                staging().typeDeclarations().put(nameOf(meta), instance);
            }

            if (staging().typeInstances().containsKey(metaDecl)) {
                staging().typeInstances().get(metaDecl).add(instance);
            } else {
                staging().typeInstances().put(metaDecl, new ArrayList<>(Collections.singletonList(instance)));
            }

            return node;
        }

        default IRNode apply(MetaEntityInstanceExpr meta) {
            // Update staging context: mark changed
            boolean changed = staging().isChanged();
            staging().setChanged(true);

            // Check existing instance
            if (staging().entityDeclarations().containsKey(staging().symbolics().getOrDefault(nameOf(meta), "<none>"))) {
                return renameOf(meta, staging().entityDeclarations().get(staging().symbolics().get(nameOf(meta))).getName());
            }

            // Find declaration
            GlobalEntityDecl decl = entities().declaration(meta.getEntityInstanceExpr().getEntityName());
            if (decl == null || !(decl instanceof MetaGlobalEntityDecl)) {
                staging().setChanged(changed);
                return meta;
            }

            MetaGlobalEntityDecl metaDecl = (MetaGlobalEntityDecl) decl;

            // Instantiate declaration
            Entity entity = metaDecl.getEntity();
            GlobalEntityDecl template = metaDecl.withEntity(entity.withTypeParameters(ImmutableList.empty()).withValueParameters(ImmutableList.empty()));
            GlobalEntityDecl instance = template.clone();

            // Specialize instance
            String symbolic = nameOf(meta);
            String logic = template.getName() + "_" + numbers().next();

            staging().symbolics().put(symbolic, logic);

            if (staging().imported().containsKey(sourceUnit(meta).getTree())) {
                staging().imported().get(sourceUnit(meta).getTree()).add(template.getName());
            } else {
                Set<String> values = new HashSet<>();
                values.add(template.getName());
                staging().imported().put(sourceUnit(meta).getTree(), values);
            }

            if (staging().renamed().containsKey(template.getName())) {
                staging().renamed().get(template.getName()).add(logic);
            } else {
                Set<String> values = new HashSet<>();
                values.add(logic);
                staging().renamed().put(template.getName(), values);
            }

            Rename rename = MultiJ.from(Rename.class)
                    .bind("name").to(logic)
                    .instance();

            instance = (GlobalEntityDecl) rename.apply(instance);

            // Substitute meta parameters
            List<MetaParameter> metaParameters = metaDecl.getParameters().stream().sorted(Comparator.comparing(MetaParameter::getName)).collect(Collectors.toList());
            List<MetaArgument> metArguments = meta.getArguments().stream().sorted(Comparator.comparing(MetaArgument::getName)).collect(Collectors.toList());
            Map<String, String> tpes = new HashMap<>();
            Map<VarDecl, String> vals = new HashMap<>();
            for (int i = 0; i < metArguments.size(); ++i) {
                MetaParameter metaParam = metaParameters.get(i);
                MetaArgument metaArg = metArguments.get(i);
                if (metaArg instanceof MetaArgumentType) {
                    tpes.put(metaParam.getName(), ((NominalTypeExpr) ((MetaArgumentType) metaArg).getType()).getName());
                } else {


                    Value value = interpreter().apply(((MetaArgumentValue) metaArg).getValue());

                    String valueName = setValueName(sourceUnit(metaDecl), value);

                    ParameterVarDecl declaration = metaDecl.getEntity().getValueParameters().stream().filter(param -> param.getName().equals(metaArg.getName())).findAny().get();
                    vals.put(declaration, valueName);

                }
            }

            SubstituteType substituteType = MultiJ.from(SubstituteType.class)
                    .bind("substitution").to(new TypeSubstitution(tpes))
                    .instance();
            instance = (GlobalEntityDecl) substituteType.apply(instance);

            Utils.Routines utils = MultiJ.from(Utils.Routines.class).bind("tree").to(tree()).instance();
            SubstituteValue substituteValue = MultiJ.from(SubstituteValue.class)
                    .bind("substitution").to(new ValueSubstitution(vals))
                    .bind("declarations").to(declarations())
                    .bind("tree").to(tree())
                    .bind("utils").to(utils)
                    .instance();

            instance = (GlobalEntityDecl) substituteValue.apply(instance);

            // Fork template instance
            instance = instance.deepClone();

            // Rename node
            IRNode node = renameOf(meta, logic);

            // Update staging context: save instance
            staging().entityDeclarations().put(instance.getName(), instance);

            if (staging().entityInstances().containsKey(metaDecl)) {
                staging().entityInstances().get(metaDecl).add(instance);
            } else {
                staging().entityInstances().put(metaDecl, new ArrayList<>(Collections.singletonList(instance)));
            }

            return node;
        }

        default IRNode apply(MetaGlobalEntityDecl meta) {
            // Update staging context: mark changed
            boolean changed = staging().isChanged();
            staging().setChanged(true);

            return meta;
        }

        default NamespaceDecl namespaceOf(MetaAlgebraicTypeDecl metaDecl) {
            return sourceUnit(metaDecl).getTree();
        }
        default NamespaceDecl namespaceOf(MetaGlobalEntityDecl metaDecl) {
            return sourceUnit(metaDecl).getTree();
        }

        /**
         * Sets a unique name for an interpreted value in a namespace. The named value is also tied to a SourceUnit.
         * The if the named value is only tied to the same namespace, it could be duplicated among multiple sourceUnits
         * that span the namespace.
         * @param sourceUnit The sourceUnit that will possibly contain a named value given by Value argument
         * @param value the value to be named and staged
         * @return a new unique name for the value in the namespace of the sourceUnit or a unique name for that value
         *         from a another sourceUnit spanning the same namespace
         */
        default String setValueName(SourceUnit sourceUnit, Value value) {

            NamespaceDecl namespace = sourceUnit.getTree();
            // -- the first time encountering a namespace
            // -- need to initialize the set of named values
            staging().interpretedValues().putIfAbsent(namespace, new HashSet<>());

            String valueName;

            // -- get the set of "already named values" in the current namespace
            Set<Staging.NamedValue> namedVals = staging().interpretedValues().get(namespace);
            // -- check whether a new NamedValue is needed
            // -- (i.e., if the value is not named yet in this namespace)
            List<Staging.NamedValue> sameValues =
                    namedVals.stream().filter(nv -> nv.getValue().equals(value))
                            .collect(Collectors.toList());
            if (sameValues.size() == 0) {
                // -- the value has not been named yet
                valueName = "$eval" + numbers().next();
                // -- keep the new NamedValue in the set
                namedVals.add(Staging.NamedValue.of(valueName, value, sourceUnit));
            } else {
                // -- the value has already been named
                valueName = sameValues.get(0).getName();
            }
            return valueName;
        }
        default String nameOf(Meta meta) {
            return name(meta) + meta.getArguments().stream()
                    .map(arg -> {
                        if (arg instanceof MetaArgumentType) {
                            return ((NominalTypeExpr) (((MetaArgumentType) arg).getType())).getName();
                        } else {
                            return interpreter().apply(((MetaArgumentValue) arg).getValue()).toString();
                        }
                    })
                    .collect(Collectors.joining(", ", "(", ")"));
        }

        default String name(IRNode node) {
            return null;
        }

        default String name(MetaNominalTypeExpr meta) {
            return meta.getNominalTypeExpr().getName();
        }

        default String name(MetaExprTypeConstruction meta) {
            return meta.getExprTypeConstruction().getConstructor();
        }

        default String name(MetaPatternDeconstruction meta) {
            return meta.getPatternDeconstruction().getDeconstructor();
        }

        default String name(MetaEntityInstanceExpr meta) {
            EntityReference reference = meta.getEntityInstanceExpr().getEntityName();
            if (reference instanceof EntityReferenceLocal) {
                return ((EntityReferenceLocal) reference).getName();
            } else {
                return ((EntityReferenceGlobal) reference).getGlobalName().toString();
            }
        }

        default IRNode renameOf(Meta meta, String name) {
            return rename(meta, name);
        }

        default IRNode rename(IRNode node, String name) {
            return null;
        }

        default IRNode rename(MetaNominalTypeExpr meta, String name) {
            return meta.getNominalTypeExpr().withName(name);
        }

        default IRNode rename(MetaExprTypeConstruction meta, String name) {
            return meta.getExprTypeConstruction().withConstructor(name);
        }

        default IRNode rename(MetaPatternDeconstruction meta, String name) {
            return meta.getPatternDeconstruction().withDeconstructor(name);
        }

        default IRNode rename(MetaEntityInstanceExpr meta, String name) {
            EntityReference reference = meta.getEntityInstanceExpr().getEntityName();
            if (reference instanceof EntityReferenceLocal) {
                return meta.getEntityInstanceExpr().withEntityName(((EntityReferenceLocal) reference).withName(name));
            } else {
                EntityReferenceGlobal eGlobal = (EntityReferenceGlobal) reference;
                QID prefix = eGlobal.getGlobalName().getButLast();
                QID qid = prefix.concat(QID.of(name));
                return meta.getEntityInstanceExpr().withEntityName(((EntityReferenceGlobal) reference).withGlobalName(qid));
            }
        }

        default Optional<TypeDecl> declarationOf(Meta meta) {
            return declaration(meta);
        }

        default Optional<TypeDecl> declaration(IRNode node) {
            return null;
        }

        default Optional<TypeDecl> declaration(MetaNominalTypeExpr meta) {
            return types().declaration(meta.getNominalTypeExpr());
        }

        default Optional<TypeDecl> declaration(MetaExprTypeConstruction meta) {
            return types().construction(meta.getExprTypeConstruction());
        }

        default Optional<TypeDecl> declaration(MetaPatternDeconstruction meta) {
            return types().construction(meta.getPatternDeconstruction());
        }

        default SourceUnit sourceUnit(IRNode node) {
            return utils().sourceUnit(node);
        }
//        default SourceUnit sourceUnit(IRNode node) {
//            return sourceUnit(tree().parent(node));
//        }
//
//        default SourceUnit sourceUnit(CompilationTask task) {
//            GlobalEntityDecl entityDecl = GlobalDeclarations.getEntity(task, task.getIdentifier());
//            return sourceUnit(tree().parent(entityDecl));
//        }
//
//        default SourceUnit sourceUnit(SourceUnit unit) {
//            return unit;
//        }
    }

    @Module
    interface Rename extends IRNode.Transformation {

        @Binding(BindingKind.INJECTED)
        String name();

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default IRNode apply(GlobalTypeDecl decl) {
            return decl.withName(name()).transformChildren(this);
        }

        default IRNode apply(AlgebraicTypeDecl decl) {
            return decl.withName(name()).transformChildren(this);
        }

        default IRNode apply(SumTypeDecl.VariantDecl decl) {
            return decl.withName(name()).transformChildren(this);
        }

        default IRNode apply(GlobalEntityDecl decl) {
            return decl.withName(name());
        }
    }

    @Module
    interface SubstituteType extends IRNode.Transformation {

        @Binding(BindingKind.INJECTED)
        TypeSubstitution substitution();

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default IRNode apply(MetaNominalTypeExpr meta) {
            // short circuit
            return meta.withArguments(meta.getArguments().stream().map(t -> (MetaArgument) apply(t)).collect(Collectors.toList()));
        }

        default IRNode apply(NominalTypeExpr expr) {
            if (substitution().mapping().containsKey(expr.getName())) {
                return expr.withName(substitution().mapping().get(expr.getName())).transformChildren(this);
            } else {
                return expr.transformChildren(this);
            }
        }
    }

    @Module
    interface SubstituteValue extends IRNode.Transformation {

        @Binding(BindingKind.INJECTED)
        ValueSubstitution substitution();

        @Binding(BindingKind.INJECTED)
        VariableDeclarations declarations();

        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Binding(BindingKind.INJECTED)
        Utils.Routines utils();

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default IRNode apply(ExprVariable expr) {
            VarDecl decl = declarations().declaration(expr);
            if (decl != null && substitution().mapping().containsKey(decl)) {
                QID qid = sourceUnit(expr).getTree().getQID().concat(QID.of(substitution().mapping().get(decl)));
                return new ExprGlobalVariable(expr, qid);
            }
            return expr;
        }

        default SourceUnit sourceUnit(IRNode node) {
            return utils().sourceUnit(node);
        }

    }

    @Module
    interface Replace extends IRNode.Transformation {

        @Binding(BindingKind.INJECTED)
        Staging staging();

        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default IRNode apply(NamespaceDecl decl) {
            List<GlobalTypeDecl> typeDecls = decl.getTypeDecls().stream().filter(typeDecl -> !(typeDecl instanceof MetaGlobalTypeDecl)).collect(Collectors.toList());
            for (GlobalTypeDecl metaDecl : decl.getTypeDecls().stream().filter(typeDecl -> typeDecl instanceof MetaGlobalTypeDecl).collect(Collectors.toList())) {
                if (typeDecls.stream().noneMatch(typeDecl -> typeDecl.getName().equals(metaDecl.getOriginalName()))) {
                    typeDecls.add(metaDecl);
                }
            }
            List<GlobalEntityDecl> entityDecls = decl.getEntityDecls().stream().filter(entityDecl -> !(entityDecl instanceof MetaGlobalEntityDecl)).collect(Collectors.toList());
            for (GlobalEntityDecl metaDecl : decl.getEntityDecls().stream().filter(entityDecl -> entityDecl instanceof MetaGlobalEntityDecl).collect(Collectors.toList())) {
                if (entityDecls.stream().noneMatch(typeDecl -> typeDecl.getOriginalName().equals(metaDecl.getName()))) {
                    entityDecls.add(metaDecl);
                }
            }
            List<Import> imports = new ArrayList<>();
            Optional<Map.Entry<NamespaceDecl, Set<String>>> imported = staging().imported().entrySet().stream().filter(e -> e.getKey().getQID().equals(decl.getQID())).findAny();
            if (imported.isPresent()) {
                Set<String> instantiated = imported.get().getValue();
                for (Import importt : decl.getImports()) {
                    if (importt instanceof SingleImport && instantiated.contains(((SingleImport) importt).getLocalName())) {
                        SingleImport singleImport = (SingleImport) importt;
                        for (String newImportName : staging().renamed().get(singleImport.getLocalName())) {
                            imports.add(((SingleImport) singleImport.deepClone())
                                    .withLocalName(newImportName)
                                    .withGlobalName(singleImport.getGlobalName().getButLast().concat(QID.of(newImportName))));
                        }
                    } else {
                        imports.add(importt);
                    }
                }
            } else {
                imports = decl.getImports();
            }
            return decl.withImports(imports).withTypeDecls(typeDecls).withEntityDecls(entityDecls);
        }
    }

    @Module
    interface Merge extends IRNode.Transformation {

        @Binding(BindingKind.INJECTED)
        Staging staging();

        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Binding(BindingKind.INJECTED)
        Convert convert();

        @Binding(BindingKind.INJECTED)
        Utils utils();

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default IRNode apply(NamespaceDecl decl) {
            SourceUnit sourceFile = sourceUnit(decl);
            Stream<GlobalTypeDecl> instantiatedTypes = staging().typeInstances().entrySet().stream()
                    .filter(e -> decl.getTypeDecls().contains(e.getKey())).flatMap(e -> e.getValue().stream());
            Stream<GlobalTypeDecl> remainingTypes = decl.getTypeDecls().stream()
                    .filter(typeDecl -> !(typeDecl instanceof MetaAlgebraicTypeDecl));
//            Stream<GlobalVarDecl> interpreted = staging().globals().entrySet().stream()
//                    .filter(e -> decl.getQID().equals(e.getKey().getQID()))
//                    .flatMap(e -> e.getValue().stream())
//                    .distinct()
//                    .filter(v -> staging().globalValueUnit().get(v).getLocation().equals(sourceFile.getLocation())) // Make sure the value comes from its original SourceUnit
//                    .map(v -> new GlobalVarDecl(ImmutableList.empty(), Availability.PUBLIC, null, staging().globals1().get(v), convert().apply(v)));
            Stream<GlobalVarDecl> interpreted = staging().interpretedValues().entrySet().stream()
                    .filter(e -> decl.getQID().equals(e.getKey().getQID()))
                    .flatMap(e -> e.getValue().stream())
                    .distinct()
                    .filter(namedVal -> namedVal.sameSource(sourceFile))
                    .map(namedVal -> new GlobalVarDecl(ImmutableList.empty(), Availability.PUBLIC, null, namedVal.getName(), convert().apply(namedVal.getValue())));
            Stream<GlobalEntityDecl> instantiatedEntities = staging().entityInstances().entrySet().stream()
                    .filter(e -> decl.getEntityDecls().contains(e.getKey())).flatMap(e -> e.getValue().stream());
            Stream<GlobalEntityDecl> remainingEntities = decl.getEntityDecls().stream()
                    .filter(entityDecl -> !(staging().entityInstances().entrySet().contains(entityDecl)));

            NamespaceDecl newDecl = decl
                    .withTypeDecls(Stream.concat(instantiatedTypes, remainingTypes).collect(Collectors.toList()))
                    .withEntityDecls(Stream.concat(instantiatedEntities, remainingEntities).collect(Collectors.toList()))
                    .withVarDecls(Stream.concat(decl.getVarDecls().stream(), interpreted).collect(Collectors.toList()));
            return newDecl;
        }


        default SourceUnit sourceUnit(IRNode node) {
            return utils().sourceUnit(node);
        }


    }

    static class Staging {

        static class NamedValue {
            private final String name;
            private final Value value;
            private final SourceUnit sourceUnit;
            public NamedValue(String name, Value value, SourceUnit sourceUnit) {
                this.name = name;
                this.value = value;
                this.sourceUnit = sourceUnit;
            }
            static public NamedValue of(String name, Value value, SourceUnit sourceUnit) {
                return new NamedValue(name, value, sourceUnit);
            }
            public Value getValue() {
                return this.value;
            }
            public String getName() {
                return this.name;
            }
            public SourceUnit getSourceUnit() { return this.sourceUnit; }
            public boolean sameSource(SourceUnit thatSource) {
                return this.sourceUnit.getLocation().equals(thatSource.getLocation());
            }

        }
        private boolean changed;

        // -- Map from namespace to a set of "named" interpreted values,
        // -- for every interpreted value v, we only keep a single name per namespace
        // -- same values across different namespaces will have different names to keep the
        // -- namespaces independent of each other. If we do not use namespaces to k
        private final Map<NamespaceDecl, Set<NamedValue>> interpretedValues = new HashMap<>();

        private final Map<String, AlgebraicTypeDecl> typeDeclarations = new HashMap<>();
        private final Map<MetaAlgebraicTypeDecl, List<AlgebraicTypeDecl>> typeInstances = new HashMap<>();
        private final Map<String, GlobalEntityDecl> entityDeclarations = new HashMap<>();
        private final Map<MetaGlobalEntityDecl, List<GlobalEntityDecl>> entityInstances = new HashMap<>();

        private final Map<String, String> symbolics = new HashMap<>();
        private final Map<String, Set<String>> renamed = new HashMap<>();
        private final Map<NamespaceDecl, Set<String>> imported = new HashMap<>();

        public boolean isChanged() {
            return changed;
        }

        public void setChanged(boolean changed) {
            this.changed = changed;
        }


        public Map<String, AlgebraicTypeDecl> typeDeclarations() {
            return typeDeclarations;
        }

        public Map<MetaAlgebraicTypeDecl, List<AlgebraicTypeDecl>> typeInstances() {
            return typeInstances;
        }

        public Map<String, GlobalEntityDecl> entityDeclarations() {
            return entityDeclarations;
        }

        public Map<MetaGlobalEntityDecl, List<GlobalEntityDecl>> entityInstances() {
            return entityInstances;
        }

        public Map<String, String> symbolics() {
            return symbolics;
        }

        public Map<String, Set<String>> renamed() {
            return renamed;
        }

        public Map<NamespaceDecl, Set<String>> imported() {
            return imported;
        }

        public Map<NamespaceDecl, Set<NamedValue>> interpretedValues() { return interpretedValues; }
    }

    static class TypeSubstitution {

        private final Map<String, String> mapping;

        public TypeSubstitution(Map<String, String> mapping) {
            this.mapping = mapping;
        }

        public Map<String, String> mapping() {
            return mapping;
        }
    }

    static class ValueSubstitution {

        private final Map<VarDecl, String> mapping;

        public ValueSubstitution(Map<VarDecl, String> mapping) {
            this.mapping = mapping;
        }

        public Map<VarDecl, String> mapping() {
            return mapping;
        }
    }


    interface Utils {
        ModuleKey<Utils> key = task -> MultiJ.from(Routines.class)
                .bind("tree").to(task.getModule(TreeShadow.key))
                .instance();

        SourceUnit sourceUnit(IRNode node);

        @Module
        interface Routines extends Utils {

            @Binding(BindingKind.INJECTED)
            TreeShadow tree();

            default SourceUnit sourceUnit(IRNode node) {
                return sourceUnit(tree().parent(node));
            }

            default SourceUnit sourceUnit(CompilationTask task) {
                GlobalEntityDecl entityDecl = GlobalDeclarations.getEntity(task, task.getIdentifier());
                return sourceUnit(tree().parent(entityDecl));
            }

            default SourceUnit sourceUnit(SourceUnit unit) {
                return unit;
            }

        }

    }
}
