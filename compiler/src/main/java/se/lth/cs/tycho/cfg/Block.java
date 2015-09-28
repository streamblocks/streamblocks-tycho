package se.lth.cs.tycho.cfg;

import java.util.function.Consumer;

public abstract class Block {
	private Block replacement;

	abstract public <R, P> R accept(BlockVisitor<R, P> visitor, P p);

	public <R> R accept(BlockVisitor<R, Void> visitor) {
		return accept(visitor, null);
	}

	public Block current() {
		return replacement == null ? this : replacement.current();
	}

	public boolean isReplaced() {
		return replacement != null;
	}

	public void replaceWith(Block replacement) {
		Block current = current();
		replacement = replacement.current();
		if (current != replacement) {
			current.replacement = replacement;
		}
	}

	abstract public void forEachSuccessor(Consumer<Block> action);
}
