package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ProductTypeDecl extends AlgebraicTypeDecl {

	private List<FieldDecl> fields;

	public List<FieldDecl> getFields() {
		return fields;
	}

	public ProductTypeDecl(String name, Availability availability, List<ParameterTypeDecl> typeParameters, List<ParameterVarDecl> valueParameters, List<FieldDecl> fields) {
		this(null, name, availability, typeParameters, valueParameters, fields);
	}

	public ProductTypeDecl(TypeDecl original, String name, Availability availability, List<ParameterTypeDecl> typeParameters, List<ParameterVarDecl> valueParameters, List<FieldDecl> fields) {
		super(original, name, availability, typeParameters, valueParameters);
		this.fields = fields;
	}

	@Override
	public Decl withName(String name) {
		return copy(name, getAvailability(), getTypeParameters(), getValueParameters(), getFields());
	}

	@Override
	public AlgebraicTypeDecl withTypeParameters(List<ParameterTypeDecl> typeParameters) {
		return copy(getName(), getAvailability(), typeParameters, getValueParameters(), getFields());
	}

	@Override
	public AlgebraicTypeDecl withValueParameters(List<ParameterVarDecl> valueParameters) {
		return copy(getName(), getAvailability(), getTypeParameters(), valueParameters, getFields());
	}

	@Override
	public GlobalDecl withAvailability(Availability availability) {
		return copy(getName(), availability, getTypeParameters(), getValueParameters(), getFields());
	}

	private ProductTypeDecl copy(String name, Availability availability, List<ParameterTypeDecl> typeParameters, List<ParameterVarDecl> valueParameters, List<FieldDecl> fields) {
		if (Objects.equals(getName(), name) && Objects.equals(getAvailability(), availability) && Lists.sameElements(getTypeParameters(), typeParameters) && Lists.sameElements(getValueParameters(), valueParameters) && Lists.sameElements(getFields(), fields)) {
			return this;
		} else {
			return new ProductTypeDecl(this, name, availability, typeParameters, valueParameters, fields);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		getFields().forEach(action);
	}

	@Override
	public Decl transformChildren(Transformation transformation) {
		return copy(getName(), getAvailability(), transformation.mapChecked(ParameterTypeDecl.class, getTypeParameters()), transformation.mapChecked(ParameterVarDecl.class, getValueParameters()), transformation.mapChecked(FieldDecl.class, getFields()));
	}
}
