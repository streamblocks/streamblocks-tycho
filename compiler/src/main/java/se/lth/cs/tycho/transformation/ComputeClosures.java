package se.lth.cs.tycho.transformation;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.decoration.FreeVariables;
import se.lth.cs.tycho.decoration.Tree;
import se.lth.cs.tycho.decoration.VariableDeclarations;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NominalTypeExpr;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.TypeParameter;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.ClosureVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprDeref;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprRef;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueDeref;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

public class ComputeClosures {

	public static IRNode computeClosures(IRNode node, LongSupplier uniqueNumbers) {
		AddToClosure addToClosure = MultiJ.from(AddToClosure.class)
				.bind("uniqueNumbers").to(uniqueNumbers)
				.instance();
		IRNode transformed = node;
		do {
			node = transformed;
			transformed = addToClosure.apply(Tree.of(transformed));
		} while (node != transformed);
		return transformed;
	}

	@Module
	interface AddToClosure extends Function<Tree<? extends IRNode>, IRNode> {

		@Binding(BindingKind.INJECTED)
		LongSupplier uniqueNumbers();

		@Override
		default IRNode apply(Tree<? extends IRNode> node) {
			return transform(node, node.node());
		}

		default IRNode transform(Tree<?> tree, IRNode node) {
			return tree.transformChildren(this);
		}

		default IRNode transform(Tree<?> tree, ExprLambda lambda) {
			Set<Tree<? extends VarDecl>> freeVariables = FreeVariables.freeVariables(tree);
			if (!freeVariables.isEmpty()) {
				Map<Tree<? extends VarDecl>, String> renameTable = renameTable(freeVariables);
				VariableTransformation varTransform = new VariableTransformation(renameTable);
				ExprLambda transformed = (ExprLambda) varTransform.transform(tree);
				ImmutableList<ClosureVarDecl> decls = freeVariables.stream()
						.map(decl -> createClosureVarDecl(decl.node(), renameTable.get(decl)))
						.collect(ImmutableList.collector());
				return transformed.withClosure(ImmutableList.concat(transformed.getClosure(), decls));
			} else {
				return tree.transformChildren(this);
			}
		}

		default Map<Tree<? extends VarDecl>, String> renameTable(Set<Tree<? extends VarDecl>> vars) {
			return vars.stream().collect(Collectors.toMap(
					Function.identity(),
					v -> v.node().getOriginalName() + "_" + uniqueNumbers().getAsLong()));
		}

		default ClosureVarDecl createClosureVarDecl(VarDecl decl, String name) {
			return new ClosureVarDecl(refTypeOf(decl.getType()), name, new ExprRef(Variable.variable(decl.getName())));
		}

		default TypeExpr<?> refTypeOf(TypeExpr<?> type) {
			return new NominalTypeExpr("Ref", ImmutableList.of(new TypeParameter("type", type)), ImmutableList.empty());
		}

	}

	private static class VariableTransformation {
		private final Map<Tree<? extends VarDecl>, String> table;

		public VariableTransformation(Map<Tree<? extends VarDecl>, String> table) {
			this.table = table;
		}

		public IRNode transform(Tree<? extends IRNode> node) {
			{
				Optional<Tree<ExprVariable>> var = node.tryCast(ExprVariable.class);
				if (var.isPresent()) {
					return transformVar(var.get());
				}
			}
			{
				Optional<Tree<LValueVariable>> lvalue = node.tryCast(LValueVariable.class);
				if (lvalue.isPresent()) {
					return transformLValue(lvalue.get());
				}
			}
			return node.transformChildren(this::transform);
		}
		public Expression transformVar(Tree<ExprVariable> var) {
			Optional<Tree<? extends VarDecl>> declaration = VariableDeclarations.getDeclaration(var.child(ExprVariable::getVariable));
			if (declaration.isPresent()) {
				String name = table.get(declaration.get());
				if (name != null) {
					return new ExprDeref(var.node().copy(var.node().getVariable().withName(name)));
				}
			}
			return var.node();
		}
		public LValue transformLValue(Tree<LValueVariable> lvalue) {
			Optional<Tree<? extends VarDecl>> declaration = VariableDeclarations.getDeclaration(lvalue.child(LValueVariable::getVariable));
			if (declaration.isPresent()) {
				String name = table.get(declaration.get());
				if (name != null) {
					return new LValueDeref(lvalue.node().copy(lvalue.node().getVariable().withName(name)));
				}
			}
			return lvalue.node();
		}
	}

}
