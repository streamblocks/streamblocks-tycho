package se.lth.cs.tycho.backend.c.att;

import java.io.PrintWriter;
import java.util.Set;

import se.lth.cs.tycho.backend.c.CType;
import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.Scope;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.expr.Expression;
import javarag.Inherited;
import javarag.Module;
import javarag.Procedural;
import javarag.Synthesized;

public class Scopes extends Module<Scopes.Decls> {

	public interface Decls {

		@Procedural
		public void scopes(ActorMachine actorMachine, PrintWriter writer);

		@Synthesized
		boolean scopeDeclIsConst(VarDecl varDecl);

		@Synthesized
		String scopeVarDecl(VarDecl decl);

		@Inherited
		Scope variableScope(VarDecl varDecl);

		@Synthesized
		boolean isPersistent(Scope scope);

		int index(IRNode n);

		String simpleExpression(Expression initialValue);

		String variableName(VarDecl decl);

		CType ctype(VarDecl decl);

		Node node(ActorMachine actorMachine);

		String scopeVarInit(Expression expression, VarDecl decl);

		Set<Scope> persistentScopes(IRNode node);

	}

	public void scopes(ActorMachine actorMachine, PrintWriter writer) {
		for (Scope s : actorMachine.getScopes()) {
			for (VarDecl decl : s.getDeclarations()) {
				String d = e().scopeVarDecl(decl);
				if (d != null)
					writer.print(d);
			}
		}
		int node = e().index(e().node(actorMachine));
		for (Scope s : actorMachine.getScopes()) {
			int index = e().index(s);
			writer.println("static void init_n" + node + "s" + index + "(void) {");
			for (LocalVarDecl decl : s.getDeclarations())
				if (decl.getInitialValue() != null && !e().scopeDeclIsConst(decl)) {
					String simpleExpression = e().simpleExpression(decl.getInitialValue());
					if (simpleExpression != null) {
						writer.println(e().variableName(decl) + " = " + simpleExpression + ";");
					} else {
						writer.print(e().scopeVarInit(decl.getInitialValue(), decl));
					}
				}
			writer.println("}");
		}
	}

	public boolean scopeDeclIsConst(LocalVarDecl decl) {
		return !decl.isAssignable() && e().isPersistent(e().variableScope(decl))
				&& e().simpleExpression(decl.getInitialValue()) != null;
	}

	public boolean isPersistent(Scope s) {
		Set<Scope> persistent = e().persistentScopes(s);
		return persistent.contains(s);
	}

	public Scope variableScope(Scope s) {
		return s;
	}

	public Scope variableScope(Object o) {
		return null;
	}

	public String scopeVarDecl(LocalVarDecl decl) {
		StringBuilder result = new StringBuilder();
		String value = null;
		Expression initialValue = decl.getInitialValue();
		if (initialValue != null) {
			value = e().simpleExpression(initialValue);
			if (initialValue instanceof ExprLambda || initialValue instanceof ExprProc) {
				return null;
			}
		}
		boolean constant = e().scopeDeclIsConst(decl);
		String name = e().variableName(decl);
		if (constant) {
			result.append("#define ");
			result.append(name);
			result.append(" (");
			result.append(value);
			result.append(")\n");
			return result.toString();
		} else {
			result.append("static ");
			CType type = e().ctype(decl);
			String var = type.variableType(name);
			result.append(var);
			if (constant) {
				result.append(" = ").append(value);
			}
			result.append(";\n");
			return result.toString();
		}
	}

}