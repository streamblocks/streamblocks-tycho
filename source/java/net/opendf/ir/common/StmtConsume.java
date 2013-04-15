package net.opendf.ir.common;

public class StmtConsume extends Statement {
	private Port port;
	private int tokens;

	@Override
	public <R, P> R accept(StatementVisitor<R, P> v, P p) {
		return v.visitStmtConsume(this, p);
	}

	public Port getPort() {
		return port;
	}

	public int getNumberOfTokens() {
		return tokens;
	}

	public StmtConsume(Port port, int tokens) {
		this.port = port;
		this.tokens = tokens;
	}
}
