package net.opendf.backend.c.att;

import java.util.List;
import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprApplication;
import net.opendf.ir.common.ExprList;
import net.opendf.ir.common.ExprVariable;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.GeneratorFilter;

public class Lists extends Module<Lists.Required> {

	interface Required {
		String variableName(DeclVar decl);
		String simpleExpression(Expression e);
		String tempVariableName(Object o);
		String generatorFilter(GeneratorFilter gf, String c);
		String generator(Expression expr, List<DeclVar> vars, String c);
	}

	@Synthesized
	public String scopeVarInit(ExprList list, DeclVar varDecl) {
		String name = get().variableName(varDecl);
		if (list.getGenerators().isEmpty()) {
			StringBuilder result = new StringBuilder();
			int index = 0;
			for (Expression e : list.getElements()) {
				result.append(name)
					.append("[")
					.append(index)
					.append("] = ")
					.append(get().simpleExpression(e))
					.append(";\n");
				index += 1;
			}
			return result.toString();
		} else {
			if (list.getElements().size() != 1) return null;
			if (list.getGenerators().size() != 1) return null;
			GeneratorFilter gf = list.getGenerators().get(0);
			String temp = get().tempVariableName(list);
			String value = get().simpleExpression(list.getElements().get(0));
			return "int "+temp+" = 0;\n"+
				get().generatorFilter(gf, name+"["+temp+"++] = "+value+";\n");
		}
	}

	@Synthesized
	public String generatorFilter(GeneratorFilter gf, String content) {
		if (!gf.getFilters().isEmpty()) return null;
		return get().generator(gf.getCollectionExpr(), gf.getVariables(), content);
	}

	@Synthesized
	public String generator(ExprApplication coll, List<DeclVar> decls, String content) {
		Expression func = coll.getFunction();
		if (!(func instanceof ExprVariable)) return null;
		ExprVariable var = (ExprVariable) func;
		if (!var.getVariable().getName().equals("$BinaryOperation...")) return null;
		if (decls.size() != 1) return null;
		DeclVar decl = decls.get(0);
		String name = get().variableName(decl);
		String from = get().simpleExpression(coll.getArgs().get(0));
		String to = get().simpleExpression(coll.getArgs().get(1));
		return "for (int "+name+"="+from+"; "+name+" <= "+to+"; "+name+"++) {\n"+content+"}\n";
	}
	
}