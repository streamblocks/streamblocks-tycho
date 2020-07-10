package se.lth.cs.tycho.meta.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.util.Lists;
import se.lth.cs.tycho.meta.core.MetaParameter;

import java.util.List;
import java.util.function.Consumer;

public class MetaGlobalEntityDecl extends GlobalEntityDecl {

	private final List<MetaParameter> parameters;

	public MetaGlobalEntityDecl(List<MetaParameter> parameters, GlobalEntityDecl original) {
		super(original, original.getAvailability(), original.getName(), original.getEntity(), original.getExternal());
		this.parameters = parameters;
	}

	public MetaGlobalEntityDecl copy(List<MetaParameter> parameters) {
		if (Lists.sameElements(getParameters(), parameters)) {
			return this;
		} else {
			return new MetaGlobalEntityDecl(parameters, this);
		}
	}

	public List<MetaParameter> getParameters() {
		return parameters;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		super.forEachChild(action);
		getParameters().forEach(action);
	}

	@Override
	public GlobalEntityDecl transformChildren(Transformation transformation) {
		return copy(transformation.mapChecked(MetaParameter.class, getParameters()));
	}
}
