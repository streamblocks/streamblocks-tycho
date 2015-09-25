package se.lth.cs.tycho.ir.stmt;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class StmtRead extends Statement {
	private final Port port;
	private final ImmutableList<LValue> lvalues;
	private final Expression repeatExpression;

	public StmtRead(Port port, ImmutableList<LValue> lvalues, Expression repeatExpression) {
		this(null, port, lvalues, repeatExpression);
	}

	private StmtRead(StmtRead original, Port port, List<LValue> lvalues, Expression repeatExpression) {
		super(original);
		assert port != null;
		this.port = port;
		this.lvalues = ImmutableList.from(lvalues);
		this.repeatExpression = repeatExpression;
	}

	public StmtRead copy(Port port, List<LValue> lvalues, Expression repeatExpression) {
		if (this.port == port && Lists.elementIdentityEquals(this.lvalues, lvalues) && this.repeatExpression == repeatExpression) {
			return this;
		} else {
			return new StmtRead(this, port, lvalues, repeatExpression);
		}
	}

	public Port getPort() {
		return port;
	}

	public ImmutableList<LValue> getLValues() {
		return lvalues;
	}

	public Expression getRepeatExpression() {
		return repeatExpression;
	}

	@Override
	public <R, P> R accept(StatementVisitor<R, P> v, P p) {
		throw new Error();
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(port);
		lvalues.forEach(action);
		if (repeatExpression != null) action.accept(repeatExpression);
	}

	@Override
	public StmtRead transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return copy(
				(Port) transformation.apply(port),
				(ImmutableList) lvalues.map(transformation),
				repeatExpression == null ? null : (Expression) transformation.apply(repeatExpression)
		);
	}
}
