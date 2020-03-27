package se.lth.cs.tycho.ir.expr.pattern;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.type.TypeExpr;

import java.util.Objects;
import java.util.function.Consumer;

public class PatternWildcard extends Pattern {

	private TypeExpr type;

	public PatternWildcard(IRNode original) {
		this(original, new NominalTypeExpr("<transient>"));
	}

	public PatternWildcard(IRNode original, TypeExpr type) {
		super(original);
		this.type = type;
	}

	public TypeExpr getType() {
		return type;
	}

	public PatternWildcard copy(TypeExpr type) {
		if (Objects.equals(getType(), type)) {
			return this;
		} else {
			return new PatternWildcard(this, type);
		}
	}

	public PatternWildcard withType(TypeExpr type) {
		return copy(type);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(type);
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return this;
	}
}
