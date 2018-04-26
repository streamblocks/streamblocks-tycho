package se.lth.cs.tycho.ir.stmt;

import java.util.function.Consumer;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;

public class StmtConsume extends Statement {
	private Port port;
	private int tokens;

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
		if (this.port == port && this.tokens == tokens) {
			return this;
		}
		return new StmtConsume(this, port, tokens);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(port);
	}

	@Override
	public StmtConsume transformChildren(Transformation transformation) {
		return copy((Port) transformation.apply(port), tokens);
	}
}
