package net.opendf.ir.common;

public class ExprInput extends Expression {

	@Override
	public <R,P> R accept(ExpressionVisitor<R,P> v, P p) {
		return v.visitExprInput(this, p);
	}
	
	public Port  getPort() { return port; }
	
	public int  getOffset() { return offset; }
	
	public boolean  hasRepeat() { return hasRepeat; }
	
	public int  getRepeat() { return repeat; }

	public int  getPatternLength() { return patternLength; }
	
	
	//
	//  Ctor
	//
	
	public ExprInput(Port port, int offset) {
		this (port, offset, false, -1, -1);
	}
	
	public ExprInput(Port port, int offset, int repeat, int patternLength) {
		this (port, offset, true, repeat, -1);
	}
	
	private ExprInput(Port port, int offset, boolean hasRepeat, int repeat, int patternLength) {
		this.port = port;
		this.offset = offset;
		this.hasRepeat = hasRepeat;
		this.repeat = repeat;
		this.patternLength = patternLength;
	}
	
	
	private boolean		hasRepeat;
	private Port		port;
	private int			offset;
	private int			repeat;
	private int			patternLength;
}
