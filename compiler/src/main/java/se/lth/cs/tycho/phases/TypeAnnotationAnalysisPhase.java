package se.lth.cs.tycho.phases;

import org.multij.Binding;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.decoration.Tree;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeAnnotationAnalysisPhase implements Phase {
	@Override
	public String getDescription() {
		return "Analyzes type annotations.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		TypeAnnotationChecker checker = MultiJ.from(TypeAnnotationChecker.class)
				.bind("reporter").to(context.getReporter())
				.instance();
		Tree.of(task).walk().forEach(checker);
		return task;
	}

	@Module
	interface TypeAnnotationChecker extends Consumer<Tree<?>> {
		@Binding
		Reporter reporter();

		@Override
		default void accept(Tree<?> node) {
			check(node, node.node());
		}

		default SourceUnit unit(Tree<?> tree) {
			return tree.findParentOfType(SourceUnit.class).get().node();
		}

		default void check(Tree<?> tree, IRNode node) {}

		default <P extends Parameter<?, P>> void checkDuplicateNames(Stream<Tree<P>> parameters) {
			Stream<List<Tree<P>>> duplicates = parameters.collect(Collectors.groupingBy(par -> par.node().getName()))
					.values().stream()
					.filter(list -> list.size() > 1);

			duplicates.forEach(duplicate -> {
				Iterator<Tree<P>> iter = duplicate.iterator();
				Tree<P> first = iter.next();
				while (iter.hasNext()) {
					Tree<P> def = iter.next();
					String message = "Parameter " + def.node().getName() + " is already defined.";
					reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, message, unit(def), def.node()));
				}
				String message = "Parameter " + first.node().getName() + " is defined here.";
				reporter().report(new Diagnostic(Diagnostic.Kind.INFO, message, unit(first), first.node()));
			});
		}

		default void check(Tree<?> tree, NominalTypeExpr typeExpr) {
			Tree<NominalTypeExpr> type = tree.assertNode(typeExpr);
			checkDuplicateNames(type.children(NominalTypeExpr::getTypeParameters));
			checkDuplicateNames(type.children(NominalTypeExpr::getValueParameters));
			switch (typeExpr.getName()) {
				case "List": {

					break;
				}
				case "int":
				case "uint": {

					break;
				}
				case "bool": {

					break;
				}
				case "float": {

					break;
				}
			}
		}
	}
}
