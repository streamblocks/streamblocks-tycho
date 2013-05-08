package net.opendf.ir.common;

import java.util.Objects;

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

	public ExprInput(Port port, int offset) {
		this(null, port, offset, false, -1, -1);
	}

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

	public ExprInput copy(Port port, int offset) {
		if (!hasRepeat() && Objects.equals(this.port, port) && this.offset == offset) {
			return this;
		}
		return new ExprInput(this, port, offset, false, -1, -1);
	}

	public ExprInput copy(Port port, int offset, int repeat, int patternLength) {
		if (hasRepeat() && Objects.equals(this.port, port) && this.offset == offset && this.repeat == repeat
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
}
