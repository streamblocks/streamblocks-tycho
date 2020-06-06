package se.lth.cs.tycho.meta.ir.expr.pattern;

import se.lth.cs.tycho.ir.expr.pattern.Pattern;
import se.lth.cs.tycho.meta.core.Meta;
import se.lth.cs.tycho.meta.core.MetaArgument;

import java.util.List;

public abstract class MetaPattern extends Pattern implements Meta {

	private final List<MetaArgument> arguments;

	public MetaPattern(List<MetaArgument> arguments) {
		super(null);
		this.arguments = arguments;
	}

	@Override
	public List<MetaArgument> getArguments() {
		return arguments;
	}
}
