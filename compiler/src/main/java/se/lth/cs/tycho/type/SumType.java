package se.lth.cs.tycho.type;

import java.util.List;
import java.util.Objects;

public class SumType extends AlgebraicType {

	public static class VariantType {

		private final String name;
		private final List<FieldType> fields;

		public VariantType(String name, List<FieldType> fields) {
			this.name = name;
			this.fields = fields;
		}

		public String getName() {
			return name;
		}

		public List<FieldType> getFields() {
			return fields;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			VariantType that = (VariantType) o;
			return getName().equals(that.getName()) &&
					getFields().equals(that.getFields());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getName(), getFields());
		}
	}

	private final List<VariantType> variants;

	public SumType(String name, List<VariantType> variants) {
		super(name);
		this.variants = variants;
	}

	public List<VariantType> getVariants() {
		return variants;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		SumType sumType = (SumType) o;
		return getVariants().equals(sumType.getVariants());
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
