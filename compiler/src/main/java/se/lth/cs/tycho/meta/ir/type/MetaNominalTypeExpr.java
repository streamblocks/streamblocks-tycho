package se.lth.cs.tycho.meta.ir.type;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.util.Lists;
import se.lth.cs.tycho.meta.core.MetaArgument;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class MetaNominalTypeExpr extends MetaTypeExpr {

	private final NominalTypeExpr nominalTypeExpr;

	public MetaNominalTypeExpr(List<MetaArgument> arguments, NominalTypeExpr nominalTypeExpr) {
		super(arguments);
		this.nominalTypeExpr = nominalTypeExpr;
	}

	public MetaNominalTypeExpr copy(List<MetaArgument> arguments, NominalTypeExpr nominalTypeExpr) {
		if (Lists.sameElements(getArguments(), arguments) && Objects.equals(getNominalTypeExpr(), nominalTypeExpr)) {
			return this;
		} else {
			return new MetaNominalTypeExpr(arguments, nominalTypeExpr);
		}
	}

	public MetaNominalTypeExpr withArguments(List<MetaArgument> arguments) {
		return copy(arguments, getNominalTypeExpr());
	}

	public NominalTypeExpr getNominalTypeExpr() {
		return nominalTypeExpr;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		getArguments().forEach(action);
		action.accept(getNominalTypeExpr());
	}

	@Override
	public MetaTypeExpr transformChildren(Transformation transformation) {
		return copy(transformation.mapChecked(MetaArgument.class, getArguments()), transformation.applyChecked(NominalTypeExpr.class, getNominalTypeExpr()));
	}
}
