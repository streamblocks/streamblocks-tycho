package se.lth.cs.tycho.ir.entity;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.ParDeclType;
import se.lth.cs.tycho.ir.decl.ParDeclValue;
import se.lth.cs.tycho.ir.util.ImmutableList;

public abstract class EntityDefinition extends Entity implements PortContainer {
	private final ImmutableList<PortDecl> inputPorts;
	private final ImmutableList<PortDecl> outputPorts;
	private final ImmutableList<ParDeclType> typeParameters;
	private final ImmutableList<ParDeclValue> valueParameters;
	
	public EntityDefinition(IRNode original, ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<ParDeclType> typeParameters, ImmutableList<ParDeclValue> valueParameters) {
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

	public ImmutableList<ParDeclType> getTypeParameters() {
		return typeParameters;
	}

	public ImmutableList<ParDeclValue> getValueParameters() {
		return valueParameters;
	}
	
}
