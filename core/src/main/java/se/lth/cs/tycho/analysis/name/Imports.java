package se.lth.cs.tycho.analysis.name;

import java.util.Optional;

import javarag.Module;
import javarag.Synthesized;
import se.lth.cs.tycho.analysis.util.TreeRoot;
import se.lth.cs.tycho.analysis.util.TreeRootModule;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.StarImport;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.messages.util.Result;

public class Imports extends Module<Imports.Attributes> {

	public interface Attributes extends Decls, TreeRootModule.Declarations, NamespaceDecls.Declarations {}
	
	public interface Decls {
		@Synthesized
		Result<Optional<VarDecl>> importVar(StarImport imp, String name);

		@Synthesized
		Result<Optional<TypeDecl>> importType(StarImport imp, String name);

		@Synthesized
		Result<Optional<EntityDecl>> importEntity(StarImport imp, String name);
	}

	public Result<Optional<VarDecl>> importVar(StarImport imp, String name) {
		return importWith(e()::globalVar, imp, name);
	}

	public Result<Optional<TypeDecl>> importType(StarImport imp, String name) {
		return importWith(e()::globalType, imp, name);
	}

	public Result<Optional<EntityDecl>> importEntity(StarImport imp, String name) {
		return importWith(e()::globalEntity, imp, name);
	}

	private <T> Result<Optional<T>> importWith(Loading<T> load, StarImport imp, String name) {
		QID qid;
		// FIXME ugly hack!
		try {
			QID last = QID.of(name);
			qid = imp.getQID().concat(last);
		} catch (IllegalArgumentException e) {
			return Result.success(Optional.empty());
		}
		NamespaceDecl ns = e().enclosingNamespaceDecl(imp);
		return load.load(e().treeRoot(imp), qid, ns);
	}

	private interface Loading<T> {
		Result<Optional<T>> load(TreeRoot root, QID qid, NamespaceDecl ns);
	}
}
