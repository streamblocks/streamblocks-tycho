package net.opendf.backend.c.att;

import javarag.Module;
import javarag.Synthesized;
import net.opendf.backend.c.CType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.GeneratorFilter;
import net.opendf.ir.common.Statement;
import net.opendf.ir.common.StmtBlock;
import net.opendf.ir.common.StmtCall;
import net.opendf.ir.common.StmtForeach;
import net.opendf.ir.common.StmtIf;
import net.opendf.ir.common.StmtWhile;
import net.opendf.ir.common.TypeExpr;

public class Statements extends Module<Statements.Required> {

	public interface Required {

		String procedureCall(Expression procedure, StmtCall stmt);

		String simpleExpression(Expression condition);

		String blockified(Statement statement);

		String localDeclaration(DeclVar decl);

		String statement(Statement stmt);

		CType ctype(TypeExpr type);

		String variableName(DeclVar d);

		String generatorFilter(GeneratorFilter generator, String body);

	}

	@Synthesized
	public String statement(StmtBlock block) {
		StringBuilder builder = new StringBuilder();
		builder.append("{\n");
		for (DeclVar decl : block.getVarDecls()) {
			CType type = get().ctype(decl.getType());
			String name = get().variableName(decl);
			builder.append(type.variableType(name));
			if (decl.getInitialValue() != null) {
				String value = get().simpleExpression(decl.getInitialValue());
				builder.append(" = ");
				builder.append(value);
			}
			builder.append(";\n");

		}
		for (Statement stmt : block.getStatements()) {
			builder.append(get().statement(stmt));
		}
		builder.append("}\n");
		return builder.toString();
	}

	@Synthesized
	public String statement(StmtCall stmt) {
		return get().procedureCall(stmt.getProcedure(), stmt);
	}

	@Synthesized
	public String statement(StmtIf stmt) {
		String ifThen =
				"if (" + get().simpleExpression(stmt.getCondition()) + ") " +
						get().blockified(stmt.getThenBranch());
		if (stmt.getElseBranch() == null) {
			return ifThen;
		} else {
			return ifThen + " else " + get().blockified(stmt.getElseBranch());
		}
	}

	@Synthesized
	public String statement(StmtWhile stmt) {
		return "while (" + get().simpleExpression(stmt.getCondition()) + ") " +
				get().blockified(stmt.getBody());
	}

	@Synthesized
	public String statement(StmtForeach foreach) {
		if (foreach.getGenerators().size() != 1) return null;
		String body = get().statement(foreach.getBody());
		return get().generatorFilter(foreach.getGenerators().get(0), body);
	}
	
	@Synthesized
	public String blockified(StmtBlock block) {
		if (block.getVarDecls().isEmpty() && block.getStatements().size() == 1 && block.getStatements().get(0) instanceof StmtBlock) {
			return get().blockified(block.getStatements().get(0));
		} else {
			return get().statement(block);
		}
	}
	
	@Synthesized
	public String blockified(Statement stmt) {
		return "{\n" + get().statement(stmt) + "}\n";
	}

}