package se.lth.cs.tycho.transformation.proc2cal;

import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;
import java.util.function.Consumer;

public class ActionBlock extends Block {
	private final ImmutableList<Statement> statements;
	private Block successor;

	public ActionBlock(List<Statement> statements, Block successor) {
		this.statements = ImmutableList.from(statements);
		this.successor = successor;
	}

	@Override
	public <R, P> R accept(BlockVisitor<R, P> visitor, P p) {
		return visitor.visitActionBlock(this, p);
	}

	public ImmutableList<Statement> getStatements() {
		return statements;
	}

	public Block getSuccessor() {
		if (successor != null) {
			successor = successor.current();
		}
		return successor;
	}

	public void setSuccessor(Block successor) {
		if (isReplaced()) {
			throw new IllegalStateException("The node is replaced by another node.");
		}
		this.successor = successor == null ? null : successor.current();
	}

	@Override
	public void forEachSuccessor(Consumer<Block> action) {
		action.accept(getSuccessor());
	}
}
