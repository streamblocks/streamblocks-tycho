package se.lth.cs.tycho.meta.ir.entity.nl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.nl.EntityExpr;
import se.lth.cs.tycho.meta.core.Meta;
import se.lth.cs.tycho.meta.core.MetaArgument;

import java.util.List;

public abstract class MetaEntityExpr implements Meta, EntityExpr {

	private final List<MetaArgument> arguments;

	public MetaEntityExpr(List<MetaArgument> arguments) {
		this.arguments = arguments;
	}

	@Override
	public List<MetaArgument> getArguments() {
		return arguments;
	}

	@Override
	public IRNode clone() {
		return null;
	}
}
