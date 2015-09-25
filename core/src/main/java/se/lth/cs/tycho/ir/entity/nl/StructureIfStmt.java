package se.lth.cs.tycho.ir.entity.nl;

import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

/**
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public class StructureIfStmt extends StructureStatement {
	public StructureIfStmt(Expression condition, ImmutableList<StructureStatement> trueStmt,
			ImmutableList<StructureStatement> falseStmt) {
		super(null);
		this.condition = condition;
		this.trueStmt = ImmutableList.from(trueStmt);
		this.falseStmt = ImmutableList.from(falseStmt);
	}

	public StructureIfStmt copy(Expression condition, ImmutableList<StructureStatement> trueStmt,
			ImmutableList<StructureStatement> falseStmt) {
		if (Objects.equals(this.condition, condition) && Lists.equals(this.trueStmt, trueStmt)
				&& Lists.equals(this.falseStmt, falseStmt)) {
			return this;
		}
		return new StructureIfStmt(condition, trueStmt, falseStmt);
	}

	public Expression getCondition() {
		return condition;
	}

	public ImmutableList<StructureStatement> getTrueStmt() {
		return trueStmt;
	}

	public ImmutableList<StructureStatement> getFalseStmt() {
		return falseStmt;
	}

	@Override
	public <R, P> R accept(StructureStmtVisitor<R, P> v, P p) {
		return v.visitStructureIfStmt(this, p);
	}

	private ImmutableList<StructureStatement> trueStmt, falseStmt;
	private Expression condition;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(condition);
		trueStmt.forEach(action);
		falseStmt.forEach(action);
	}
}
