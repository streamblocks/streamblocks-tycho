package se.lth.cs.tycho.ir.stmt.lvalue;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.Field;
import se.lth.cs.tycho.ir.IRNode;

/**
 * An LValue for assigning to a field of a structure.
 */
public class LValueField extends LValue {
	private LValue structure;
	private Field field;

	/**
	 * Constructs an LValueVield with a structure and a field.
	 * 
	 * @param structure
	 *            the structure
	 * @param field
	 *            the field of the structure
	 */
	public LValueField(LValue structure, Field field) {
		this(null, structure, field);
	}

	private LValueField(LValueField original, LValue structure, Field field) {
		super(original);
		this.structure = structure;
		this.field = field;
	}

	public LValueField copy(LValue structure, Field field) {
		if (Objects.equals(this.structure, structure) && Objects.equals(this.field, field)) {
			return this;
		}
		return new LValueField(this, structure, field);
	}

	/**
	 * Returns the enclosing structure, e.g. the record.
	 * 
	 * @return the structure
	 */
	public LValue getStructure() {
		return structure;
	}

	/**
	 * Returns the field.
	 * 
	 * @return the field.
	 */
	public Field getField() {
		return field;
	}

	@Override
	public <R, P> R accept(LValueVisitor<R, P> visitor, P parameter) {
		return visitor.visitLValueField(this, parameter);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(structure);
		action.accept(field);
	}

	@Override
	public LValueField transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return copy((LValue) transformation.apply(structure), (Field) transformation.apply(field));
	}
}
