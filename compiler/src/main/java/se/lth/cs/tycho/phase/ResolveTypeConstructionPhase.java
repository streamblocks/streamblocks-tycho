package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.TypeScopes;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.AlgebraicTypeDecl;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.ir.decl.SumTypeDecl;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprTypeConstruction;
import se.lth.cs.tycho.ir.expr.ExprVariable;

import java.util.Collections;
import java.util.Objects;

public class ResolveTypeConstructionPhase implements Phase {

	@Override
	public String getDescription() {
		return "Resolve type constructions";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		Transformation transformation = MultiJ.from(Transformation.class)
				.bind("typeScopes").to(task.getModule(TypeScopes.key))
				.instance();
		return task.transformChildren(transformation);
	}

	@Module
	interface Transformation extends IRNode.Transformation {

		@Binding(BindingKind.INJECTED)
		TypeScopes typeScopes();

		@Override
		default IRNode apply(IRNode node) {
			return node.transformChildren(this);
		}

		default IRNode apply(ExprApplication application) {
			return typeScopes()
					.construction(application.getFunction())
					.map(decl -> {
						String constructor = decl.getName();
						AlgebraicTypeDecl algebraicTypeDecl = ((GlobalTypeDecl) decl).getDeclaration();
						if (algebraicTypeDecl instanceof SumTypeDecl) {
							constructor = ((SumTypeDecl) algebraicTypeDecl).getVariants().stream().filter(variant -> Objects.equals(variant.getName(), ((ExprVariable) application.getFunction()).getVariable().getName())).findAny().get().getName();
						}
						return (IRNode) new ExprTypeConstruction(application, constructor, application.getArgs());
					})
					.orElse(application);
		}

		default IRNode apply(ExprVariable variable) {
			return typeScopes()
					.construction(variable)
					.filter(decl -> ((GlobalTypeDecl) decl).getDeclaration() instanceof SumTypeDecl)
					.map(decl -> {
						SumTypeDecl sum = (SumTypeDecl) ((GlobalTypeDecl) decl).getDeclaration();
						return (IRNode) new ExprTypeConstruction(variable, sum.getVariants().stream().filter(variant -> Objects.equals(variant.getName(), variable.getVariable().getName())).findAny().get().getName(), Collections.emptyList());
					})
					.orElse(variable);
		}
	}
}
