package net.opendf.ir.stmt;

import java.util.Objects;

import net.opendf.ir.Port;

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
		this(null, port, tokens);
	}

	private StmtConsume(StmtConsume original, Port port, int tokens) {
		super(original);
		this.port = port;
		this.tokens = tokens;
	}

	public StmtConsume copy(Port port, int tokens) {
		if (Objects.equals(this.port, port) && this.tokens == tokens) {
			return this;
		}
		return new StmtConsume(this, port, tokens);
	}
}
