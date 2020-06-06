package se.lth.cs.tycho.meta.ir.decl;

import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.meta.core.MetaParameter;

import java.util.List;

public abstract class MetaGlobalTypeDecl extends GlobalTypeDecl {

	private final List<MetaParameter> parameters;

	public MetaGlobalTypeDecl(TypeDecl original, String name, Availability availability, List<MetaParameter> parameters) {
		super(original, name, availability);
		this.parameters = parameters;
	}

	public List<MetaParameter> getParameters() {
		return parameters;
	}
}
