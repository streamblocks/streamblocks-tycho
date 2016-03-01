package se.lth.cs.tycho.cfg;

import se.lth.cs.tycho.ir.expr.Expression;

import java.util.function.Consumer;

public class ConditionBlock extends Block {
	private final Expression condition;
	private Block successorIfTrue;
	private Block successorIfFalse;

	public ConditionBlock(Expression condition, Block successorIfTrue, Block successorIfFalse) {
		this.condition = condition;
		this.successorIfTrue = successorIfTrue;
		this.successorIfFalse = successorIfFalse;
	}

	@Override
	public <R, P> R accept(BlockVisitor<R, P> visitor, P p) {
		return visitor.visitConditionBlock(this, p);
	}

	public Expression getCondition() {
		return condition;
	}

	public Block getSuccessorIfTrue() {
		if (successorIfTrue != null) {
			successorIfTrue = successorIfTrue.current();
		}
		return successorIfTrue;
	}

	public Block getSuccessorIfFalse() {
		if (successorIfFalse != null) {
			successorIfFalse = successorIfFalse.current();
		}
		return successorIfFalse;
	}

	public void setSuccessorIfTrue(Block successorIfTrue) {
		if (isReplaced()) {
			throw new IllegalStateException("The node is replaced by another node.");
		}
		this.successorIfTrue = successorIfTrue == null ? null : successorIfTrue;
	}

	public void setSuccessorIfFalse(Block successorIfFalse) {
		if (isReplaced()) {
			throw new IllegalStateException("The node is replaced by another node.");
		}
		this.successorIfFalse = successorIfFalse == null ? null : successorIfFalse;
	}

	@Override
	public void forEachSuccessor(Consumer<Block> action) {
		action.accept(getSuccessorIfTrue());
		action.accept(getSuccessorIfFalse());
	}

}
