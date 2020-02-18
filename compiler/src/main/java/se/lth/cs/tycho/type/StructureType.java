package se.lth.cs.tycho.type;

import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Objects;

public class StructureType {

	private final String name;
	private final ImmutableList<FieldType> fields;

	public static class FieldType {

		private final String name;
		private final Type type;

		public FieldType(String name, Type type) {
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public Type getType() {
			return type;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			FieldType fieldType = (FieldType) o;
			return getName().equals(fieldType.getName()) &&
					getType().equals(fieldType.getType());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getName(), getType());
		}
	}

	public StructureType(String name, ImmutableList<FieldType> fields) {
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
		StructureType that = (StructureType) o;
		return Objects.equals(getName(), that.getName()) &&
				getFields().equals(that.getFields());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), getFields());
	}
}
