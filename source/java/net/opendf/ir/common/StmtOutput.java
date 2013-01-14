package net.opendf.ir.common;

public class StmtOutput extends Statement {

	@Override
	public <R,P> R accept(StatementVisitor<R,P> v, P p) {
		return v.visitStmtOutput(this, p);
	}

	public PortName  getPort() { return port; }
	
	public boolean  hasRepeat() { return hasRepeat; }
	
	public int  getRepeat() { return repeat; }
	
	public Expression[]  getValues() { return values; }


	//
	//  Ctor
	//
	
	public StmtOutput(Expression[] values, PortName port) {
		this(values, port, false, 0);
	}
	
	public StmtOutput(Expression[] values, PortName port, int repeat) {
		this(values, port, true, repeat);
	}
	
	private StmtOutput(Expression[] values, PortName port, boolean hasRepeat, int repeat) {
		this.values = values;
		this.port = port;
		this.hasRepeat = hasRepeat;
		this.repeat = repeat;
	}
	
	private Expression[]	values;
	
	private boolean			hasRepeat;
	private PortName		port;
	private int				repeat;
}
