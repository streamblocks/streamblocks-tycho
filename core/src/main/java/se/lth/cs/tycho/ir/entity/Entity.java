package se.lth.cs.tycho.ir.entity;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;

public abstract class Entity extends AbstractIRNode {

	protected final ImmutableList<PortDecl> inputPorts;
	protected final ImmutableList<PortDecl> outputPorts;
	protected final ImmutableList<TypeDecl> typeParameters;
	protected final ImmutableList<VarDecl> valueParameters;

	public Entity(IRNode original, ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<TypeDecl> typeParameters, ImmutableList<VarDecl> valueParameters) {
		super(original);
		this.inputPorts = inputPorts;
		this.outputPorts = outputPorts;
		this.typeParameters = typeParameters;
		this.valueParameters = valueParameters;
	}

	public abstract <R, P> R accept(EntityVisitor<R, P> visitor, P param);

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
