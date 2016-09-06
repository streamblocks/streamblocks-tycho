package se.lth.cs.tycho.decoration;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprUnaryOp;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Optional;
import java.util.function.Predicate;

public final class StaticConstants {
	private StaticConstants() {}

	private static final StaticConstantsModule module = MultiJ.instance(StaticConstantsModule.class);

	public static boolean isStaticConstant(Tree<Expression> expression) {
		throw new UnsupportedOperationException("Not yet implemented");
		// return module.isStaticConstant(expression, expression.node());
	}

	@Module
	interface StaticConstantsModule {
		default boolean isStaticConstant(Tree tree_, Expression expression) {
			return false;
		}

		default boolean isStaticConstant(Tree tree_, ExprLiteral exprLiteral) {
			return true;
		}

		default boolean isStaticConstant(Tree tree_, ExprBinaryOp exprBinaryOp) {
			return reconstruct(tree_, exprBinaryOp)
					.children(ExprBinaryOp::getOperands)
					.allMatch(StaticConstants::isStaticConstant);
		}

		default boolean isStaticConstant(Tree tree, ExprUnaryOp exprUnaryOp) {
			return StaticConstants.isStaticConstant(
					reconstruct(tree, exprUnaryOp).child(ExprUnaryOp::getOperand));
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
