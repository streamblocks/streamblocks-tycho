package net.opendf.ir.common;

public class StmtConsume extends Statement {
	private PortName port;
	private int tokens;

	@Override
	public <R, P> R accept(StatementVisitor<R, P> v, P p) {
		return v.visitStmtConsume(this, p);
	}

	public PortName getPort() {
		return port;
	}

	public int getNumberOfTokens() {
		return tokens;
	}

	public StmtConsume(PortName port, int tokens) {
		this.port = port;
		this.tokens = tokens;
	}
}
