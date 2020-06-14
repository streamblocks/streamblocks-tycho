package se.lth.cs.tycho.meta.ir.expr;

import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.meta.core.Meta;
import se.lth.cs.tycho.meta.core.MetaArgument;

import java.util.List;

public abstract class MetaExpr extends Expression implements Meta {

	private final List<MetaArgument> arguments;

	public MetaExpr(List<MetaArgument> arguments) {
		super(null);
		this.arguments = arguments;
	}

	@Override
	public List<MetaArgument> getArguments() {
		return arguments;
	}
}