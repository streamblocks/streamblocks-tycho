package se.lth.cs.tycho.ir.entity;

import se.lth.cs.tycho.ir.GlobalReference;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

public class GlobalEntityReference extends Entity implements GlobalReference {
	private final QID qid;
	private final boolean isNamespaceReference;

	public GlobalEntityReference(QID qid, boolean isNamespaceReference) {
		this(null, qid, isNamespaceReference);
	}
	
	public GlobalEntityReference(IRNode original, QID qid, boolean isNamespaceReference) {
		super(original);
		this.qid = qid;
		this.isNamespaceReference = isNamespaceReference;
	}

	@Override
	public boolean isNamespaceReference() {
		return isNamespaceReference;
	}
	
	@Override
	public QID getQualifiedIdentifier() {
		return qid;
	}

	@Override
	public <R, P> R accept(EntityVisitor<R, P> visitor, P param) {
		return visitor.visitGlobalEntityReference(this, param);
	}

	public GlobalEntityReference copy(QID qid, boolean isNamespaceReference) {
		if (this.qid.equals(qid) && this.isNamespaceReference == isNamespaceReference) {
			return this;
		} else {
			return new GlobalEntityReference(this, qid, isNamespaceReference);
		}
	}
}
