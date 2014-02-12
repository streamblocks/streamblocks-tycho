package net.opendf.backend.c.att;

import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.LValue;
import net.opendf.ir.common.LValueIndexer;
import net.opendf.ir.common.LValueVariable;
import net.opendf.ir.common.StmtAssignment;
import net.opendf.ir.common.Variable;

public class Assignments extends Module<Assignments.Required> {


	@Synthesized
	public String lvalue(LValueVariable var) {
		return get().variableName(var.getVariable());
	}

	@Synthesized
	public String lvalue(LValueIndexer indexer) {
		return get().lvalue(indexer.getStructure())+"["+
			get().simpleExpression(indexer.getIndex())+"]";
	}

	@Synthesized
	public String statement(StmtAssignment assign) {
		String simpleExpression = get().simpleExpression(assign.getExpression());
		if (simpleExpression != null) {
			String lvalue = get().lvalue(assign.getLValue());
			return lvalue + " = " + simpleExpression + ";\n";
		}
		return null;
	}



	interface Required {
		String variableName(Variable var);
		String simpleExpression(Expression e);
		String lvalue(LValue lv);
	}
	
}