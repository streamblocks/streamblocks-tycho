package net.opendf.ir.common;

public class ExprInput extends Expression {

	@Override
	public void accept(ExpressionVisitor v) {
		v.visitExprInput(this);
	}
	
	public PortName  getPort() { return port; }
	
	public int  getOffset() { return offset; }
	
	public boolean  hasRepeat() { return hasRepeat; }
	
	public int  getRepeat() { return repeat; }
	
	
	//
	//  Ctor
	//
	
	public ExprInput(PortName port, int offset) {
		this (port, offset, false, -1); 
	}
	
	public ExprInput(PortName port, int offset, int repeat) {
		this (port, offset, true, repeat);
	}
	
	private ExprInput(PortName port, int offset, boolean hasRepeat, int repeat) {
		this.port = port;
		this.offset = offset;
		this.hasRepeat = hasRepeat;
		this.repeat = repeat;
	}
	
	
	private boolean		hasRepeat;
	private PortName	port;
	private int			offset;
	private int			repeat;
}
