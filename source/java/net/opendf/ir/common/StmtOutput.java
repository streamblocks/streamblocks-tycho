package net.opendf.ir.common;

public class StmtOutput extends Statement {

	@Override
	public void accept(StatementVisitor v) {
		v.visitStmtOutput(this);
	}

	public PortName  getPort() { return port; }
	
	public int  getOffset() { return offset; }
	
	public boolean  hasRepeat() { return hasRepeat; }
	
	public int  getRepeat() { return repeat; }
	

	//
	//  Ctor
	//
	
	public StmtOutput(Expression value, PortName port, int offset) {
		this(value, port, offset, false, 0);
	}
	
	public StmtOutput(Expression value, PortName port, int offset, int repeat) {
		this(value, port, offset, true, repeat);
	}
	
	private StmtOutput(Expression value, PortName port, int offset, boolean hasRepeat, int repeat) {
		this.value = value;
		this.port = port;
		this.offset = offset;
		this.hasRepeat = hasRepeat;
		this.repeat = repeat;
	}
	
	private Expression	value;
	
	private boolean		hasRepeat;
	private PortName	port;
	private int			offset;
	private int			repeat;	
}
