package se.lth.cs.tycho.phases;

import se.lth.cs.multij.Module;
import se.lth.cs.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.util.ImmutableList;

public class RemoveNamespacesPhase implements Phase {
	private final RemoveImports removeImports = MultiJ.instance(RemoveImports.class);

	@Override
	public String getDescription() {
		return "Moves all declarations to the root namespace.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		ImmutableList.Builder<VarDecl> varDecls = ImmutableList.builder();
		ImmutableList.Builder<TypeDecl> typeDecls = ImmutableList.builder();
		ImmutableList.Builder<EntityDecl> entityDecls = ImmutableList.builder();
		task.getSourceUnits().forEach(sourceUnit -> {
			varDecls.addAll(sourceUnit.getTree().getVarDecls());
			typeDecls.addAll(sourceUnit.getTree().getTypeDecls());
			entityDecls.addAll(sourceUnit.getTree().getEntityDecls());
		});
		CompilationTask result = task.copy(ImmutableList.empty(), task.getIdentifier().getLast(), new NamespaceDecl(
				QID.empty(), ImmutableList.empty(), varDecls.build(), entityDecls.build(), typeDecls.build()));
		return (CompilationTask) removeImports.transform(result);
	}

	@Module
	interface RemoveImports {
		default IRNode transform(IRNode node) {
			return removeImports(node.transformChildren(this::transform));
		}

		default IRNode removeImports(IRNode node) {
			return node;
		}

		default <D extends Decl> ImmutableList<D> removeImportsFromList(ImmutableList<D> decls) {
			ImmutableList<D> result = decls.stream().filter(d -> !d.isImport()).collect(ImmutableList.collector());
			if (result.size() == decls.size()) {
				return decls;
			} else {
				return result;
			}
		}

		default IRNode removeImports(NamespaceDecl ns) {
			return ns.withEntityDecls(removeImportsFromList(ns.getEntityDecls()))
					.withTypeDecls(removeImportsFromList(ns.getTypeDecls()))
					.withVarDecls(removeImportsFromList(ns.getVarDecls()));
		}

		default IRNode removeImports(ExprLet let) {
			return let.copy(
					removeImportsFromList(let.getTypeDecls()),
					removeImportsFromList(let.getVarDecls()),
					let.getBody());
		}

		default IRNode removeImports(StmtBlock block) {
			return block.copy(
					removeImportsFromList(block.getTypeDecls()),
					removeImportsFromList(block.getVarDecls()),
					block.getStatements());
		}

		default IRNode removeImports(CalActor actor) {
			return actor
					.withVarDecls(removeImportsFromList(actor.getVarDecls()));
					//.withTypeDecls(removeImportsFromList(actor.getTypeDecls())); // TODO implement
		}

		default IRNode removeImports(NlNetwork network) {
			return network
					.withVarDecls(removeImportsFromList(network.getVarDecls()));
					//.withTypeDecls(removeImportsFromList(network.getTypeDecls())); // TODO implement
		}
	}
}
