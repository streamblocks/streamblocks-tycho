package se.lth.cs.tycho.analysis.util;

import java.util.Optional;

import javarag.Inherited;
import javarag.Module;
import javarag.NonTerminal;
import javarag.Synthesized;
import se.lth.cs.tycho.instance.Instance;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.loader.AmbiguityException;
import se.lth.cs.tycho.messages.Message;
import se.lth.cs.tycho.messages.util.Result;

public class TreeRootModule extends Module<TreeRootModule.Declarations> {

	public interface Declarations {
		@Inherited
		public TreeRoot treeRoot(IRNode root);

		@Synthesized
		@NonTerminal
		NamespaceDecl compilationUnit(TreeRoot root, NamespaceDecl ns);
		
		@Synthesized
		@NonTerminal
		Instance instanceToCompile(TreeRoot root, Instance inst);

		@Synthesized
		Result<Optional<EntityDecl>> globalEntity(TreeRoot root, QID qid, NamespaceDecl ns);

		@Synthesized
		Result<Optional<VarDecl>> globalVar(TreeRoot root, QID qid, NamespaceDecl ns);

		@Synthesized
		Result<Optional<TypeDecl>> globalType(TreeRoot root, QID qid, NamespaceDecl ns);

		@Synthesized
		IRNode mainTree(TreeRoot root);
		
		@Synthesized
		IRNode getMainTree(IRNode main, TreeRoot root);
	}

	private NamespaceDecl getCompilationUnit(Decl globalDecl, TreeRoot root) {
		return getCompilationUnit(root.getLoader().getLocation(globalDecl), root);
	}

	private NamespaceDecl getCompilationUnit(NamespaceDecl namespace, TreeRoot root) {
		NamespaceDecl location = root.getLoader().getLocation(namespace);
		if (location == null) {
			return e().compilationUnit(root, namespace);
		} else {
			return getCompilationUnit(location, root);
		}
	}
	
	public NamespaceDecl compilationUnit(TreeRoot root, NamespaceDecl unit) {
		return unit;
	}

	public Result<Optional<EntityDecl>> globalEntity(TreeRoot root, QID qid, NamespaceDecl ns) {
		try {
			EntityDecl decl = root.getLoader().loadEntity(qid, ns);
			return Result.success(Optional.ofNullable(decl));
		} catch (AmbiguityException e) {
			return Result.failure(Message.error(e.getMessage()));
		}
	}

	public Result<Optional<VarDecl>> globalVar(TreeRoot root, QID qid, NamespaceDecl ns) {
		try {
			VarDecl decl = root.getLoader().loadVar(qid, ns);
			return Result.success(Optional.ofNullable(decl));
		} catch (AmbiguityException e) {
			return Result.failure(Message.error(e.getMessage()));
		}
	}

	public Result<Optional<TypeDecl>> globalType(TreeRoot root, QID qid, NamespaceDecl ns) {
		try {
			TypeDecl decl = root.getLoader().loadType(qid, ns);
			return Result.success(Optional.ofNullable(decl));
		} catch (AmbiguityException e) {
			return Result.failure(Message.error(e.getMessage()));
		}
	}

	public TreeRoot treeRoot(TreeRoot root) {
		return root;
	}

	public IRNode mainTree(TreeRoot root) {
		return e().getMainTree(root.getMainTree(), root);
	}
	
	public IRNode getMainTree(Decl decl, TreeRoot root) {
		getCompilationUnit(decl, root);
		return decl;
	}
	
	public IRNode getMainTree(Instance inst, TreeRoot root) {
		return e().instanceToCompile(root, inst);
	}
	
	public Instance instanceToCompile(TreeRoot root, Instance inst) {
		return inst;
	}

}
