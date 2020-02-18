package se.lth.cs.tycho.type;

import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Objects;

public class UserType implements Type {

	private final String name;
	private final ImmutableList<StructureType> structures;

	public UserType(String name, ImmutableList<StructureType> structures) {
		this.name = name;
		this.structures = structures;
	}

	public String getName() {
		return name;
	}

	public ImmutableList<StructureType> getStructures() {
		return structures;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserType userType = (UserType) o;
		return getName().equals(userType.getName()) &&
				getStructures().equals(userType.getStructures());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), getStructures());
	}
}
