package net.opendf.backend.c.att;

import javarag.Module;
import javarag.Synthesized;
import net.opendf.backend.c.CType;
import net.opendf.ir.GeneratorFilter;
import net.opendf.ir.TypeExpr;
import net.opendf.ir.decl.LocalVarDecl;
import net.opendf.ir.decl.VarDecl;
import net.opendf.ir.expr.Expression;
import net.opendf.ir.stmt.Statement;
import net.opendf.ir.stmt.StmtBlock;
import net.opendf.ir.stmt.StmtCall;
import net.opendf.ir.stmt.StmtForeach;
import net.opendf.ir.stmt.StmtIf;
import net.opendf.ir.stmt.StmtWhile;

public class Statements extends Module<Statements.Decls> {

	public interface Decls {

		@Synthesized
		String blockified(Statement statement);

		@Synthesized
		String statement(Statement stmt);

		String procedureCall(Expression procedure, StmtCall stmt);

		String simpleExpression(Expression condition);

		CType ctype(TypeExpr type);

		String variableName(VarDecl d);

		String generatorFilter(GeneratorFilter generator, String body);

	}

	public String statement(StmtBlock block) {
		StringBuilder builder = new StringBuilder();
		builder.append("{\n");
		for (LocalVarDecl decl : block.getVarDecls()) {
			CType type = e().ctype(decl.getType());
			String name = e().variableName(decl);
			builder.append(type.variableType(name));
			if (decl.getInitialValue() != null) {
				String value = e().simpleExpression(decl.getInitialValue());
				builder.append(" = ");
				builder.append(value);
			}
			builder.append(";\n");

		}
		for (Statement stmt : block.getStatements()) {
			builder.append(e().statement(stmt));
		}
		builder.append("}\n");
		return builder.toString();
	}

	public String statement(StmtCall stmt) {
		return e().procedureCall(stmt.getProcedure(), stmt);
	}

	public String statement(StmtIf stmt) {
		String ifThen =
				"if (" + e().simpleExpression(stmt.getCondition()) + ") " +
						e().blockified(stmt.getThenBranch());
		if (stmt.getElseBranch() == null) {
			return ifThen;
		} else {
			return ifThen + " else " + e().blockified(stmt.getElseBranch());
		}
	}

	public String statement(StmtWhile stmt) {
		return "while (" + e().simpleExpression(stmt.getCondition()) + ") " +
				e().blockified(stmt.getBody());
	}

	public String statement(StmtForeach foreach) {
		if (foreach.getGenerators().size() != 1)
			return null;
		String body = e().statement(foreach.getBody());
		return e().generatorFilter(foreach.getGenerators().get(0), body);
	}

	public String blockified(StmtBlock block) {
		if (block.getVarDecls().isEmpty() && block.getStatements().size() == 1
				&& block.getStatements().get(0) instanceof StmtBlock) {
			return e().blockified(block.getStatements().get(0));
		} else {
			return e().statement(block);
		}
	}

	public String blockified(Statement stmt) {
		return "{\n" + e().statement(stmt) + "}\n";
	}

}