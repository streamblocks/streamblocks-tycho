package net.opendf.ir.net.ast;

import net.opendf.ir.common.Expression;
import net.opendf.ir.util.ImmutableList;

/**
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

public class StructureIfStmt extends StructureStatement{
	public StructureIfStmt(Expression condition, ImmutableList<StructureStatement> trueStmt, ImmutableList<StructureStatement> falseStmt){
		this.condition = condition;
		this.trueStmt = ImmutableList.copyOf(trueStmt);
		this.falseStmt = ImmutableList.copyOf(falseStmt);
	}
	public Expression getCondition(){
		return condition;
	}
	public ImmutableList<StructureStatement> getTrueStmt(){
		return trueStmt;
	}
	public ImmutableList<StructureStatement> getFalseStmt(){
		return falseStmt;
	}
	@Override
	public <R, P> R accept(StructureStmtVisitor<R, P> v, P p) {
		return v.visitStructureIfStmt(this, p);
	}
	
	private ImmutableList<StructureStatement> trueStmt, falseStmt;
	private Expression condition;
}
