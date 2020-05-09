package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ProductTypeDecl extends AlgebraicTypeDecl {

	private List<FieldDecl> fields;

	public List<FieldDecl> getFields() {
		return fields;
	}

	public ProductTypeDecl(String name, Availability availability, List<FieldDecl> fields) {
		this(null, name, availability, fields);
	}

	public ProductTypeDecl(TypeDecl original, String name, Availability availability, List<FieldDecl> fields) {
		super(original, name, availability);
		this.fields = fields;
	}

	@Override
	public Decl withName(String name) {
		return copy(name, getAvailability(), getFields());
	}

	@Override
	public GlobalDecl withAvailability(Availability availability) {
		return copy(getName(), availability, getFields());
	}

	private ProductTypeDecl copy(String name, Availability availability, List<FieldDecl> fields) {
		if (Objects.equals(getName(), name) && Objects.equals(getAvailability(), availability) && Lists.sameElements(getFields(), fields)) {
			return this;
		} else {
			return new ProductTypeDecl(this, name, availability, fields);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		getFields().forEach(action);
	}

	@Override
	public Decl transformChildren(Transformation transformation) {
		return copy(getName(), getAvailability(), transformation.mapChecked(FieldDecl.class, getFields()));
	}
}
