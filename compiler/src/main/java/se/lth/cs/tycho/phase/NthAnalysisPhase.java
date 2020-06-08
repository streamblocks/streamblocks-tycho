package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.ExprNth;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueNth;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;
import se.lth.cs.tycho.type.TupleType;
import se.lth.cs.tycho.type.Type;

public class NthAnalysisPhase implements Phase {

	@Override
	public String getDescription() {
		return "Analyzes accessing of tuple nth element.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
		Analysis analysis = MultiJ.from(Analysis.class)
				.bind("reporter").to(context.getReporter())
				.bind("tree").to(task.getModule(TreeShadow.key))
				.bind("types").to(task.getModule(Types.key))
				.instance();
		analysis.analyse(task);
		return task;
	}

	@Module
	interface Analysis {

		@Binding(BindingKind.INJECTED)
		Reporter reporter();

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(BindingKind.INJECTED)
		Types types();

		default void analyse(IRNode node) {
			check(node);
			node.forEachChild(this::analyse);
		}

		default void check(IRNode node) {

		}

		default void check(ExprNth expr) {
			TupleType type = (TupleType) types().type(expr.getStructure());
			int n = expr.getNth().getNumber();
			if (n < 1 || n > type.getTypes().size()) {
				error(expr, type, expr.getNth().getNumber());
			}
		}

		default void check(LValueNth lvalue) {
			TupleType type = (TupleType) types().type(lvalue.getStructure());
			int n = lvalue.getNth().getNumber();
			if (n < 1 || n > type.getTypes().size()) {
				error(lvalue, type, lvalue.getNth().getNumber());
			}
		}

		default void error(IRNode node, Type type, Integer number) {
			reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Value #" + number + " is not a member of " + type + ".", sourceUnit(node), node));
		}

		default SourceUnit sourceUnit(IRNode node) {
			return sourceUnit(tree().parent(node));
		}

		default SourceUnit sourceUnit(SourceUnit unit) {
			return unit;
		}
	}
}
