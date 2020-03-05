package se.lth.cs.tycho.type;

import java.util.List;
import java.util.Objects;

public class ProductType extends AlgebraicType {

	private final List<FieldType> fields;

	public ProductType(String name, List<FieldType> fields) {
		super(name);
		this.fields = fields;
	}

	public List<FieldType> getFields() {
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
		return super.hashCode();
	}
}
