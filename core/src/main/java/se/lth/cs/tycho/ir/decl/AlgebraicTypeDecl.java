package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;

public abstract class AlgebraicTypeDecl extends GlobalTypeDecl {

	private ImmutableList<ParameterTypeDecl> typeParameters;
	private ImmutableList<ParameterVarDecl> valueParameters;

	public AlgebraicTypeDecl(TypeDecl original, String name, Availability availability, List<ParameterTypeDecl> typeParameters, List<ParameterVarDecl> valueParameters) {
		super(original, name, availability);
		this.typeParameters = ImmutableList.from(typeParameters);
		this.valueParameters = ImmutableList.from(valueParameters);
	}

	public ImmutableList<ParameterTypeDecl> getTypeParameters() {
		return typeParameters;
	}

	public ImmutableList<ParameterVarDecl> getValueParameters() {
		return valueParameters;
	}

	public abstract AlgebraicTypeDecl withTypeParameters(List<ParameterTypeDecl> typeParameters);

	public abstract AlgebraicTypeDecl withValueParameters(List<ParameterVarDecl> valueParameters);
}
