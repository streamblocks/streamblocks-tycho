package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.StarImport;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class NamespaceDecl extends AbstractIRNode {
	private final QID qid;
	private final ImmutableList<StarImport> starImports;
	private final ImmutableList<VarDecl> varDecls;
	private final ImmutableList<EntityDecl> entityDecls;
	private final ImmutableList<TypeDecl> typeDecls;

	public NamespaceDecl(QID qid, List<StarImport> starImports, List<VarDecl> varDecls,
			List<EntityDecl> entityDecls, List<TypeDecl> typeDecls) {
		this(null, qid, starImports, varDecls, entityDecls, typeDecls);
	}
	
	private NamespaceDecl(IRNode original, QID qid, List<StarImport> starImports, List<VarDecl> varDecls,
			List<EntityDecl> entityDecls, List<TypeDecl> typeDecls) {
		super(original);
		this.qid = qid;
		this.starImports = ImmutableList.from(starImports);
		this.varDecls = ImmutableList.from(varDecls);
		this.entityDecls = ImmutableList.from(entityDecls);
		this.typeDecls = ImmutableList.from(typeDecls);
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

	public NamespaceDecl withStarImports(List<StarImport> starImports) {
		if (Lists.sameElements(this.starImports, starImports)) {
			return this;
		} else {
			return new NamespaceDecl(this, qid, ImmutableList.from(starImports), varDecls, entityDecls, typeDecls);
		}
	}

	public ImmutableList<VarDecl> getVarDecls() {
		return varDecls;
	}

	public NamespaceDecl withVarDecls(List<VarDecl> varDecls) {
		if (Lists.sameElements(this.varDecls, varDecls)) {
			return this;
		} else {
			return new NamespaceDecl(this, qid, starImports, ImmutableList.from(varDecls), entityDecls, typeDecls);
		}
	}

	public ImmutableList<EntityDecl> getEntityDecls() {
		return entityDecls;
	}

	public NamespaceDecl withEntityDecls(List<EntityDecl> entityDecls) {
		if (Lists.sameElements(this.entityDecls, entityDecls)) {
			return this;
		} else {
			return new NamespaceDecl(this, qid, starImports, varDecls, ImmutableList.from(entityDecls), typeDecls);
		}
	}

	public ImmutableList<TypeDecl> getTypeDecls() {
		return typeDecls;
	}

	public NamespaceDecl withTypeDecls(List<TypeDecl> typeDecls) {
		if (Lists.sameElements(this.typeDecls, typeDecls)) {
			return this;
		} else {
			return new NamespaceDecl(this, qid, starImports, varDecls, entityDecls, ImmutableList.from(typeDecls));
		}
	}


	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		varDecls.forEach(action);
		typeDecls.forEach(action);
		entityDecls.forEach(action);
		starImports.forEach(action);
	}

	@Override
	@SuppressWarnings("unchecked")
	public NamespaceDecl transformChildren(Transformation transformation) {
		return new NamespaceDecl(this, qid,
				(ImmutableList) starImports.map(transformation),
				(ImmutableList) varDecls.map(transformation),
				(ImmutableList) entityDecls.map(transformation),
				(ImmutableList) typeDecls.map(transformation));
	}

}
