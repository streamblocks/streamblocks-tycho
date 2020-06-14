package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.TypeScopes;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstruction;
import se.lth.cs.tycho.ir.expr.pattern.PatternBinding;
import se.lth.cs.tycho.reporting.CompilationException;

import java.util.Collections;

public class ResolvePatternDeconstructionPhase implements Phase {

	@Override
	public String getDescription() {
		return "Resolve deconstruction patterns";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
		Transformation transformation = MultiJ.from(Transformation.class)
				.bind("typeScopes").to(task.getModule(TypeScopes.key))
				.instance();
		return task.transformChildren(transformation);
	}

	@Module
	interface Transformation extends IRNode.Transformation {

		@Binding(BindingKind.INJECTED)
		TypeScopes typeScopes();

		@Override
		default IRNode apply(IRNode node) {
			return node.transformChildren(this);
		}

		default IRNode apply(PatternBinding pattern) {
			if (typeScopes().construction(pattern.getDeclaration()).isPresent()) {
				return new PatternDeconstruction(pattern, pattern.getDeclaration().getName(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
			}
			return pattern;
		}
	}
}
