package se.lth.cs.tycho.ir.stmt.lvalue;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Nth;

import java.util.Objects;
import java.util.function.Consumer;

public class LValueNth extends LValue {

	private final LValue structure;
	private final Nth nth;

	public LValueNth(LValue structure, Nth nth) {
		this(null, structure, nth);
	}

	public LValueNth(IRNode original, LValue structure, Nth nth) {
		super(original);
		this.structure = structure;
		this.nth = nth;
	}

	public LValueNth copy(LValue structure, Nth nth) {
		if (Objects.equals(getStructure(), structure) && Objects.equals(getNth(), nth)) {
			return this;
		} else {
			return new LValueNth(this, structure, nth);
		}
	}

	public LValue getStructure() {
		return structure;
	}

	public Nth getNth() {
		return nth;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getStructure());
		action.accept(getNth());
	}

	@Override
	public LValue transformChildren(Transformation transformation) {
		return copy(transformation.applyChecked(LValue.class, getStructure()), transformation.applyChecked(Nth.class, getNth()));
	}
}
