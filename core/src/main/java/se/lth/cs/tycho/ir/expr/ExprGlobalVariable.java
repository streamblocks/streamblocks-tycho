package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

import java.util.Objects;
import java.util.function.Consumer;

public class ExprGlobalVariable extends Expression {
	private final QID globalName;

	public ExprGlobalVariable(QID globalName) {
		this(null, globalName);
	}

	private ExprGlobalVariable(IRNode original, QID globalName) {
		super(original);
		this.globalName = globalName;
	}

	public QID getGlobalName() {
		return globalName;
	}

	public ExprGlobalVariable withGlobalName(QID globalName) {
		if (Objects.equals(this.globalName, globalName)) {
			return this;
		} else {
			return new ExprGlobalVariable(this, globalName);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
	}

	@Override
	public ExprGlobalVariable transformChildren(Transformation transformation) {
		return this;
	}
}
