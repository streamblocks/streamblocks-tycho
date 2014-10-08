package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.GlobalReference;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

public class GlobalValueReference extends Expression implements GlobalReference {
	private final QID qid;

	public GlobalValueReference(QID qid) {
		this(null, qid);
	}
	
	public GlobalValueReference(IRNode original, QID qid) {
		super(original);
		this.qid = qid;
	}

	@Override
	public QID getQualifiedIdentifier() {
		return qid;
	}

	@Override
	public <R, P> R accept(ExpressionVisitor<R, P> visitor, P param) {
		return visitor.visitGlobalValueReference(this, param);
	}

	public GlobalValueReference copy(QID qid) {
		if (this.qid.equals(qid)) {
			return this;
		} else {
			return new GlobalValueReference(this, qid);
		}
	}

}
