package se.lth.cs.tycho.ir.entity.cal;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class Priority extends AbstractIRNode {
	private final QID high;
	private final QID low;

	public Priority(QID high, QID low) {
		this(null, high, low);
	}

	private Priority(Priority original, QID high, QID low) {
		super(original);
		this.high = high;
		this.low = low;
	}

	public Priority copy(QID high, QID low) {
		if (Objects.equals(this.high, high) && Objects.equals(this.low, low)) {
			return this;
		} else {
			return new Priority(this, high, low);
		}
	}

	public QID getHigh() {
		return high;
	}

	public QID getLow() {
		return low;
	}

	public Priority withHigh(QID high) {
		if (Objects.equals(this.high, high)) {
			return this;
		} else {
			return new Priority(this, high, low);
		}
	}

	public Priority withLow(QID low) {
		if (Objects.equals(this.low, low)) {
			return this;
		} else {
			return new Priority(this, high, low);
		}
	}

	@Override
	public Priority transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return this;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
	}
}
