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

	public NamespaceDecl withStarImports(List<StarImport> starImports) {
		if (Lists.elementIdentityEquals(this.starImports, starImports)) {
			return this;
		} else {
			return new NamespaceDecl(this, qid, ImmutableList.from(starImports), varDecls, entityDecls, typeDecls);
		}
	}

	public ImmutableList<VarDecl> getVarDecls() {
		return varDecls;
	}

	public NamespaceDecl withVarDecls(List<VarDecl> varDecls) {
		if (Lists.elementIdentityEquals(this.varDecls, varDecls)) {
			return this;
		} else {
			return new NamespaceDecl(this, qid, starImports, ImmutableList.from(varDecls), entityDecls, typeDecls);
		}
	}

	public ImmutableList<EntityDecl> getEntityDecls() {
		return entityDecls;
	}

	public NamespaceDecl withEntityDecls(List<EntityDecl> entityDecls) {
		if (Lists.elementIdentityEquals(this.entityDecls, entityDecls)) {
			return this;
		} else {
			return new NamespaceDecl(this, qid, starImports, varDecls, ImmutableList.from(entityDecls), typeDecls);
		}
	}

	public ImmutableList<TypeDecl> getTypeDecls() {
		return typeDecls;
	}

	public NamespaceDecl withTypeDecls(List<TypeDecl> typeDecls) {
		if (Lists.elementIdentityEquals(this.typeDecls, typeDecls)) {
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
	public NamespaceDecl transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return new NamespaceDecl(this, qid,
				(ImmutableList) starImports.map(transformation),
				(ImmutableList) varDecls.map(transformation),
				(ImmutableList) entityDecls.map(transformation),
				(ImmutableList) typeDecls.map(transformation));
	}

}
