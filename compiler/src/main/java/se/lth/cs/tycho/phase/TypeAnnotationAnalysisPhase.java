package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.TypeScopes;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.GlobalDeclarations;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.TypeParameter;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class TypeAnnotationAnalysisPhase implements Phase {
	@Override
	public String getDescription() {
		return "Analyzes type annotations.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		TypeExprChecker checker = MultiJ.from(TypeExprChecker.class)
				.bind("reporter").to(context.getReporter())
				.bind("tree").to(task.getModule(TreeShadow.key))
				.bind("typeScopes").to(task.getModule(TypeScopes.key))
				.instance();
		checker.checkTree(task);
		return task;
	}

	@Module
	interface TypeExprChecker {
		@Binding(BindingKind.INJECTED)
		Reporter reporter();

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(BindingKind.INJECTED)
		TypeScopes typeScopes();

		default void checkTree(IRNode node) {
			checkNode(node);
			node.forEachChild(this::checkTree);
		}

		default void checkNode(IRNode node) {}

		default void checkNode(NominalTypeExpr type) {
			switch (type.getName()) {
				case "List":
					checkTypeParams(type, "type");
					checkValueParams(type, "size");
					break;
				case "Set":
				case "complex":
					checkTypeParams(type, "type");
					break;
				case "Map":
					checkTypeParams(type, "key", "value");
					break;
				case "uint":
				case "int":
					checkTypeParams(type);
					checkValueParams(type, "size");
					break;
				case "char":
				case "float":
				case "double":
				case "bool":
				case "String":
					checkTypeParams(type);
					checkValueParams(type);
					break;
				default:
					ImmutableList<TypeDecl> typeDecls = typeScopes().declarations(sourceUnit(type).getTree());
					if (typeDecls.stream().anyMatch(decl -> decl.getName().equals(type.getName()))) {
						return;
					}
					reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, String.format("Unknown type %s", type.getName()), sourceUnit(type), type));
			}
		}

		default void checkTypeParams(NominalTypeExpr type, String... names) {
			Set<String> nameSet = Arrays.stream(names).collect(Collectors.toSet());
			for (TypeParameter par : type.getTypeParameters()) {
				if (!nameSet.contains(par.getName())) {
					reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, String.format("Unknown parameter %s. Expected one of %s", par.getName(), Arrays.toString(names)), sourceUnit(type), type));
				}
			}
		}

		default void checkValueParams(NominalTypeExpr type, String... names) {
			Set<String> nameSet = Arrays.stream(names).collect(Collectors.toSet());
			for (ValueParameter par : type.getValueParameters()) {
				if (!nameSet.contains(par.getName())) {
					reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, String.format("Unknown parameter %s. Expected one of %s", par.getName(), Arrays.toString(names)), sourceUnit(type), type));
				}
			}
		}

		default SourceUnit sourceUnit(IRNode node) {
			return sourceUnit(tree().parent(node));
		}

		default SourceUnit sourceUnit(CompilationTask task) {
			GlobalEntityDecl entityDecl = GlobalDeclarations.getEntity(task, task.getIdentifier());
			return sourceUnit(tree().parent(entityDecl));
		}

		default SourceUnit sourceUnit(SourceUnit unit) {
			return unit;
		}
	}
}
