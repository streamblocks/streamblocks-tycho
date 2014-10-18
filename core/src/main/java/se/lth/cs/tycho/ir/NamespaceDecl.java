package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.Import;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;

public class NamespaceDecl {
	private final QID qid;
	private final ImmutableList<NamespaceDecl> namespaceDecls;
	private final ImmutableList<Import> imports;
	private final ImmutableList<VarDecl> varDecls;
	private final ImmutableList<EntityDecl> entityDecls;
	private final ImmutableList<TypeDecl> typeDecls;

	public NamespaceDecl(QID qid, ImmutableList<NamespaceDecl> namespaceDecls, ImmutableList<Import> imports, ImmutableList<VarDecl> varDecls,
			ImmutableList<EntityDecl> entityDecls, ImmutableList<TypeDecl> typeDecls) {
		this.qid = qid;
		this.namespaceDecls = namespaceDecls;
		this.imports = imports;
		this.varDecls = varDecls;
		this.entityDecls = entityDecls;
		this.typeDecls = typeDecls;
	}

	public QID getQID() {
		return qid;
	}
	
	public ImmutableList<Decl> getAllDecls() {
		ImmutableList.Builder<Decl> builder = ImmutableList.builder();
		builder.addAll(varDecls);
		builder.addAll(entityDecls);
		builder.addAll(typeDecls);
		return builder.build();
	}

	public ImmutableList<NamespaceDecl> getNamespaceDecls() {
		return namespaceDecls;
	}
	
	public ImmutableList<Import> getImports() {
		return imports;
	}

	public ImmutableList<VarDecl> getVarDecls() {
		return varDecls;
	}

	public ImmutableList<EntityDecl> getEntityDecls() {
		return entityDecls;
	}

	public ImmutableList<TypeDecl> getTypeDecls() {
		return typeDecls;
	}

}
