package net.opendf.backend.c.att;

import java.io.PrintWriter;
import java.util.Set;

import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;
import net.opendf.backend.c.CType;
import net.opendf.ir.IRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Scope;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprLambda;
import net.opendf.ir.common.ExprProc;
import net.opendf.ir.common.Expression;
import net.opendf.ir.net.Node;

public class Scopes extends Module<Scopes.Required> {

	public interface Required {

		String scopeVarDecl(DeclVar decl);

		int index(IRNode n);

		Object variableScope(DeclVar varDecl);

		boolean isPersistent(Object variableScope);

		String simpleExpression(Expression initialValue);

		String variableType(CType type, String name);

		String variableName(DeclVar decl);

		CType ctype(DeclVar decl);

		Node node(ActorMachine actorMachine);

		String scopeVarInit(Expression expression, DeclVar decl);

		Set<Scope> persistentScopes(IRNode node);

		boolean scopeDeclIsConst(DeclVar varDecl);

	}

	@Synthesized
	public void scopes(ActorMachine actorMachine, PrintWriter writer) {
		for (Scope s : actorMachine.getScopes()) {
			for (DeclVar decl : s.getDeclarations()) {
				String d = get().scopeVarDecl(decl);
				if (d != null) writer.print(d);
			}
		}
		int node = get().index(get().node(actorMachine));
		for (Scope s : actorMachine.getScopes()) {
			int index = get().index(s);
			writer.println("static void init_n" + node + "s" + index + "(void) {");
			for (DeclVar decl : s.getDeclarations()) if (decl.getInitialValue() != null && !get().scopeDeclIsConst(decl)) {
				String simpleExpression = get().simpleExpression(decl.getInitialValue());
				if (simpleExpression != null) {
					writer.println(get().variableName(decl) + " = " + simpleExpression + ";");
				} else {
					writer.print(get().scopeVarInit(decl.getInitialValue(), decl));
				}
			}
			writer.println("}");
		}
	}

	@Synthesized
	public boolean scopeDeclIsConst(DeclVar decl) {
		return !decl.isAssignable() && get().isPersistent(get().variableScope(decl)) && get().simpleExpression(decl.getInitialValue()) != null;
	}

	@Synthesized
	public boolean isPersistent(Scope s) {
		Set<Scope> persistent = get().persistentScopes(s);
		return persistent.contains(s);
	}

	@Inherited
	public Scope variableScope(Scope s) {
		return s;
	}

	@Inherited
	public Scope variableScope(Object o) {
		return null;
	}

	@Synthesized
	public String scopeVarDecl(DeclVar decl) {
		StringBuilder result = new StringBuilder();
		result.append("static ");
		String value = null;
		Expression initialValue = decl.getInitialValue();
		if (initialValue != null) {
			value = get().simpleExpression(initialValue);
			if (initialValue instanceof ExprLambda || initialValue instanceof ExprProc) {
				return null;
			}
		}
		boolean constant = get().scopeDeclIsConst(decl);
		if (constant) {
			result.append("const ");
		}
		CType type = get().ctype(decl);
		String name = get().variableName(decl);
		String var = type.variableType(name);
		result.append(var);
		if (constant) {
			result.append(" = ").append(value);
		}
		result.append(";\n");
		return result.toString();
	}

}
