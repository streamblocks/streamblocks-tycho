package se.lth.cs.tycho.decoration;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprUnaryOp;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ConstantFolding {
	private ConstantFolding() {}

	private static final ConstantFoldingModule module = MultiJ.instance(ConstantFoldingModule.class);

	public static Optional<BigInteger> integerConstant(Tree<Expression> expression) {
		return module.integerConstant(expression, expression.node());
	}

	public static Optional<Boolean> booleanConstant(Tree<Expression> expression) {
		return module.booleanConstant(expression, expression.node());
	}

	@Module
	interface ConstantFoldingModule {
		default Optional<BigInteger> integerConstant(Tree tree_, Expression expression) {
			return Optional.empty();
		}

		default Optional<BigInteger> integerConstant(Tree tree_, ExprLiteral expression) {
			if (expression.getKind() == ExprLiteral.Kind.Integer) {
				String text = expression.getText();
				if (text.startsWith("0x")) {
					return Optional.of(new BigInteger(text.substring(2), 16));
				} else {
					return Optional.of(new BigInteger(text, 10));
				}
			} else {
				return Optional.empty();
			}
		}

		default Optional<BigInteger> integerConstant(Tree tree_, ExprVariable expression) {
			return variableConstant(reconstruct(tree_, expression), ConstantFolding::integerConstant);
		}

		default <T> Optional<T> variableConstant(Tree<ExprVariable> expr, Function<Tree<Expression>, Optional<T>> evaluator) {
			Optional<Tree<VarDecl>> decl = VariableDeclarations.getDeclaration(expr.child(ExprVariable::getVariable))
					.flatMap(ImportDeclarations::followVariableImport);
			if (decl.isPresent() && decl.get().node().isConstant()) {
				return evaluator.apply(decl.get().child(VarDecl::getValue));
			} else {
				return Optional.empty();
			}
		}

		default Optional<BigInteger> integerConstant(Tree tree_, ExprUnaryOp expression) {
			Optional<BigInteger> operandValue = ConstantFolding.integerConstant(reconstruct(tree_, expression).child(ExprUnaryOp::getOperand));
			if (operandValue.isPresent()) {
				switch (expression.getOperation()) {
					case "-": return Optional.of(operandValue.get().negate());
				}
			}
			return Optional.empty();
		}

		default Optional<BigInteger> integerConstant(Tree tree_, ExprBinaryOp expression) {
			return genericBinaryOp(reconstruct(tree_, expression), ConstantFolding::integerConstant, this::integerBinaryOp);
		}

		default <T> Optional<T> genericBinaryOp(Tree<ExprBinaryOp> binaryOp, Function<Tree<Expression>, Optional<T>> evaluator, Operation<T> operation) {
			if (binaryOp.node().getOperands().size() != 2 || binaryOp.node().getOperations().size() != 1) {
				return Optional.empty();
			}
			List<Tree<Expression>> operands = binaryOp.children(ExprBinaryOp::getOperands).collect(Collectors.toList());
			Optional<T> x = evaluator.apply(operands.get(0));
			if (x.isPresent()) {
				Optional<T> y = evaluator.apply(operands.get(1));
				if (y.isPresent()) {
					return operation.apply(binaryOp.node().getOperations().get(0), x.get(), y.get());
				}
			}
			return Optional.empty();
		}

		interface Operation<T> {
			Optional<T> apply(String op, T x, T y);
		}

		default Optional<BigInteger> integerBinaryOp(String operation, BigInteger x, BigInteger y) {
			switch (operation) {
				case "+": return Optional.of(x.add(y));
				case "-": return Optional.of(x.subtract(y));
				case "*": return Optional.of(x.multiply(y));
				case "/": return Optional.of(x.divide(y));
				case "%": return Optional.of(x.remainder(y));
				case "<<": return Optional.of(x.shiftLeft(y.intValueExact()));
				case ">>": return Optional.of(x.shiftRight(y.intValueExact()));
				case "|": return Optional.of(x.or(y));
				case "&": return Optional.of(x.and(y));
				case "^": return Optional.of(x.xor(y));
				default: return Optional.empty();
			}
		}

		default Optional<Boolean> booleanConstant(Tree tree_, Expression expression) {
			return Optional.empty();
		}

		default Optional<Boolean> booleanConstant(Tree tree_, ExprLiteral expression) {
			switch (expression.getKind()) {
				case True: return Optional.of(true);
				case False: return Optional.of(false);
				default: return Optional.empty();
			}
		}

		default Optional<Boolean> booleanConstant(Tree tree_, ExprVariable expression) {
			return variableConstant(reconstruct(tree_, expression), ConstantFolding::booleanConstant);
		}

		default Optional<Boolean> booleanConstant(Tree tree_, ExprBinaryOp expression) {
			return genericBinaryOp(reconstruct(tree_, expression), ConstantFolding::booleanConstant, this::booleanBinaryOp);
		}

		default Optional<Boolean> booleanBinaryOp(String op, boolean x, boolean y) {
			switch (op) {
				case "and":
				case "&&": return Optional.of(x && y);
				case "or":
				case "||": return Optional.of(x || y);
				default: return Optional.empty();
			}
		}

		@SuppressWarnings("unchecked")
		default <T extends IRNode> Tree<T> reconstruct(Tree tree, T node) {
			if (tree.node() != node) {
				throw new IllegalArgumentException("Tree node mismatch.");
			}
			return (Tree<T>) tree;
		}
	}
}


