package net.opendf.ir.entity;

import net.opendf.ir.IRNode;
import net.opendf.ir.decl.ParDeclType;
import net.opendf.ir.decl.ParDeclValue;
import net.opendf.ir.util.ImmutableList;

public abstract class EntityDefinition extends Entity implements PortContainer {
	private final ImmutableList<PortDecl> inputPorts;
	private final ImmutableList<PortDecl> outputPorts;
	private final ImmutableList<ParDeclType> typeParameters;
	private final ImmutableList<ParDeclValue> valueParameters;
	
	public EntityDefinition(ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<ParDeclType> typeParameters, ImmutableList<ParDeclValue> valueParameters) {
		this(null, inputPorts, outputPorts, typeParameters, valueParameters);
	}
	
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
