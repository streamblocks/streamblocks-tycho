package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.TupleTypeExpr;
import se.lth.cs.tycho.phase.TreeShadow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface TupleAnnotations {

	ModuleKey<TupleAnnotations> key = task -> MultiJ.from(TupleAnnotations.Implementation.class)
			.bind("tree").to(task.getModule(TreeShadow.key))
			.instance();

	List<TupleTypeExpr> annotations();

	@Module
	interface Implementation extends TupleAnnotations {

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(BindingKind.LAZY)
		default List<TupleTypeExpr> annotations() {
			Collector collector = MultiJ.instance(Collector.class);
			collector.accept(tree().root());
			return collector.annotations();
		}

		@Module
		interface Collector extends Consumer<IRNode> {

			@Binding(BindingKind.LAZY)
			default List<TupleTypeExpr> annotations() {
				return new ArrayList<>();
			}

			@Override
			default void accept(IRNode node) {
				node.forEachChild(this);
			}

			default void accept(TupleTypeExpr expr) {
				annotations().add(expr);
				expr.forEachChild(this);
			}
		}

	}

}
