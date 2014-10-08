package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.GlobalReference;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

public class GlobalValueReference extends Expression implements GlobalReference {
	private final QID qid;
	private final boolean isContentReference;

	public GlobalValueReference(QID qid, boolean isContentReference) {
		this(null, qid, isContentReference);
	}
	
	public GlobalValueReference(IRNode original, QID qid, boolean isContentReference) {
		super(original);
		this.qid = qid;
		this.isContentReference = isContentReference;
	}

	@Override
	public QID getQualifiedIdentifier() {
		return qid;
	}
	
	@Override
	public boolean isContentReference() {
		return isContentReference;
	}

	@Override
	public <R, P> R accept(ExpressionVisitor<R, P> visitor, P param) {
		return visitor.visitGlobalValueReference(this, param);
	}

	public GlobalValueReference copy(QID qid, boolean isContentReference) {
		if (this.qid.equals(qid) && this.isContentReference == isContentReference) {
			return this;
		} else {
			return new GlobalValueReference(this, qid, isContentReference);
		}
	}

}
