package se.lth.cs.tycho.ir.entity;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;

public abstract class EntityDefinition extends Entity implements PortContainer {
	private final ImmutableList<PortDecl> inputPorts;
	private final ImmutableList<PortDecl> outputPorts;
	private final ImmutableList<TypeDecl> typeParameters;
	private final ImmutableList<VarDecl> valueParameters;
	
	public EntityDefinition(IRNode original, ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<TypeDecl> typeParameters, ImmutableList<VarDecl> valueParameters) {
		super(original);
		this.inputPorts = inputPorts;
		this.outputPorts = outputPorts;
		this.typeParameters = typeParameters;
		this.valueParameters = valueParameters;
	}

	public ImmutableList<PortDecl> getInputPorts() {
		return inputPorts;
	}

	public ImmutableList<PortDecl> getOutputPorts() {
		return outputPorts;
	}

	public ImmutableList<TypeDecl> getTypeParameters() {
		return typeParameters;
	}

	public ImmutableList<VarDecl> getValueParameters() {
		return valueParameters;
	}
	
}
