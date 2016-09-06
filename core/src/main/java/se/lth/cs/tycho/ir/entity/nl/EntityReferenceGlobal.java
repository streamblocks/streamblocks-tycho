package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

import java.util.Objects;
import java.util.function.Consumer;

public class EntityReferenceGlobal extends AbstractIRNode implements EntityReference {
	private final QID globalName;

	private EntityReferenceGlobal(IRNode original, QID globalName) {
		super(original);
		this.globalName = globalName;
	}

	public EntityReferenceGlobal(QID globalName) {
		this(null, globalName);
	}

	public QID getGlobalName() {
		return globalName;
	}

	public EntityReferenceGlobal withGlobalName(QID globalName) {
		if (Objects.equals(this.globalName, globalName)) {
			return this;
		} else {
			return new EntityReferenceGlobal(this, globalName);
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
