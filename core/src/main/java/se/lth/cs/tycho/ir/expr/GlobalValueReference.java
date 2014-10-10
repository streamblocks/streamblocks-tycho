package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.GlobalReference;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

public class GlobalValueReference extends Expression implements GlobalReference {
	private final QID qid;
	private final boolean isNamespaceReference;

	public GlobalValueReference(QID qid, boolean isNamespaceReference) {
		this(null, qid, isNamespaceReference);
	}
	
	public GlobalValueReference(IRNode original, QID qid, boolean isNamespaceReference) {
		super(original);
		this.qid = qid;
		this.isNamespaceReference = isNamespaceReference;
	}

	@Override
	public QID getQualifiedIdentifier() {
		return qid;
	}
	
	@Override
	public boolean isNamespaceReference() {
		return isNamespaceReference;
	}

	@Override
	public <R, P> R accept(ExpressionVisitor<R, P> visitor, P param) {
		return visitor.visitGlobalValueReference(this, param);
	}

	public GlobalValueReference copy(QID qid, boolean isNamespaceReference) {
		if (this.qid.equals(qid) && this.isNamespaceReference == isNamespaceReference) {
			return this;
		} else {
			return new GlobalValueReference(this, qid, isNamespaceReference);
		}
	}

}
