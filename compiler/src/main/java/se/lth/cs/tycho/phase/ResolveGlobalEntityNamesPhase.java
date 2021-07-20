package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.nl.EntityReference;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceGlobal;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceLocal;
import se.lth.cs.tycho.attribute.EntityDeclarations;
import se.lth.cs.tycho.attribute.GlobalNames;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;

import java.util.NoSuchElementException;
import java.util.Optional;

public class ResolveGlobalEntityNamesPhase implements Phase {
	@Override
	public String getDescription() {
		return "Resolves local entity names to global names.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
        Transformation transformation = MultiJ.from(Transformation.class)
				.bind("entities").to(task.getModule(EntityDeclarations.key))
				.bind("globalNames").to(task.getModule(GlobalNames.key))
				.instance();
        CompilationTask transformed_task = null;
        try {
        	transformed_task = task.transformChildren(transformation);
		} catch (NoSuchElementException e) {
        	context.getReporter().report(
        			new Diagnostic(Diagnostic.Kind.ERROR, "Could not resolve global entity names!\n" +
							e.getMessage())
			);
		}

		return transformed_task;
	}

	@Module
	interface Transformation extends IRNode.Transformation {
		@Binding(BindingKind.INJECTED)
		EntityDeclarations entities();

		@Binding(BindingKind.INJECTED)
		GlobalNames globalNames();

		@Override
		default IRNode apply(IRNode node) {
			return node.transformChildren(this);
		}

		default EntityReference apply(EntityReferenceLocal ref) {
			try {
				GlobalEntityDecl entity = entities().declaration(ref);
				try {
					Optional<QID> name = globalNames().globalName(entity);
					return name.<EntityReference>map(EntityReferenceGlobal::new).orElse(ref);
				} catch (Exception e) {
					throw new CompilationException(
							new Diagnostic(Diagnostic.Kind.ERROR, "Could not resolve global entity name" + entity.getName() + "\n" + e.getMessage()));
				}
			} catch (Exception e) {
				throw new CompilationException(
						new Diagnostic(Diagnostic.Kind.ERROR, "Failed looking up Entity reference " + ref.getName() + "\n" + e.getMessage()));
			}


		}
	}
}
