package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.StarImport;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.function.Consumer;
import java.util.function.Function;

public class NamespaceDecl extends AbstractIRNode {
	private final QID qid;
	private final ImmutableList<StarImport> starImports;
	private final ImmutableList<VarDecl> varDecls;
	private final ImmutableList<EntityDecl> entityDecls;
	private final ImmutableList<TypeDecl> typeDecls;

	public NamespaceDecl(QID qid, ImmutableList<StarImport> starImports, ImmutableList<VarDecl> varDecls,
			ImmutableList<EntityDecl> entityDecls, ImmutableList<TypeDecl> typeDecls) {
		this(null, qid, starImports, varDecls, entityDecls, typeDecls);
	}
	
	private NamespaceDecl(IRNode original, QID qid, ImmutableList<StarImport> starImports, ImmutableList<VarDecl> varDecls,
			ImmutableList<EntityDecl> entityDecls, ImmutableList<TypeDecl> typeDecls) {
		super(original);
		this.qid = qid;
		this.starImports = starImports;
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

	public ImmutableList<StarImport> getStarImports() {
		return starImports;
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

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		varDecls.forEach(action);
		typeDecls.forEach(action);
		entityDecls.forEach(action);
		starImports.forEach(action);
	}

	@Override
	public NamespaceDecl transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return new NamespaceDecl(this, qid,
				(ImmutableList) starImports.map(transformation),
				(ImmutableList) varDecls.map(transformation),
				(ImmutableList) entityDecls.map(transformation),
				(ImmutableList) typeDecls.map(transformation));
	}

}
