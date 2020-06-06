package se.lth.cs.tycho.meta.ir.entity;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.ParameterTypeDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.meta.core.MetaParameter;

import java.util.List;

public abstract class MetaEntity extends Entity {

	private final List<MetaParameter> parameters;

	public MetaEntity(IRNode original, List<PortDecl> inputPorts, List<PortDecl> outputPorts, List<ParameterTypeDecl> typeParameters, List<ParameterVarDecl> valueParameters, List<MetaParameter> parameters) {
		super(original, inputPorts, outputPorts, typeParameters, valueParameters);
		this.parameters = parameters;
	}

	public List<MetaParameter> getParameters() {
		return parameters;
	}
}
