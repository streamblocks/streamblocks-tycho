package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.EntityDeclarations;
import se.lth.cs.tycho.attribute.GlobalNames;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.TypeParameter;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.decl.AlgebraicTypeDecl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.ParameterTypeDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.EntityComprehensionExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityListExpr;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.expr.ExprTypeConstruction;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstruction;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.meta.core.MetaArgument;
import se.lth.cs.tycho.meta.core.MetaArgumentType;
import se.lth.cs.tycho.meta.core.MetaArgumentValue;
import se.lth.cs.tycho.meta.core.MetaParameter;
import se.lth.cs.tycho.meta.core.MetaParameterType;
import se.lth.cs.tycho.meta.core.MetaParameterValue;
import se.lth.cs.tycho.meta.ir.decl.MetaAlgebraicTypeDecl;
import se.lth.cs.tycho.meta.ir.decl.MetaGlobalEntityDecl;
import se.lth.cs.tycho.meta.ir.entity.nl.MetaEntityInstanceExpr;
import se.lth.cs.tycho.meta.ir.expr.MetaExprTypeConstruction;
import se.lth.cs.tycho.meta.ir.expr.pattern.MetaPatternDeconstruction;
import se.lth.cs.tycho.meta.ir.type.MetaNominalTypeExpr;
import se.lth.cs.tycho.reporting.CompilationException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemplateTransformationPhase implements Phase {

    @Override
    public String getDescription() {
        return "Transforms parameterizable and specialized nodes to meta nodes";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        Transformation transformation = MultiJ.from(Transformation.class)
                .bind("entities").to(task.getModule(EntityDeclarations.key))
                .bind("globalNames").to(task.getModule(GlobalNames.key))
                .bind("declarations").to(task.getModule(VariableDeclarations.key))
                .bind("tree").to(task.getModule(TreeShadow.key))
                .bind("task").to(task)
                .instance();
        return task.transformChildren(transformation);
    }

    @Module
    interface Transformation extends IRNode.Transformation {

        @Binding(BindingKind.INJECTED)
        EntityDeclarations entities();

        @Binding(BindingKind.INJECTED)
        GlobalNames globalNames();

        @Binding(BindingKind.INJECTED)
        CompilationTask task();

        @Binding(BindingKind.INJECTED)
        VariableDeclarations declarations();

        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default IRNode apply(GlobalEntityDecl decl) {
            Entity entity = decl.getEntity();


            QID identifier = task().getIdentifier();

            // If its Top return
            Optional<QID> qid = globalNames().globalName(decl);
            if (qid.isPresent()) {
                if (qid.get().equals(identifier)) {
                    return decl.transformChildren(this);
                }
            }

            if ((decl.getExternal()) || (entity.getTypeParameters().isEmpty() && entity.getValueParameters().isEmpty())) {
                return decl.transformChildren(this);
            }
            Stream<MetaParameter> types = entity.getTypeParameters().stream().map(this::convert);
            Stream<MetaParameter> values = entity.getValueParameters().stream().map(this::convert);
            List<MetaParameter> params = Stream.concat(types, values).collect(Collectors.toList());
            return new MetaGlobalEntityDecl(params, (GlobalEntityDecl) decl
                    //.withTypeParameters(ImmutableList.empty())
                    //.withValueParameters(ImmutableList.empty())
                    .transformChildren(this));
        }

        default IRNode apply(EntityInstanceExpr expr) {
            GlobalEntityDecl decl = entities().declaration(expr.getEntityName());
            if ((decl == null || decl.getExternal()) || (expr.getTypeParameters().isEmpty() && expr.getValueParameters().isEmpty())) {
                return expr.transformChildren(this);
            }
            Stream<MetaArgument> types = expr.getTypeParameters().stream().map(this::convert);
            Stream<MetaArgument> values = expr.getValueParameters().stream().map(this::convert);
            List<MetaArgument> args = Stream.concat(types, values).collect(Collectors.toList());
            return new MetaEntityInstanceExpr(args, (EntityInstanceExpr) expr
                    .withTypeParameters(ImmutableList.empty())
                    .withValueParameters(ImmutableList.empty())
                    .transformChildren(this)) {
            };
        }

        default IRNode apply(EntityListExpr entityList) {
            return entityList.transformChildren(this);
        }

        default IRNode apply(EntityComprehensionExpr comprehensionEntity) {
            return comprehensionEntity.transformChildren(this);
        }

        default IRNode apply(AlgebraicTypeDecl decl) {
            if (decl.getTypeParameters().isEmpty() && decl.getValueParameters().isEmpty()) {
                return decl.transformChildren(this);
            }
            Stream<MetaParameter> types = decl.getTypeParameters().stream().map(this::convert);
            Stream<MetaParameter> values = decl.getValueParameters().stream().map(this::convert);
            List<MetaParameter> params = Stream.concat(types, values).collect(Collectors.toList());
            return new MetaAlgebraicTypeDecl(params, (AlgebraicTypeDecl) decl
                    //.withTypeParameters(ImmutableList.empty())
                    //.withValueParameters(ImmutableList.empty())
                    .transformChildren(this));
        }

        default IRNode apply(NominalTypeExpr expr) {
            if (Arrays.asList("Set", "List", "Map", "int", "uint").contains(expr.getName())
                    || (expr.getTypeParameters().isEmpty() && expr.getValueParameters().isEmpty())) {
                return expr.transformChildren(this);
            }
            Stream<MetaArgument> types = expr.getTypeParameters().stream().map(this::convert);
            Stream<MetaArgument> values = expr.getValueParameters().stream().map(this::convert);
            List<MetaArgument> args = Stream.concat(types, values).collect(Collectors.toList());
            return new MetaNominalTypeExpr(args, expr
                    .withTypeParameters(ImmutableList.empty())
                    .withValueParameters(ImmutableList.empty())
                    .transformChildren(this));
        }

        default IRNode apply(ExprTypeConstruction expr) {
            if (expr.getTypeParameters().isEmpty() && expr.getValueParameters().isEmpty()) {
                return expr.transformChildren(this);
            }
            Stream<MetaArgument> types = expr.getTypeParameters().stream().map(this::convert);
            Stream<MetaArgument> values = expr.getValueParameters().stream().map(this::convert);
            List<MetaArgument> args = Stream.concat(types, values).collect(Collectors.toList());
            return new MetaExprTypeConstruction(args, (ExprTypeConstruction) expr
                    .withTypeParameters(ImmutableList.empty())
                    .withValueParameters(ImmutableList.empty())
                    .transformChildren(this));
        }

        default IRNode apply(PatternDeconstruction pattern) {
            if (pattern.getTypeParameters().isEmpty() && pattern.getValueParameters().isEmpty()) {
                return pattern;
            }
            Stream<MetaArgument> types = pattern.getTypeParameters().stream().map(this::convert);
            Stream<MetaArgument> values = pattern.getValueParameters().stream().map(this::convert);
            List<MetaArgument> args = Stream.concat(types, values).collect(Collectors.toList());
            return new MetaPatternDeconstruction(args, (PatternDeconstruction) pattern
                    .withTypeParameters(ImmutableList.empty())
                    .withValueParameters(ImmutableList.empty())
                    .transformChildren(this));
        }

        default MetaParameter convert(ParameterTypeDecl decl) {
            return new MetaParameterType(decl, decl.getName());
        }

        default MetaParameter convert(ParameterVarDecl decl) {
            return new MetaParameterValue(decl.getName(),
                    decl.getDefaultValue() == null ? null : (Expression) apply(decl.getDefaultValue()),
                    decl.getType() == null ? null : (TypeExpr) apply(decl.getType()));
        }

        default MetaArgument convert(TypeParameter param) {
            return new MetaArgumentType(param, param.getName(), (TypeExpr) apply(param.getValue()));
        }

        default MetaArgument convert(ValueParameter param) {
            return new MetaArgumentValue(param.getName(), (Expression) apply(param.getValue()));
        }

    }
}
