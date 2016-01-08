package se.lth.cs.tycho.phases;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.phases.attributes.Closures;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.util.Set;

public class ClosureAnalysisPhase implements Phase {
	@Override
	public String getDescription() {
		return "Checks if the closures are supported.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		Closures closures = context.getAttributeManager().getAttributeModule(Closures.key, task);
		task.getSourceUnits().stream().forEach(unit -> {
			CheckClosures analysis = MultiJ.from(CheckClosures.class)
					.bind("closures").to(closures)
					.bind("reporter").to(context.getReporter())
					.bind("sourceUnit").to(unit)
					.bind("tree").to(context.getAttributeManager().getAttributeModule(TreeShadow.key, task))
					.instance();
			analysis.checkClosures(unit);
		});
		return task;
	}

	@Module
	public interface CheckClosures {
		@Binding(BindingKind.INJECTED)
		Closures closures();

		@Binding
		Reporter reporter();

		@Binding
		SourceUnit sourceUnit();

		@Binding
		TreeShadow tree();

		default void checkClosures(IRNode node) {
			node.forEachChild(this::checkClosures);
		}

		default void checkClosures(ExprLambda lambda) {
			Set<VarDecl> free = closures().freeVariables(lambda);
			if (!free.isEmpty()) {
				String name = name(tree().parent(lambda));
				StringBuilder builder = new StringBuilder();
				builder.append("Free variables of \'").append(name).append("\':\n");
				for (VarDecl f : free) {
					builder.append('\t').append(f.getName()).append('\n');
				}
				reporter().report(new Diagnostic(Diagnostic.Kind.INFO, builder.toString(), sourceUnit(), lambda));
			}
			lambda.forEachChild(this::checkClosures);
		}

		default void checkClosures(ExprProc proc) {
			Set<VarDecl> free = closures().freeVariables(proc);
			if (!free.isEmpty()) {
				String name = name(tree().parent(proc));
				StringBuilder builder = new StringBuilder();
				builder.append("Free variables of \'").append(name).append("\':\n");
				for (VarDecl f : free) {
					builder.append('\t').append(f.getName()).append('\n');
				}
				reporter().report(new Diagnostic(Diagnostic.Kind.INFO, builder.toString(), sourceUnit(), proc));
			}
			proc.forEachChild(this::checkClosures);
		}

		default String name(VarDecl var) {
			return var.getName();
		}
		default String name(IRNode other) {
			return "<anonymous>";
		}
	}
}
