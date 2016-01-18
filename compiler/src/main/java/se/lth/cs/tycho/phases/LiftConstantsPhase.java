package se.lth.cs.tycho.phases;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phases.attributes.StaticConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class LiftConstantsPhase implements Phase {
	@Override
	public String getDescription() {
		return "Moves compile-time constant variables to namespace scope.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		StaticConstants eval = context.getAttributeManager().getAttributeModule(StaticConstants.key, task);
		return task.withSourceUnits(task.getSourceUnits().map(unit -> liftInUnit(unit, eval)));
	}

	public SourceUnit liftInUnit(SourceUnit unit, StaticConstants eval) {
		Transformation transformation = MultiJ.from(Transformation.class)
				.bind("constants").to(eval)
				.instance();
		NamespaceDecl target = unit.getTree().transformChildren(transformation);
		ImmutableList<VarDecl> varDecls = ImmutableList.<VarDecl> builder()
				.addAll(target.getVarDecls())
				.addAll(transformation.lifted())
				.build();
		target = target.withVarDecls(varDecls);
		return unit.withTree(target);
	}

	@Module
	interface Transformation extends Function<IRNode, IRNode> {
		@Binding
		default List<VarDecl> lifted() {
			return new ArrayList<>();
		}

		@Binding(BindingKind.INJECTED)
		StaticConstants constants();

		@Override
		default IRNode apply(IRNode node) {
			return transform(node);
		}

		default IRNode transform(IRNode node) {
			return node.transformChildren(this);
		}

		default IRNode transform(ExprLet let) {
			return let.withVarDecls(transformVarDecls(let.getVarDecls(), lifted()::add))
					.transformChildren(this);
		}

		default IRNode transform(StmtBlock block) {
			return block.withVarDecls(transformVarDecls(block.getVarDecls(), lifted()::add))
					.transformChildren(this);
		}

		default IRNode transform(CalActor actor) {
			return actor.withVarDecls(transformVarDecls(actor.getVarDecls(), lifted()::add))
					.transformChildren(this);
		}

		default IRNode transform(Action action) {
			return action.withVarDecls(transformVarDecls(action.getVarDecls(), lifted()::add))
					.transformChildren(this);
		}

		default ImmutableList<VarDecl> transformVarDecls(ImmutableList<VarDecl> decls, Consumer<VarDecl> lifted) {
			ImmutableList.Builder<VarDecl> kept = ImmutableList.builder();
			for (VarDecl decl : decls) {
				if (constants().isConstant().test(decl)) {
					lifted.accept(decl);
				} else {
					kept.add((VarDecl) transform(decl));
				}
			}
			return kept.build();
		}
	}
}
