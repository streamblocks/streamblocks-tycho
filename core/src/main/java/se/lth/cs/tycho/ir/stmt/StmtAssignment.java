package se.lth.cs.tycho.ir.stmt;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;

/**
 * A statement for assigning to a variable.
 */
public class StmtAssignment extends Statement {
	private LValue lvalue;
	private Expression expression;

	/**
	 * Constructs a StmtAssignment.
	 * 
	 * @param lvalue
	 *            the left hand side
	 * @param expression
	 *            the right hand side
	 */
	public StmtAssignment(LValue lvalue, Expression expression) {
		this(null, lvalue, expression);
	}

	private StmtAssignment(StmtAssignment original, LValue lvalue, Expression expression) {
		super(original);
		this.lvalue = lvalue;
		this.expression = expression;
	}

	public StmtAssignment copy(LValue lvalue, Expression expression) {
		if (Objects.equals(this.lvalue, lvalue) && Objects.equals(this.expression, expression)) {
			return this;
		}
		return new StmtAssignment(this, lvalue, expression);
	}

	/**
	 * Returns the left hand side of the assignment.
	 * 
	 * @return the left hand side
	 */
	public LValue getLValue() {
		return lvalue;
	}

	/**
	 * Returns the right hand side of the assignment.
	 * 
	 * @return the right hand side
	 */
	public Expression getExpression() {
		return expression;
	}

	@Override
	public <R, P> R accept(StatementVisitor<R, P> v, P p) {
		return v.visitStmtAssignment(this, p);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(lvalue);
		action.accept(expression);
	}

	@Override
	public StmtAssignment transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return copy((LValue) transformation.apply(lvalue), (Expression) transformation.apply(expression));
	}
}
