package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;


public interface IRNode extends Cloneable {

	void forEachChild(Consumer<? super IRNode> action);

	IRNode transformChildren(Transformation transformation);

	default int getFromLineNumber() {
		return 0;
	}

	default int getFromColumnNumber() {
		return 0;
	}

	default int getToLineNumber() {
		return 0;
	}

	default int getToColumnNumber() {
		return 0;
	}

	default boolean hasPosition() {
		return getFromLineNumber() > 0 && getFromColumnNumber() > 0 && getToLineNumber() > 0 && getToColumnNumber() > 0;
	}

	IRNode clone();

	default IRNode deepClone() {
		return clone().transformChildren(IRNode::deepClone);
	}

	@FunctionalInterface
	interface Transformation extends Function<IRNode, IRNode> {
		IRNode apply(IRNode node);
		default <T extends IRNode> T applyChecked(Class<T> type, T node) {
			IRNode result = apply(node);
			return type.cast(result);
		}
		default <T extends IRNode> List<T> mapChecked(Class<T> type, List<T> nodes) {
			return nodes.stream()
					.map(this)
					.map(type::cast)
					.collect(ImmutableList.collector());
		}
	}
}


