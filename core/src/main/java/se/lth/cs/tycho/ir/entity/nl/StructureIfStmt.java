package se.lth.cs.tycho.ir.entity.nl;

import java.util.Objects;

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
		this(null, condition, trueStmt, falseStmt);
	}

	private StructureIfStmt(StructureIfStmt original, Expression condition, ImmutableList<StructureStatement> trueStmt,
			ImmutableList<StructureStatement> falseStmt) {
		//TODO tool attributes
		super(original, null);
		this.condition = condition;
		this.trueStmt = ImmutableList.copyOf(trueStmt);
		this.falseStmt = ImmutableList.copyOf(falseStmt);
	}

	public StructureIfStmt copy(Expression condition, ImmutableList<StructureStatement> trueStmt,
			ImmutableList<StructureStatement> falseStmt) {
		if (Objects.equals(this.condition, condition) && Lists.equals(this.trueStmt, trueStmt)
				&& Lists.equals(this.falseStmt, falseStmt)) {
			return this;
		}
		return new StructureIfStmt(this, condition, trueStmt, falseStmt);
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
}
