package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.function.Consumer;
import java.util.function.Function;


public interface IRNode {

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

}
