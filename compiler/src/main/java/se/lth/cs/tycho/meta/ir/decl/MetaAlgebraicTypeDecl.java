package se.lth.cs.tycho.meta.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.AlgebraicTypeDecl;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.GlobalDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.util.Lists;
import se.lth.cs.tycho.meta.core.MetaParameter;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class MetaAlgebraicTypeDecl extends MetaGlobalTypeDecl {

	private final AlgebraicTypeDecl algebraicTypeDecl;

	public MetaAlgebraicTypeDecl(List<MetaParameter> parameters, AlgebraicTypeDecl algebraicTypeDecl) {
		this(null, algebraicTypeDecl.getName(), algebraicTypeDecl.getAvailability(), parameters, algebraicTypeDecl);
	}

	public MetaAlgebraicTypeDecl(TypeDecl original, String name, Availability availability, List<MetaParameter> parameters, AlgebraicTypeDecl algebraicTypeDecl) {
		super(original, name, availability, parameters);
		this.algebraicTypeDecl = algebraicTypeDecl;
	}

	public MetaAlgebraicTypeDecl copy(List<MetaParameter> parameters, AlgebraicTypeDecl algebraicTypeDecl) {
		if (Lists.sameElements(getParameters(), parameters) && Objects.equals(getAlgebraicTypeDecl(), algebraicTypeDecl)) {
			return this;
		} else {
			return new MetaAlgebraicTypeDecl(this, algebraicTypeDecl.getName(), algebraicTypeDecl.getAvailability(), parameters, algebraicTypeDecl);
		}
	}

	public AlgebraicTypeDecl getAlgebraicTypeDecl() {
		return algebraicTypeDecl;
	}

	@Override
	public Availability getAvailability() {
		return algebraicTypeDecl.getAvailability();
	}

	@Override
	public GlobalDecl withAvailability(Availability availability) {
		return copy(getParameters(), (AlgebraicTypeDecl) getAlgebraicTypeDecl().withAvailability(availability));
	}

	@Override
	public Decl withName(String name) {
		return copy(getParameters(), (AlgebraicTypeDecl) getAlgebraicTypeDecl().withName(name));
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		getParameters().forEach(action);
		action.accept(getAlgebraicTypeDecl());
	}

	@Override
	public Decl transformChildren(Transformation transformation) {
		return copy(transformation.mapChecked(MetaParameter.class, getParameters()), transformation.applyChecked(AlgebraicTypeDecl.class, getAlgebraicTypeDecl()));
	}
}
