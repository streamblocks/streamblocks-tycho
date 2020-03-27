package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.decoration.TypeToTypeExpr;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.PatternVarDecl;
import se.lth.cs.tycho.ir.expr.pattern.PatternVariable;
import se.lth.cs.tycho.ir.expr.pattern.PatternWildcard;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.attribute.Types;

import java.util.Objects;

import static org.multij.BindingKind.INJECTED;

public class AddTransientPatternTypeAnnotationPhase implements Phase {

	@Override
	public String getDescription() {
		return "Annotates transient pattern variables and pattern wildcards with type information.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		Transformation transformation = MultiJ.from(Transformation.class)
				.bind("tree").to(task.getModule(TreeShadow.key))
				.bind("types").to(task.getModule(Types.key))
				.instance();
		return transformation.applyChecked(CompilationTask.class, task);
	}

	@Module
	interface Transformation extends IRNode.Transformation {

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(INJECTED)
		Types types();

		@Override
		default IRNode apply(IRNode node) {
			return node.transformChildren(this);
		}

		default PatternVariable apply(PatternVariable var) {
			NominalTypeExpr typeExpr = (NominalTypeExpr) var.getDeclaration().getType();
			if (Objects.equals(typeExpr.getName(), "<transient>")) {
				return var.copy((PatternVarDecl) var.getDeclaration().withType(TypeToTypeExpr.convert(types().type(var))));
			}
			return var;
		}

		default PatternWildcard apply(PatternWildcard wildcard) {
			NominalTypeExpr typeExpr = (NominalTypeExpr) wildcard.getType();
			if (Objects.equals(typeExpr.getName(), "<transient>")) {
				return wildcard.withType(TypeToTypeExpr.convert(types().type(wildcard)));
			}
			return wildcard;
		}
	}
}
