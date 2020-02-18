package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.TypeScopes;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprConstruction;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;

public class ResolveApplicationLikeConstructionPhase implements Phase {

	@Override
	public String getDescription() {
		return "Resolve expression application-like constructions";
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
			Expression function = application.getFunction();
			if (!(function instanceof ExprVariable)) {
				return application;
			}
			return typeScopes()
					.declaration((ExprVariable) function)
					.map(GlobalTypeDecl.class::cast)
					.filter(decl -> decl.getStructures().size() == 1)
					.map(decl -> {
						ExprConstruction construction = new ExprConstruction(decl.getName(), null, application.getArgs());
						construction.setPosition(
								application.getFromLineNumber(),
								application.getFromColumnNumber(),
								application.getToLineNumber(),
								application.getToColumnNumber());
						return (IRNode) construction;
					})
					.orElse(application);
		}
	}
}
