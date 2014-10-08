package se.lth.cs.tycho.backend.c.att;

import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtAssignment;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueIndexer;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import javarag.Module;
import javarag.Synthesized;

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