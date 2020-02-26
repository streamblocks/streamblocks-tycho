package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.util.stream.Collectors;

public class TypeDeclarationAnalysisPhase implements Phase {

	@Override
	public String getDescription() {
		return "Type declaration analysis";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		TypeDeclarationChecker checker = MultiJ.from(TypeDeclarationChecker.class)
				.bind("tree").to(task.getModule(TreeShadow.key))
				.bind("reporter").to(context.getReporter())
				.instance();
		checker.check(task);
		return task;
	}
	@Module
	interface TypeDeclarationChecker {

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(BindingKind.INJECTED)
		Reporter reporter();

		default void check(IRNode node) {
			checkDeclaration(node);
			node.forEachChild(this::check);
		}

		default void checkDeclaration(IRNode node) {}

		default void checkDeclaration(GlobalTypeDecl decl) {
			decl.getRecords()
					.stream()
					.filter(record -> record.getName() != null)
					.collect(Collectors.groupingBy(RecordDecl::getName))
					.forEach((name, records) -> {
						if (records.size() > 1) {
							reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Record " + name + " is already declared.", sourceUnit(records.get(1)), records.get(1)));
						}
					});
		}

		default void checkDeclaration(RecordDecl decl) {
			decl.getFields()
					.stream()
					.collect(Collectors.groupingBy(FieldVarDecl::getName))
					.forEach((name, fields) -> {
						if (fields.size() > 1) {
							reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Field " + name + " is already declared.", sourceUnit(fields.get(1)), fields.get(1)));
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
