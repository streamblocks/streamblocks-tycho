package se.lth.cs.tycho.ir.entity;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

import java.util.Objects;
import java.util.function.Consumer;

public class GlobalEntityReference extends AbstractIRNode {
	private final QID globalName;

	private GlobalEntityReference(IRNode original, QID globalName) {
		super(original);
		this.globalName = globalName;
	}

	public GlobalEntityReference(QID globalName) {
		this(null, globalName);
	}

	public QID getGlobalName() {
		return globalName;
	}

	public GlobalEntityReference withGlobalName(QID globalName) {
		if (Objects.equals(this.globalName, globalName)) {
			return this;
		} else {
			return new GlobalEntityReference(this, globalName);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
	}

	@Override
	public GlobalEntityReference transformChildren(Transformation transformation) {
		return this;
	}
}
