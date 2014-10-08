package se.lth.cs.tycho.ir.entity;

import se.lth.cs.tycho.ir.GlobalReference;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

public class GlobalEntityReference extends Entity implements GlobalReference {
	private final QID qid;
	private final boolean isContentReference;

	public GlobalEntityReference(QID qid, boolean isContentReference) {
		this(null, qid, isContentReference);
	}
	
	public GlobalEntityReference(IRNode original, QID qid, boolean isContentReference) {
		super(original);
		this.qid = qid;
		this.isContentReference = isContentReference;
	}

	@Override
	public boolean isContentReference() {
		return isContentReference;
	}
	
	@Override
	public QID getQualifiedIdentifier() {
		return qid;
	}

	@Override
	public <R, P> R accept(EntityVisitor<R, P> visitor, P param) {
		return visitor.visitGlobalEntityReference(this, param);
	}

	public GlobalEntityReference copy(QID qid, boolean isContentReference) {
		if (this.qid.equals(qid) && this.isContentReference == isContentReference) {
			return this;
		} else {
			return new GlobalEntityReference(this, qid, isContentReference);
		}
	}
}
