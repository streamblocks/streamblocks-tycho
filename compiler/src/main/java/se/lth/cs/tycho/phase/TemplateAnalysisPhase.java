package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.EntityDeclarations;
import se.lth.cs.tycho.attribute.TypeScopes;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.TypeParameter;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.decl.AlgebraicTypeDecl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.nl.EntityComprehensionExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityListExpr;
import se.lth.cs.tycho.ir.expr.ExprTypeConstruction;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstruction;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class TemplateAnalysisPhase implements Phase {

    @Override
    public String getDescription() {
        return "Analyzes template parameters.";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        Analysis analysis = MultiJ.from(Analysis.class)
                .bind("types").to(task.getModule(TypeScopes.key))
                .bind("entities").to(task.getModule(EntityDeclarations.key))
                .bind("tree").to(task.getModule(TreeShadow.key))
                .bind("reporter").to(context.getReporter())
                .instance();
        analysis.apply(task);
        return task;
    }

    @Module
    interface Analysis {

        @Binding(BindingKind.INJECTED)
        TypeScopes types();

        @Binding(BindingKind.INJECTED)
        EntityDeclarations entities();

        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Binding(BindingKind.INJECTED)
        Reporter reporter();

        default void apply(IRNode node) {
            analyze(node);
            node.forEachChild(this::apply);
        }

        default void analyze(IRNode node) {

        }

        default void analyze(EntityInstanceExpr expr) {
            GlobalEntityDecl decl = entities().declaration(expr.getEntityName());
            if (decl != null) {
                check(expr, expr.getTypeParameters(), expr.getValueParameters(), decl.getEntity());
            }
        }

        default void analyze(EntityListExpr listExpr) {
            for (EntityExpr entityExpr : listExpr.getEntityList()) {
                analyze(entityExpr);
            }
        }

        default void analyze(EntityComprehensionExpr comprehensionExpr) {
            analyze(comprehensionExpr.getCollection());
        }

        default void analyze(NominalTypeExpr expr) {
            Optional<TypeDecl> decl = types().declaration(expr);
            if (decl.isPresent()) {
                check(expr, expr.getTypeParameters(), expr.getValueParameters(), decl.get());
            }
        }

        default void analyze(ExprTypeConstruction expr) {
            Optional<TypeDecl> decl = types().declaration(expr);
            if (decl.isPresent()) {
                check(expr, expr.getTypeParameters(), expr.getValueParameters(), decl.get());
            }
        }

        default void analyze(PatternDeconstruction pattern) {
            Optional<TypeDecl> decl = types().declaration(pattern);
            if (decl.isPresent()) {
                check(pattern, pattern.getTypeParameters(), pattern.getValueParameters(), decl.get());
            }
        }

        default void check(IRNode node, List<TypeParameter> typeArguments, List<ValueParameter> valueArguments, IRNode decl) {

        }

        default void check(IRNode node, List<TypeParameter> typeArguments, List<ValueParameter> valueArguments, AlgebraicTypeDecl decl) {

            decl.getTypeParameters().forEach(param -> {
                if (typeArguments.stream().noneMatch(arg -> Objects.equals(arg.getName(), param.getName()))) {
                    reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Missing type argument " + param.getName() + ".", sourceUnit(node), node));
                }
            });

            typeArguments.stream().collect(Collectors.groupingBy(TypeParameter::getName)).forEach((name, args) -> {
                if (args.size() > 1) {
                    reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Duplicate type argument " + name + ".", sourceUnit(args.get(1)), args.get(1)));
                }
            });

            decl.getValueParameters().forEach(param -> {
                if (valueArguments.stream().noneMatch(arg -> Objects.equals(arg.getName(), param.getName()))) {
                    reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Missing value argument " + param.getName() + ".", sourceUnit(node), node));
                }
            });

            valueArguments.stream().collect(Collectors.groupingBy(ValueParameter::getName)).forEach((name, args) -> {
                if (args.size() > 1) {
                    reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Duplicate value argument " + name + ".", sourceUnit(args.get(1)), args.get(1)));
                }
            });
        }

        default void check(IRNode node, List<TypeParameter> typeArguments, List<ValueParameter> valueArguments, Entity entity) {

            entity.getTypeParameters().forEach(param -> {
                if (typeArguments.stream().noneMatch(arg -> Objects.equals(arg.getName(), param.getName()))) {
                    reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Missing type argument " + param.getName() + ".", sourceUnit(node), node));
                }
            });

            typeArguments.stream().collect(Collectors.groupingBy(TypeParameter::getName)).forEach((name, args) -> {
                if (args.size() > 1) {
                    reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Duplicate type argument " + name + ".", sourceUnit(args.get(1)), args.get(1)));
                }
            });

            entity.getValueParameters().forEach(param -> {
                if (valueArguments.stream().noneMatch(arg -> Objects.equals(arg.getName(), param.getName()))) {
                    reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Missing value argument " + param.getName() + ".", sourceUnit(node), node));
                }
            });

            valueArguments.stream().collect(Collectors.groupingBy(ValueParameter::getName)).forEach((name, args) -> {
                if (args.size() > 1) {
                    reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Duplicate value argument " + name + ".", sourceUnit(args.get(1)), args.get(1)));
                }
            });
        }

        default SourceUnit sourceUnit(IRNode node) {
            return sourceUnit(tree().parent(node));
        }

        default SourceUnit sourceUnit(SourceUnit unit) {
            return unit;
        }
    }
}
