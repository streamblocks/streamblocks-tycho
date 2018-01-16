package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.ir.decl.Import;
import se.lth.cs.tycho.ir.module.ModuleDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class NamespaceDecl extends AbstractIRNode {
	private final QID qid;
	private final ImmutableList<Import> imports;
	private final ImmutableList<GlobalVarDecl> varDecls;
	private final ImmutableList<GlobalEntityDecl> entityDecls;
	private final ImmutableList<GlobalTypeDecl> typeDecls;
	private final ImmutableList<ModuleDecl> moduleDecls;

	public NamespaceDecl(QID qid, List<Import> imports, List<GlobalVarDecl> varDecls,
						 List<GlobalEntityDecl> entityDecls, List<GlobalTypeDecl> typeDecls, List<ModuleDecl> moduleDecls) {
		this(null, qid, imports, varDecls, entityDecls, typeDecls, moduleDecls);
	}
	
	private NamespaceDecl(IRNode original, QID qid, List<Import> imports, List<GlobalVarDecl> varDecls,
						  List<GlobalEntityDecl> entityDecls, List<GlobalTypeDecl> typeDecls, List<ModuleDecl> moduleDecls) {
		super(original);
		this.qid = qid;
		this.imports = ImmutableList.from(imports);
		this.varDecls = ImmutableList.from(varDecls);
		this.entityDecls = ImmutableList.from(entityDecls);
		this.typeDecls = ImmutableList.from(typeDecls);
		this.moduleDecls = ImmutableList.from(moduleDecls);
	}
	private NamespaceDecl copy(QID qid, List<Import> imports, List<GlobalVarDecl> varDecls,
						 List<GlobalEntityDecl> entityDecls, List<GlobalTypeDecl> typeDecls, List<ModuleDecl> moduleDecls) {
		if (Objects.equals(this.qid, qid) && Lists.sameElements(this.imports, imports) && Lists.sameElements(this.varDecls, varDecls) && Lists.sameElements(this.entityDecls, entityDecls) && Lists.sameElements(this.typeDecls, typeDecls) && Lists.sameElements(this.moduleDecls, moduleDecls)) {
			return this;
		} else {
			return new NamespaceDecl(this, qid, imports, varDecls, entityDecls, typeDecls, moduleDecls);
		}
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

	public ImmutableList<Import> getImports() {
		return imports;
	}

	public NamespaceDecl withImports(List<Import> imports) {
		if (Lists.sameElements(this.imports, imports)) {
			return this;
		} else {
			return new NamespaceDecl(this, qid, ImmutableList.from(imports), varDecls, entityDecls, typeDecls, moduleDecls);
		}
	}

	public ImmutableList<GlobalVarDecl> getVarDecls() {
		return varDecls;
	}

	public NamespaceDecl withVarDecls(List<GlobalVarDecl> varDecls) {
		if (Lists.sameElements(this.varDecls, varDecls)) {
			return this;
		} else {
			return new NamespaceDecl(this, qid, imports, ImmutableList.from(varDecls), entityDecls, typeDecls, moduleDecls);
		}
	}

	public ImmutableList<GlobalEntityDecl> getEntityDecls() {
		return entityDecls;
	}

	public NamespaceDecl withEntityDecls(List<GlobalEntityDecl> entityDecls) {
		if (Lists.sameElements(this.entityDecls, entityDecls)) {
			return this;
		} else {
			return new NamespaceDecl(this, qid, imports, varDecls, ImmutableList.from(entityDecls), typeDecls, moduleDecls);
		}
	}

	public ImmutableList<GlobalTypeDecl> getTypeDecls() {
		return typeDecls;
	}

	public NamespaceDecl withTypeDecls(List<GlobalTypeDecl> typeDecls) {
		if (Lists.sameElements(this.typeDecls, typeDecls)) {
			return this;
		} else {
			return new NamespaceDecl(this, qid, imports, varDecls, entityDecls, ImmutableList.from(typeDecls), moduleDecls);
		}
	}

	public ImmutableList<ModuleDecl> getModuleDecls() {
		return moduleDecls;
	}

	public NamespaceDecl withModuleDecls(List<ModuleDecl> moduleDecls) {
		if (Lists.sameElements(this.moduleDecls, moduleDecls)) {
			return this;
		} else {
			return new NamespaceDecl(this, qid, imports, varDecls, entityDecls, typeDecls, ImmutableList.from(moduleDecls));
		}
	}


	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		varDecls.forEach(action);
		typeDecls.forEach(action);
		entityDecls.forEach(action);
		moduleDecls.forEach(action);
		imports.forEach(action);
	}

	@Override
	@SuppressWarnings("unchecked")
	public NamespaceDecl transformChildren(Transformation transformation) {
		return copy(qid,
				transformation.mapChecked(Import.class, imports),
				transformation.mapChecked(GlobalVarDecl.class, varDecls),
				transformation.mapChecked(GlobalEntityDecl.class, entityDecls),
				transformation.mapChecked(GlobalTypeDecl.class, typeDecls),
				transformation.mapChecked(ModuleDecl.class, moduleDecls));
	}

}
