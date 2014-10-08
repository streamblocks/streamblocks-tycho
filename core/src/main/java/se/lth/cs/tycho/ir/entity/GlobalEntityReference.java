package se.lth.cs.tycho.ir.entity;

import se.lth.cs.tycho.ir.GlobalReference;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

public class GlobalEntityReference extends Entity implements GlobalReference {
	private final QID qid;

	public GlobalEntityReference(IRNode original, QID qid) {
		super(original);
		this.qid = qid;
	}

	@Override
	public QID getQualifiedIdentifier() {
		return qid;
	}

	@Override
	public <R, P> R accept(EntityVisitor<R, P> visitor, P param) {
		return visitor.visitGlobalEntityReference(this, param);
	}

	public GlobalEntityReference copy(QID qid) {
		if (this.qid.equals(qid)) {
			return this;
		} else {
			return new GlobalEntityReference(this, qid);
		}
	}
}
