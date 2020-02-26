package se.lth.cs.tycho.type;

import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Objects;

public class SumType extends AlgebraicType {

	public static class VariantType {

		private final String name;
		private final ImmutableList<FieldType> fields;

		public VariantType(String name, ImmutableList<FieldType> fields) {
			this.name = name;
			this.fields = fields;
		}

		public String getName() {
			return name;
		}

		public ImmutableList<FieldType> getFields() {
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

	private final ImmutableList<VariantType> variants;

	public SumType(String name, ImmutableList<VariantType> variants) {
		super(name);
		this.variants = variants;
	}

	public ImmutableList<VariantType> getVariants() {
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
		return Objects.hash(super.hashCode(), getVariants());
	}
}
