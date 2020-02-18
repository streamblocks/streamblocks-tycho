package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class GlobalTypeDecl extends TypeDecl implements GlobalDecl {

	private Availability availability;
	private List<StructureDecl> structures;

	public GlobalTypeDecl(Availability availability, String name, List<StructureDecl> structures) {
		this(null, availability, name, structures);
	}

	protected GlobalTypeDecl(TypeDecl original, Availability availability, String name, List<StructureDecl> structures) {
		super(original, name);
		this.availability = availability;
		this.structures = ImmutableList.from(structures);
	}

	public List<StructureDecl> getStructures() {
		return structures;
	}

	public GlobalTypeDecl withStructures(List<StructureDecl> structures) {
		return copy(getAvailability(), getName(), structures);
	}

	@Override
	public GlobalTypeDecl withName(String name) {
		return copy(getAvailability(), name, getStructures());
	}

	public Availability getAvailability() {
		return availability;
	}

	@Override
	public GlobalTypeDecl withAvailability(Availability availability) {
		return copy(availability, getName(), getStructures());
	}

	private GlobalTypeDecl copy(Availability availability, String name, List<StructureDecl> structures) {
		if (availability == getAvailability() && Objects.equals(name, getName()) && Lists.sameElements(structures, getStructures())) {
			return this;
		} else {
			return new GlobalTypeDecl(this, availability, name, structures);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		structures.forEach(action);
	}

	@Override
	public GlobalTypeDecl transformChildren(Transformation transformation) {
		return copy(
				getAvailability(),
				getName(),
				getStructures() == null ? null : transformation.mapChecked(StructureDecl.class, getStructures()));
	}
}
