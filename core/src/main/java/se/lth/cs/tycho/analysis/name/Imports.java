package se.lth.cs.tycho.analysis.name;

import java.util.Optional;

import javarag.Module;
import javarag.Synthesized;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.Import;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.loader.AmbiguityException;
import se.lth.cs.tycho.loader.DeclarationLoader;
import se.lth.cs.tycho.messages.Message;
import se.lth.cs.tycho.messages.util.Result;

public class Imports extends Module<Imports.Decls> {
	public interface Decls extends DeclarationLoaderInterface, NamespaceDeclInterface {
		@Synthesized
		Result<Optional<VarDecl>> importVar(Import imp, String name);

		@Synthesized
		Result<Optional<TypeDecl>> importType(Import imp, String name);

		@Synthesized
		Result<Optional<EntityDecl>> importEntity(Import imp, String name);
	}

	public Result<Optional<VarDecl>> importVar(Import imp, String name) {
		return importWith(DeclarationLoader::loadVar, imp, name);
	}

	public Result<Optional<TypeDecl>> importType(Import imp, String name) {
		return importWith(DeclarationLoader::loadType, imp, name);
	}

	public Result<Optional<EntityDecl>> importEntity(Import imp, String name) {
		return importWith(DeclarationLoader::loadEntity, imp, name);
	}

	private <T> Result<Optional<T>> importWith(Loader<T> load, Import imp, String name) {
		QID qid;
		if (imp.isNamespaceImport()) {
			qid = imp.getQID().concat(QID.of(name));
		} else if (imp.getName().equals(name)) {
			qid = imp.getQID();
		} else {
			return Result.success(Optional.empty());
		}
		DeclarationLoader loader = e().declarationLoader(imp);
		NamespaceDecl ns = e().enclosingNamespaceDecl(imp);
		try {
			T decl = load.load(loader, qid, ns);
			return Result.success(Optional.ofNullable(decl));
		} catch (AmbiguityException e) {
			return Result.failure(Message.error(e.getMessage()));
		}
	}

	private interface Loader<T> {
		T load(DeclarationLoader loader, QID qid, NamespaceDecl ns) throws AmbiguityException;
	}
}
