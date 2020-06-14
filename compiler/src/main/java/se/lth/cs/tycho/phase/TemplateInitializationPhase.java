package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.EntityDeclarations;
import se.lth.cs.tycho.attribute.TypeScopes;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.decl.AlgebraicTypeDecl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.expr.ExprTypeConstruction;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstruction;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TemplateInitializationPhase implements Phase {

	public String getDescription() {
		return "Adds missing value arguments with default value of value parameters if any";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
		Transformation transformation = MultiJ.from(Transformation.class)
				.bind("types").to(task.getModule(TypeScopes.key))
				.bind("entities").to(task.getModule(EntityDeclarations.key))
				.bind("tree").to(task.getModule(TreeShadow.key))
				.instance();
		return task.transformChildren(transformation);
	}

	@Module
	interface Transformation extends IRNode.Transformation {

		@Binding(BindingKind.INJECTED)
		TypeScopes types();

		@Binding(BindingKind.INJECTED)
		EntityDeclarations entities();

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Override
		default IRNode apply(IRNode node) {
			return node.transformChildren(this);
		}

		default IRNode apply(EntityInstanceExpr expr) {
			GlobalEntityDecl decl = entities().declaration(expr.getEntityName());
			if (decl != null && decl.getEntity() instanceof CalActor) {
				CalActor actor = (CalActor) decl.getEntity();
				List<ValueParameter> values = provided(actor.getValueParameters(), expr.getValueParameters());
				if (!(values.isEmpty())) {
					values.addAll(expr.getValueParameters());
					return expr.withValueParameters(ImmutableList.from(values)).transformChildren(this);
				}
			}
			return expr.transformChildren(this);
		}

		default IRNode apply(NominalTypeExpr expr) {
			Optional<TypeDecl> decl = types().declaration(expr);
			if (decl.isPresent() && decl.get() instanceof AlgebraicTypeDecl) {
				AlgebraicTypeDecl algebraic = (AlgebraicTypeDecl) decl.get();
				List<ValueParameter> values = provided(algebraic.getValueParameters(), expr.getValueParameters());
				if (!(values.isEmpty())) {
					values.addAll(expr.getValueParameters());
					return expr.withValueParameters(ImmutableList.from(values)).transformChildren(this);
				}
			}
			return expr.transformChildren(this);
		}

		default IRNode apply(ExprTypeConstruction expr) {
			Optional<TypeDecl> decl = types().declaration(expr);
			if (decl.isPresent() && decl.get() instanceof AlgebraicTypeDecl) {
				AlgebraicTypeDecl algebraic = (AlgebraicTypeDecl) decl.get();
				List<ValueParameter> values = provided(algebraic.getValueParameters(), expr.getValueParameters());
				if (!(values.isEmpty())) {
					values.addAll(expr.getValueParameters());
					return expr.withValueParameters(ImmutableList.from(values)).transformChildren(this);
				}
			}
			return expr.transformChildren(this);
		}

		default IRNode apply(PatternDeconstruction pattern) {
			Optional<TypeDecl> decl = types().declaration(pattern);
			if (decl.isPresent() && decl.get() instanceof AlgebraicTypeDecl) {
				AlgebraicTypeDecl algebraic = (AlgebraicTypeDecl) decl.get();
				List<ValueParameter> values = provided(algebraic.getValueParameters(), pattern.getValueParameters());
				if (!(values.isEmpty())) {
					values.addAll(pattern.getValueParameters());
					return pattern.withValueParameters(ImmutableList.from(values)).transformChildren(this);
				}
			}
			return pattern.transformChildren(this);
		}

		default List<ValueParameter> provided(List<ParameterVarDecl> params, List<ValueParameter> args) {
			return params.stream()
					.filter(param -> param.getDefaultValue() != null && args.stream().noneMatch(arg -> arg.getName().equals(param.getName())))
					.map(param -> new ValueParameter(param.getName(), param.getDefaultValue().deepClone()))
					.collect(Collectors.toList());
		}

	}
}
