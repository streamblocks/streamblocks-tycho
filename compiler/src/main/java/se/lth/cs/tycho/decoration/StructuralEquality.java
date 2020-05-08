package se.lth.cs.tycho.decoration;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.TypeParameter;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprField;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;
import se.lth.cs.tycho.ir.expr.ExprIf;
import se.lth.cs.tycho.ir.expr.ExprIndexer;
import se.lth.cs.tycho.ir.expr.ExprList;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprTypeAssertion;
import se.lth.cs.tycho.ir.expr.ExprTypeConstruction;
import se.lth.cs.tycho.ir.expr.ExprUnaryOp;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.type.TypeExpr;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

public final class StructuralEquality {
	private StructuralEquality() {}
	private static final Equality equality = MultiJ.instance(Equality.class);

	public static boolean equals(Expression a, Expression b) {
		return equality.eq(a, b);
	}
	@Module
	interface Equality {
		default boolean eq(Expression a, Expression b) {
			return false;
		}

		default boolean eq(Variable a, Variable b) {
			return Objects.equals(a.getName(), b.getName());
		}

		default boolean eq(ExprVariable a, ExprVariable b) {
			return eq(a.getVariable(), b.getVariable());
		}

		default boolean eq(ExprBinaryOp a, ExprBinaryOp b) {
			return listEquals(a.getOperations(), b.getOperations(), String::equals)
					&& listEquals(a.getOperands(), b.getOperands(), this::eq);
		}

		default boolean eq(ExprUnaryOp a, ExprUnaryOp b) {
			return Objects.equals(a.getOperation(), b.getOperation()) && eq(a.getOperand(), b.getOperand());
		}

		default boolean eq(ExprApplication a, ExprApplication b) {
			return eq(a.getFunction(), b.getFunction()) && listEquals(a.getArgs(), b.getArgs(), this::eq);
		}

		default boolean eq(ExprLiteral a, ExprLiteral b) {
			return a.getKind() == b.getKind() && Objects.equals(a.getText(), b.getText());
		}

		default boolean eq(ExprGlobalVariable a, ExprGlobalVariable b) {
			return Objects.equals(a.getGlobalName(), b.getGlobalName());
		}

		default boolean eq(ExprIndexer a, ExprIndexer b) {
			return eq(a.getStructure(), b.getStructure()) && eq(a.getIndex(), b.getIndex());
		}

		default boolean eq(ExprField a, ExprField b) {
			return eq(a.getStructure(), b.getStructure())
					&& Objects.equals(a.getField().getName(), b.getField().getName());
		}

		default boolean eq(ExprIf a, ExprIf b) {
			return eq(a.getCondition(), b.getCondition())
					&& eq(a.getThenExpr(), b.getThenExpr())
					&& eq(a.getElseExpr(), b.getElseExpr());
		}

		default boolean eq(ExprTypeConstruction a, ExprTypeConstruction b) {
			return Objects.equals(a.getConstructor(), b.getConstructor())
					&& listEquals(a.getArgs(), b.getArgs(), this::eq);
		}

		default boolean eq(ExprTypeAssertion a, ExprTypeAssertion b) {
			return eq(a.getType(), b.getType()) && eq(a.getExpression(), b.getExpression());
		}

		default boolean eq(ExprList a, ExprList b) {
			return listEquals(a.getElements(), b.getElements(), this::eq);
		}

		default boolean eq(TypeExpr a, TypeExpr b) {
			return false;
		}

		default boolean eq(NominalTypeExpr a, NominalTypeExpr b) {
			return Objects.equals(a.getName(), b.getName())
					&& listEquals(a.getTypeParameters(), b.getTypeParameters(), this::eq)
					&& listEquals(a.getValueParameters(), b.getValueParameters(), this::eq);
		}

		default boolean eq(TypeParameter a, TypeParameter b) {
			return Objects.equals(a.getName(), b.getName()) && eq(a.getValue(), b.getValue());
		}

		default boolean eq(ValueParameter a, ValueParameter b) {
			return Objects.equals(a.getName(), b.getName()) && eq(a.getValue(), b.getValue());
		}

		default <T> boolean listEquals(List<T> a, List<T> b, BiPredicate<T, T> equals) {
			if (a.size() != b.size()) {
				return false;
			}
			Iterator<T> as = a.iterator();
			Iterator<T> bs = b.iterator();
			while (as.hasNext() && bs.hasNext()) {
				if (!equals.test(as.next(), bs.next())) {
					return false;
				}
			}
			return as.hasNext() == bs.hasNext();
		}
	}
}
