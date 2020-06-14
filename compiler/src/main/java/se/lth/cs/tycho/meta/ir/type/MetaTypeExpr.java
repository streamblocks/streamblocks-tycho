package se.lth.cs.tycho.meta.ir.type;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.meta.core.Meta;
import se.lth.cs.tycho.meta.core.MetaArgument;

import java.util.List;

public abstract class MetaTypeExpr extends AbstractIRNode implements Meta, TypeExpr<MetaTypeExpr> {

	private final List<MetaArgument> arguments;

	public MetaTypeExpr(List<MetaArgument> arguments) {
		super(null);
		this.arguments = arguments;
	}

	@Override
	public List<MetaArgument> getArguments() {
		return arguments;
	}
}
