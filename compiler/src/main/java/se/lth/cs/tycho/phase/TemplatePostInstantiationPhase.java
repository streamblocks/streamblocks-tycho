package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.meta.ir.entity.nl.MetaEntityInstanceExpr;
import se.lth.cs.tycho.meta.ir.expr.MetaExprTypeConstruction;
import se.lth.cs.tycho.meta.ir.expr.pattern.MetaPatternDeconstruction;
import se.lth.cs.tycho.meta.ir.type.MetaNominalTypeExpr;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

public class TemplatePostInstantiationPhase implements Phase {

	@Override
	public String getDescription() {
		return "Checks non-instantiated meta constructs.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
		Analysis analysis = MultiJ.from(Analysis.class)
				.bind("tree").to(task.getModule(TreeShadow.key))
				.bind("reporter").to(context.getReporter())
				.instance();
		analysis.apply(task);
		return task;
	}

	@Module
	interface Analysis {

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

		default void analyze(MetaEntityInstanceExpr meta) {
			report(meta.getEntityInstanceExpr());
		}

		default void analyze(MetaPatternDeconstruction meta) {
			report(meta.getPatternDeconstruction());
		}

		default void analyze(MetaExprTypeConstruction meta) {
			report(meta.getExprTypeConstruction());
		}

		default void analyze(MetaNominalTypeExpr meta) {
			report(meta.getNominalTypeExpr());
		}

		default void report(IRNode node) {
			reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Could not instantiate meta construct.", sourceUnit(node), node));
		}

		default SourceUnit sourceUnit(IRNode node) {
			return sourceUnit(tree().parent(node));
		}

		default SourceUnit sourceUnit(SourceUnit unit) {
			return unit;
		}
	}
}
