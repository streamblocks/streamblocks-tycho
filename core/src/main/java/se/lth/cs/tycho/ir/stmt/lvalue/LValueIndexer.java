package se.lth.cs.tycho.ir.stmt.lvalue;

import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;

/**
 * An LValue for assigning to an index referenced element of a structure. If
 * structure is a list, then index could be an integer expression.
 */
public class LValueIndexer extends LValue {
	private LValue structure;
	private Expression index;

	/**
	 * Constructs an LValueIndexer with a structure and an index.
	 * 
	 * @param structure
	 *            the structure
	 * @param index
	 *            the location in the structure
	 */
	public LValueIndexer(LValue structure, Expression index) {
		this(null, structure, index);
	}

	private LValueIndexer(LValueIndexer original, LValue structure, Expression index) {
		super(original);
		this.structure = structure;
		this.index = index;
	}

	public LValueIndexer copy(LValue structure, Expression index) {
		if (Objects.equals(this.structure, structure) && Objects.equals(this.index, index)) {
			return this;
		}
		return new LValueIndexer(this, structure, index);
	}

	/**
	 * Returns the enclosing structure, e.g. the list.
	 * 
	 * @return the structure
	 */
	public LValue getStructure() {
		return structure;
	}

	/**
	 * Returns the index expression, i.e. the location in the structure.
	 * 
	 * @return the index
	 */
	public Expression getIndex() {
		return index;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(structure);
		action.accept(index);
	}

	@Override
	public LValueIndexer transformChildren(Transformation transformation) {
		return copy((LValue) transformation.apply(structure), (Expression) transformation.apply(index));
	}
}
