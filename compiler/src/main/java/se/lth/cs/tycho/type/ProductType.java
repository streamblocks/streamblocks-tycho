package se.lth.cs.tycho.type;

import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Objects;

public class ProductType extends AlgebraicType {

	private final ImmutableList<FieldType> fields;

	public ProductType(String name, ImmutableList<FieldType> fields) {
		super(name);
		this.fields = fields;
	}

	public ImmutableList<FieldType> getFields() {
		return fields;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		ProductType that = (ProductType) o;
		return getFields().equals(that.getFields());
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getFields());
	}
}
