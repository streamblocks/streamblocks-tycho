package se.lth.cs.tycho.backend.c.att;

import java.util.List;

import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprList;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import javarag.Module;
import javarag.Synthesized;

public class Lists extends Module<Lists.Decls> {

	public interface Decls {
		@Synthesized
		String varInit(Expression expr, String name);

		@Synthesized
		String generatorFilter(GeneratorFilter gf, String content);

		@Synthesized
		String generator(Expression expr, List<VarDecl> decls, String content);

		String variableName(VarDecl decl);

		String simpleExpression(Expression e);

		String tempVariableName(Object o);

	}

	public String varInit(ExprList list, String name) {
		if (list.getGenerators().isEmpty()) {
			StringBuilder result = new StringBuilder();
			int index = 0;
			for (Expression e : list.getElements()) {
				result.append(name)
						.append("[")
						.append(index)
						.append("] = ")
						.append(e().simpleExpression(e))
						.append(";\n");
				index += 1;
			}
			return result.toString();
		} else {
			if (list.getElements().size() != 1)
				return null;
			if (list.getGenerators().size() != 1)
				return null;
			GeneratorFilter gf = list.getGenerators().get(0);
			String temp = e().tempVariableName(list);
			String value = e().simpleExpression(list.getElements().get(0));
			return "int " + temp + " = 0;\n" +
					e().generatorFilter(gf, name + "[" + temp + "++] = " + value + ";\n");
		}
	}

	public String generatorFilter(GeneratorFilter gf, String content) {
		if (!gf.getFilters().isEmpty())
			return null;
		return e().generator(gf.getCollectionExpr(), gf.getVariables(), content);
	}

	public String generator(ExprApplication coll, List<VarDecl> decls, String content) {
		Expression func = coll.getFunction();
		if (!(func instanceof ExprVariable))
			return null;
		ExprVariable var = (ExprVariable) func;
		if (!var.getVariable().getName().equals("$BinaryOperation..."))
			return null;
		if (decls.size() != 1)
			return null;
		VarDecl decl = decls.get(0);
		String name = e().variableName(decl);
		String from = e().simpleExpression(coll.getArgs().get(0));
		String to = e().simpleExpression(coll.getArgs().get(1));
		return "for (int " + name + "=" + from + "; " + name + " <= " + to + "; " + name + "++) {\n" + content + "}\n";
	}

}