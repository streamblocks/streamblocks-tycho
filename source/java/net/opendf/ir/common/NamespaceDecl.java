package net.opendf.ir.common;

import net.opendf.ir.common.decl.Decl;
import net.opendf.ir.util.ImmutableList;


public class NamespaceDecl {
	private final QID qid;
	private final ImmutableList<NamespaceDecl> namespaceDecls;
	private final ImmutableList<Decl> decls;

	public NamespaceDecl(QID qid, ImmutableList<NamespaceDecl> namespaceDecls, ImmutableList<Decl> decls) {
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

	public ImmutableList<Decl> getDecls() {
		return decls;
	}
}
