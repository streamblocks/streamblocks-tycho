package se.lth.cs.tycho.ir.expr;

import java.util.function.Consumer;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;

public class ExprInput extends Expression {

	@Override
	public <R, P> R accept(ExpressionVisitor<R, P> v, P p) {
		return v.visitExprInput(this, p);
	}

	public Port getPort() {
		return port;
	}

	public int getOffset() {
		return offset;
	}

	public boolean hasRepeat() {
		return hasRepeat;
	}

	public int getRepeat() {
		return repeat;
	}

	public int getPatternLength() {
		return patternLength;
	}

	//
	// Ctor
	//

	/**
	 * Create a port with hasRepeat==false
	 */
	public ExprInput(Port port, int offset) {
		this(null, port, offset, false, -1, -1);
	}

	/**
	 * Create a port with hasRepeat==true
	 */
	public ExprInput(Port port, int offset, int repeat, int patternLength) {
		this(null, port, offset, true, repeat, patternLength);
	}

	private ExprInput(ExprInput original, Port port, int offset, boolean hasRepeat, int repeat, int patternLength) {
		super(original);
		this.port = port;
		this.offset = offset;
		this.hasRepeat = hasRepeat;
		this.repeat = repeat;
		this.patternLength = patternLength;
	}

	/**
	 * Create a port with hasRepeat==false
	 */
	public ExprInput copy(Port port, int offset) {
		if (!hasRepeat() && this.port == port && this.offset == offset) {
			return this;
		}
		return new ExprInput(this, port, offset, false, -1, -1);
	}

	/**
	 * Create a port with hasRepeat==true
	 */
	public ExprInput copy(Port port, int offset, int repeat, int patternLength) {
		if (hasRepeat() && this.port == port && this.offset == offset && this.repeat == repeat
				&& this.patternLength == patternLength) {
			return this;
		}
		return new ExprInput(this, port, offset, true, repeat, patternLength);
	}

	private boolean hasRepeat;
	private Port port;
	private int offset;
	private int repeat;
	private int patternLength;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(port);
	}

	@Override
	public ExprInput transformChildren(Transformation transformation) {
		if (hasRepeat) {
			return copy(
					(Port) transformation.apply(port),
					offset,
					repeat,
					patternLength
			);
		} else {
			return copy((Port) transformation.apply(port), offset);
		}
	}
}
