package se.lth.cs.tycho.meta.ir.decl;

import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.meta.core.MetaParameter;

import java.util.List;

public class MetaGlobalEntityDecl extends GlobalEntityDecl {

	private final List<MetaParameter> parameters;

	public MetaGlobalEntityDecl(List<MetaParameter> parameters, GlobalEntityDecl original) {
		super(original, original.getAvailability(), original.getName(), original.getEntity(), original.getExternal());
		this.parameters = parameters;
	}

	public List<MetaParameter> getParameters() {
		return parameters;
	}
}
