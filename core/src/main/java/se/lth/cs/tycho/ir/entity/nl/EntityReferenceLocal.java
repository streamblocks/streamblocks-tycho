package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;

import java.util.Objects;
import java.util.function.Consumer;

public class EntityReferenceLocal extends AbstractIRNode implements EntityReference {
	private final String name;

	private EntityReferenceLocal(IRNode original, String name) {
		super(original);
		this.name = name;
	}

	public EntityReferenceLocal(String name) {
		this(null, name);
	}

	public String getName() {
		return name;
	}

	public EntityReferenceLocal withName(String name) {
		if (Objects.equals(this.name, name)) {
			return this;
		} else {
			return new EntityReferenceLocal(this, name);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {

	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return this;
	}
}
