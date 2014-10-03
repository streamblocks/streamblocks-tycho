package net.opendf.backend.c.att;

import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.LValue;
import net.opendf.ir.common.LValueIndexer;
import net.opendf.ir.common.LValueVariable;
import net.opendf.ir.common.Statement;
import net.opendf.ir.common.StmtAssignment;
import net.opendf.ir.common.Variable;

public class Assignments extends Module<Assignments.Decls> {
	
	public interface Decls {
		@Synthesized
		String lvalue(LValue lv);

		@Synthesized
		String statement(Statement stmt);

		String variableName(Variable var);

		String simpleExpression(Expression e);
	}

	public String lvalue(LValueVariable var) {
		return e().variableName(var.getVariable());
	}

	public String lvalue(LValueIndexer indexer) {
		return e().lvalue(indexer.getStructure()) + "[" +
				e().simpleExpression(indexer.getIndex()) + "]";
	}

	public String statement(StmtAssignment assign) {
		String simpleExpression = e().simpleExpression(assign.getExpression());
		if (simpleExpression != null) {
			String lvalue = e().lvalue(assign.getLValue());
			return lvalue + " = " + simpleExpression + ";\n";
		}
		return null;
	}

}