package net.opendf.ir;

import net.opendf.ir.decl.GlobalDecl;
import net.opendf.ir.util.ImmutableList;


public class NamespaceDecl {
	private final QID qid;
	private final ImmutableList<NamespaceDecl> namespaceDecls;
	private final ImmutableList<GlobalDecl> decls;

	public NamespaceDecl(QID qid, ImmutableList<NamespaceDecl> namespaceDecls, ImmutableList<GlobalDecl> decls) {
		this.qid = qid;
		this.namespaceDecls = namespaceDecls;
		this.decls = decls;
	}

	public QID getQID() {
		return qid;
	}

	public ImmutableList<NamespaceDecl> getNamespaceDecls() {
		return namespaceDecls;
	}

	public ImmutableList<GlobalDecl> getDecls() {
		return decls;
	}
}
