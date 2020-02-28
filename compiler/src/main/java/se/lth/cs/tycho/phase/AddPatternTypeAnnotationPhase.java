package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.TypeScopes;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.ir.decl.PatternVarDecl;
import se.lth.cs.tycho.ir.decl.ProductTypeDecl;
import se.lth.cs.tycho.ir.decl.SumTypeDecl;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstructor;
import se.lth.cs.tycho.ir.expr.pattern.PatternVariable;
import se.lth.cs.tycho.ir.expr.pattern.PatternWildcard;
import se.lth.cs.tycho.reporting.CompilationException;

import java.util.Objects;

public class AddPatternTypeAnnotationPhase implements Phase {

	@Override
	public String getDescription() {
		return "Annotates pattern variables and pattern wildcards with type information.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
		Transformation transformation = MultiJ.from(Transformation.class)
				.bind("tree").to(task.getModule(TreeShadow.key))
				.bind("typeScopes").to(task.getModule(TypeScopes.key))
				.instance();
		return transformation.transform(task);
	}

	@Module
	interface Transformation {

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(BindingKind.INJECTED)
		TypeScopes typeScopes();

		default IRNode transform(IRNode node) {
			return node.transformChildren(this::transform);
		}

		default CompilationTask transform(CompilationTask task) {
			return task.transformChildren(this::transform);
		}

		default PatternVariable transform(PatternVariable pattern) {
			IRNode parent = tree().parent(pattern);
			if (parent instanceof PatternDeconstructor) {
				PatternDeconstructor deconstructor = (PatternDeconstructor) parent;
				return typeScopes().construction(deconstructor).map(decl -> {
					GlobalTypeDecl type = (GlobalTypeDecl) decl;
					if (type.getDeclaration() instanceof ProductTypeDecl) {
						ProductTypeDecl product = (ProductTypeDecl) type.getDeclaration();
						int index = deconstructor.getPatterns().indexOf(pattern);
						return index < product.getFields().size() ? pattern.copy((PatternVarDecl) pattern.getDeclaration().withType(product.getFields().get(index).getType())) : pattern;
					} else if (type.getDeclaration() instanceof SumTypeDecl) {
						SumTypeDecl sum = (SumTypeDecl) type.getDeclaration();
						SumTypeDecl.VariantDecl variant = sum.getVariants().stream().filter(v -> Objects.equals(v.getName(), deconstructor.getName())).findAny().get();
						int index = deconstructor.getPatterns().indexOf(pattern);
						return index < variant.getFields().size() ? pattern.copy((PatternVarDecl) pattern.getDeclaration().withType(variant.getFields().get(index).getType())) : pattern;
					} else {
						return pattern;
					}
				}).orElse(pattern);
			}
			return pattern;
		}

		default PatternWildcard transform(PatternWildcard pattern) {
			IRNode parent = tree().parent(pattern);
			if (parent instanceof PatternDeconstructor) {
				PatternDeconstructor deconstructor = (PatternDeconstructor) parent;
				return typeScopes().construction(deconstructor).map(decl -> {
					GlobalTypeDecl type = (GlobalTypeDecl) decl;
					if (type.getDeclaration() instanceof ProductTypeDecl) {
						ProductTypeDecl product = (ProductTypeDecl) type.getDeclaration();
						int index = deconstructor.getPatterns().indexOf(pattern);
						return index < product.getFields().size() ? pattern.withType(product.getFields().get(index).getType()) : pattern;
					} else if (type.getDeclaration() instanceof SumTypeDecl) {
						SumTypeDecl sum = (SumTypeDecl) type.getDeclaration();
						SumTypeDecl.VariantDecl variant = sum.getVariants().stream().filter(v -> Objects.equals(v.getName(), deconstructor.getName())).findAny().get();
						int index = deconstructor.getPatterns().indexOf(pattern);
						return index < variant.getFields().size() ? pattern.withType(variant.getFields().get(index).getType()) : pattern;
					} else {
						return pattern;
					}
				}).orElse(pattern);
			}
			return pattern;
		}
	}
}
