package se.lth.cs.tycho.ir;

import java.util.function.Consumer;
import java.util.function.Function;


public interface IRNode extends Cloneable {

	void forEachChild(Consumer<? super IRNode> action);

	IRNode transformChildren(Function<? super IRNode, ? extends IRNode> transformation);

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
		IRNode copy = transformChildren(IRNode::deepClone);
		if (this == copy) {
			return clone();
		} else {
			return copy;
		}
	}

}
