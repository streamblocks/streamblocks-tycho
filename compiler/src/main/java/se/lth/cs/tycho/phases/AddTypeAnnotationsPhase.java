package se.lth.cs.tycho.phases;

import org.multij.Binding;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.decoration.TypeToTypeExpr;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.phases.attributes.Types;
import se.lth.cs.tycho.types.Type;

import java.util.function.BiFunction;

import static org.multij.BindingKind.INJECTED;

public class AddTypeAnnotationsPhase implements Phase {
	@Override
	public String getDescription() {
		return "Adds type annotations to variable declarations.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
        TypeAnnotationAdder typeAnnotationAdder = MultiJ.from(TypeAnnotationAdder.class)
				.bind("types").to(task.getModule(Types.key))
				.instance();
		return typeAnnotationAdder.applyChecked(CompilationTask.class, task);
		}

	@Module
	interface TypeAnnotationAdder extends IRNode.Transformation {
		@Binding(INJECTED)
		Types types();

		@Override
		default IRNode apply(IRNode node) {
			return node.transformChildren(this);
		}

		default LocalVarDecl apply(LocalVarDecl decl) {
			return transformGeneric(decl.transformChildren(this), LocalVarDecl::withType);
		}

		default GlobalVarDecl apply(GlobalVarDecl decl) {
			return transformGeneric(decl.transformChildren(this), GlobalVarDecl::withType);
		}

		default <T extends VarDecl> T transformGeneric(T node, BiFunction<T, TypeExpr, T> withType) {
			if (node.getType() == null) {
				Type type = types().type(node.getValue());
				TypeExpr typeExpr = TypeToTypeExpr.convert(type);
				return withType.apply(node, typeExpr);
			} else {
				return node;
			}
		}
	}
}
