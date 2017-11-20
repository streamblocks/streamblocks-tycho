package se.lth.cs.tycho.phases;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.phases.attributes.AttributeManager;
import se.lth.cs.tycho.phases.attributes.VariableDeclarations;

import java.util.Optional;

public class ResolveGlobalVariableNamesPhase implements Phase {
	@Override
	public String getDescription() {
		return "Resolves local names of global variables to global names";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		Transformation transformation = MultiJ.from(Transformation.class)
				.bind("task").to(task)
				.bind("attributes").to(context.getAttributeManager())
				.instance();
		return transformation.transform(task);
	}

	@Module
	interface Transformation {
		@Binding(BindingKind.INJECTED)
		CompilationTask task();

		@Binding(BindingKind.INJECTED)
		AttributeManager attributes();

		@Binding(BindingKind.LAZY)
		default VariableDeclarations variables() {
			return attributes().getAttributeModule(VariableDeclarations.key, task());
		}

		@Binding(BindingKind.LAZY)
		default TreeShadow tree() {
			return attributes().getAttributeModule(TreeShadow.key, task());
		}

		default IRNode transform(IRNode node) {
			return node.transformChildren(this::transform);
		}

		default CompilationTask transform(CompilationTask task) {
			return task.transformChildren(this::transform);
		}

		default Expression transform(ExprVariable var) {
			VarDecl decl = variables().declaration(var);
			Optional<QID> globalName = globalName(decl);
			return globalName.<Expression>map(ExprGlobalVariable::new).orElse(var);
		}

		default Optional<QID> globalName(VarDecl decl) {
			return Optional.empty();
		}

		default Optional<QID> globalName(GlobalVarDecl decl) {
			NamespaceDecl ns = (NamespaceDecl) tree().parent(decl);
			return Optional.of(ns.getQID().concat(QID.of(decl.getName())));
		}
	}
}
