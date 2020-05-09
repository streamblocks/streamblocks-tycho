package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.AliasTypeDecl;
import se.lth.cs.tycho.ir.decl.FieldDecl;
import se.lth.cs.tycho.ir.decl.ProductTypeDecl;
import se.lth.cs.tycho.ir.decl.SumTypeDecl;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.util.Objects;
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

		default void checkDeclaration(ProductTypeDecl decl) {
			decl.getFields()
					.stream()
					.collect(Collectors.groupingBy(FieldDecl::getName))
					.forEach((name, fields) -> {
						if (fields.size() > 1) {
							reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Field " + name + " is already declared.", sourceUnit(fields.get(1)), fields.get(1)));
						}
					});
		}

		default void checkDeclaration(SumTypeDecl decl) {
			decl.getVariants()
					.stream()
					.collect(Collectors.groupingBy(SumTypeDecl.VariantDecl::getName))
					.forEach((name, fields) -> {
						if (fields.size() > 1) {
							reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Variant " + name + " is already declared.", sourceUnit(fields.get(1)), fields.get(1)));
						}
					});
		}

		default void checkDeclaration(SumTypeDecl.VariantDecl decl) {
			decl.getFields()
					.stream()
					.collect(Collectors.groupingBy(FieldDecl::getName))
					.forEach((name, fields) -> {
						if (fields.size() > 1) {
							reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Field " + name + " is already declared.", sourceUnit(fields.get(1)), fields.get(1)));
						}
					});
		}

		default void checkDeclaration(AliasTypeDecl decl) {
			if (Objects.equals(decl.getName(), ((NominalTypeExpr) decl.getType()).getName())) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Type " + decl.getName() + " is already declared.", sourceUnit(decl), decl));
			}
		}

		default SourceUnit sourceUnit(IRNode node) {
			return sourceUnit(tree().parent(node));
		}

		default SourceUnit sourceUnit(SourceUnit unit) {
			return unit;
		}
	}
}
