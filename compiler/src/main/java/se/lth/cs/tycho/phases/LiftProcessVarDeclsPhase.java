package se.lth.cs.tycho.phases;

import org.multij.Binding;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.Transformations;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.ProcessDescription;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtIf;
import se.lth.cs.tycho.ir.stmt.StmtWhile;
import se.lth.cs.tycho.ir.util.ImmutableList;

public class LiftProcessVarDeclsPhase implements Phase {
	@Override
	public String getDescription() {
		return "Lifts variable declarations in the process description to the actor scope.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return Transformations.transformEntityDecls(task, decl ->
				decl.withEntity(transform(decl.getEntity())));
	}

	private Entity transform(Entity node) {
		if (node instanceof CalActor) {
			CalActor actor = (CalActor) node;
			if (actor.getProcessDescription() != null) {
				VarDeclTransformation transformation = MultiJ.instance(VarDeclTransformation.class);
				ProcessDescription process = actor.getProcessDescription();
				ImmutableList<Statement> statements = process.getStatements().map(transformation::transform);
				ProcessDescription transformed = process.copy(statements, process.isRepeated());
				ImmutableList<LocalVarDecl> liftedVarDecls = transformation.builder().build();
				return actor
						.withProcessDescription(transformed)
						.withVarDecls(ImmutableList.concat(actor.getVarDecls(), liftedVarDecls));
			} else {
				return actor;
			}
		} else {
			return node;
		}
	}

	@Module
	interface VarDeclTransformation {
		@Binding
		default ImmutableList.Builder<LocalVarDecl> builder() {
			return ImmutableList.builder();
		}
		default Statement transform(Statement stmt) {
			return stmt;
		}
		default Statement transform(StmtBlock stmt) {
			builder().addAll(stmt.getVarDecls());
			return stmt.copy(stmt.getTypeDecls(), ImmutableList.empty(), stmt.getStatements().map(this::transform));
		}
		default Statement transform(StmtIf stmt) {
			return stmt.copy(
					stmt.getCondition(),
					stmt.getThenBranch().map(this::transform),
					stmt.getElseBranch().map(this::transform));
		}
		default Statement transform(StmtWhile stmt) {
			return stmt.copy(stmt.getCondition(), stmt.getBody().map(this::transform));
		}
	}
}
