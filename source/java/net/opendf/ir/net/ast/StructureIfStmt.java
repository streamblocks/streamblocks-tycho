package net.opendf.ir.net.ast;

import net.opendf.ir.common.Expression;

/**
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

public class StructureIfStmt extends StructureStatement{
	public StructureIfStmt(Expression condition, StructureStatement[] trueStmt, StructureStatement[] falseStmt){
		this.condition = condition;
		this.trueStmt = trueStmt;
		this.falseStmt = falseStmt;
	}
	public Expression getCondition(){
		return condition;
	}
	public StructureStatement[] getTrueStmt(){
		return trueStmt;
	}
	public StructureStatement[] getFalseStmt(){
		return falseStmt;
	}
	@Override
	public <R, P> R accept(StructureStmtVisitor<R, P> v, P p) {
		return v.visitStructureIfStmt(this, p);
	}
	
	private StructureStatement trueStmt[], falseStmt[];
	private Expression condition;
}
