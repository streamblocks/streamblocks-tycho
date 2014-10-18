package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

public class Import extends AbstractIRNode {
	private final DeclKind kind;
	private final QID qid;
	private final boolean namespaceImport;
	private final String alias;

	private Import(IRNode original, DeclKind kind, QID qid, boolean namespaceImport, String alias) {
		super(original);
		this.kind = kind;
		this.qid = qid;
		this.namespaceImport = namespaceImport;
		this.alias = alias;
	}

	public static Import singleImport(DeclKind kind, QID qid, String alias) {
		return new Import(null, kind, qid, false, alias);
	}

	public static Import namespaceImport(DeclKind kind, QID qid) {
		return new Import(null, kind, qid, true, null);
	}

	public DeclKind getKind() {
		return kind;
	}

	public QID getQID() {
		return qid;
	}

	public boolean isNamespaceImport() {
		return namespaceImport;
	}

	public String getAlias() {
		return alias;
	}

}
