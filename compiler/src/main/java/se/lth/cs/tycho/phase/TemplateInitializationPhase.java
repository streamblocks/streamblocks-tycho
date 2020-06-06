package se.lth.cs.tycho.phase;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.TypeParameter;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.decl.AlgebraicTypeDecl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.ParameterTypeDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.expr.ExprTypeConstruction;
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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemplateInitializationPhase implements Phase {

	@Override
	public String getDescription() {
		return "Initialization before the template instantiation phase";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
		Transformation transformation = MultiJ.from(Transformation.class).instance();
		return task.transformChildren(transformation);
	}

	@Module
	interface Transformation extends IRNode.Transformation {

		@Override
		default IRNode apply(IRNode node) {
			return node.transformChildren(this);
		}

		default IRNode apply(GlobalEntityDecl decl) {
			Entity entity = decl.getEntity();
			if (!(entity instanceof CalActor) || (entity.getTypeParameters().isEmpty() && entity.getValueParameters().isEmpty())) {
				return decl.transformChildren(this);
			}
			Stream<MetaParameter> types  = entity.getTypeParameters().stream().map(this::convert);
			Stream<MetaParameter> values = entity.getValueParameters().stream().map(this::convert);
			List<MetaParameter> params = Stream.concat(types, values).collect(Collectors.toList());
			return new MetaGlobalEntityDecl(params, (GlobalEntityDecl) decl
					//.withTypeParameters(ImmutableList.empty())
					//.withValueParameters(ImmutableList.empty())
					.transformChildren(this));
		}

		default IRNode apply(EntityInstanceExpr expr) {
			if (expr.getTypeParameters().isEmpty() && expr.getValueParameters().isEmpty()) {
				return expr.transformChildren(this);
			}
			Stream<MetaArgument> types  = expr.getTypeParameters().stream().map(this::convert);
			Stream<MetaArgument> values = expr.getValueParameters().stream().map(this::convert);
			List<MetaArgument> args = Stream.concat(types, values).collect(Collectors.toList());
			return new MetaEntityInstanceExpr(args, (EntityInstanceExpr) expr
					.withTypeParameters(ImmutableList.empty())
					.withValueParameters(ImmutableList.empty())
					.transformChildren(this)) {
			};
		}

		default IRNode apply(AlgebraicTypeDecl decl) {
			if (decl.getTypeParameters().isEmpty() && decl.getValueParameters().isEmpty()) {
				return decl.transformChildren(this);
			}
			Stream<MetaParameter> types  = decl.getTypeParameters().stream().map(this::convert);
			Stream<MetaParameter> values = decl.getValueParameters().stream().map(this::convert);
			List<MetaParameter> params = Stream.concat(types, values).collect(Collectors.toList());
			return new MetaAlgebraicTypeDecl(params, (AlgebraicTypeDecl) decl
					//.withTypeParameters(ImmutableList.empty())
					//.withValueParameters(ImmutableList.empty())
					.transformChildren(this));
		}

		default IRNode apply(NominalTypeExpr expr) {
			if (expr.getName().equals("List")
					|| expr.getName().equals("int")
					|| expr.getName().equals("uint")
					|| (expr.getTypeParameters().isEmpty() && expr.getValueParameters().isEmpty())) {
				return expr.transformChildren(this);
			}
			Stream<MetaArgument> types  = expr.getTypeParameters().stream().map(this::convert);
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
			Stream<MetaArgument> types  = expr.getTypeParameters().stream().map(this::convert);
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
			Stream<MetaArgument> types  = pattern.getTypeParameters().stream().map(this::convert);
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
			return new MetaParameterValue(decl.getName(), decl.getDefaultValue() == null ? null : (Expression) apply(decl.getDefaultValue()));
		}

		default MetaArgument convert(TypeParameter param) {
			return new MetaArgumentType(param, param.getName(), (TypeExpr) apply(param.getValue()));
		}

		default MetaArgument convert(ValueParameter param) {
			return new MetaArgumentValue(param.getName(), (Expression) apply(param.getValue()));
		}
	}
}
