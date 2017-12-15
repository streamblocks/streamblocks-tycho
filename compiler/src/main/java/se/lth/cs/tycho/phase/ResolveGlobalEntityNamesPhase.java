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
		return task.transformChildren(transformation);
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
			GlobalEntityDecl entity = entities().declaration(ref);
			Optional<QID> name = globalNames().globalName(entity);
			return name.<EntityReference>map(EntityReferenceGlobal::new).orElse(ref);
		}
	}
}
