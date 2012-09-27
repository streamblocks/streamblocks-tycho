package net.opendf.ir.common;

public class ExprInput extends Expression {

	@Override
	public <R,P> R accept(ExpressionVisitor<R,P> v, P p) {
		return v.visitExprInput(this, p);
	}
	
	public PortName  getPort() { return port; }
	
	public int  getOffset() { return offset; }
	
	public boolean  hasRepeat() { return hasRepeat; }
	
	public int  getRepeat() { return repeat; }

	public int  getPatternLength() { return patternLength; }
	
	
	//
	//  Ctor
	//
	
	public ExprInput(PortName port, int offset) {
		this (port, offset, false, -1); 
	}
	
	public ExprInput(PortName port, int offset, int repeat) {
		this (port, offset, true, repeat);
	}
	
	private ExprInput(PortName port, int offset, boolean hasRepeat, int repeat, int patternLength) {
		this.port = port;
		this.offset = offset;
		this.hasRepeat = hasRepeat;
		this.repeat = repeat;
		this.patternLength = patternLength;
	}
	
	
	private boolean		hasRepeat;
	private PortName	port;
	private int			offset;
	private int			repeat;
	private int			patternLength;
}
