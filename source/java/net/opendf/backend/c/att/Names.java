package net.opendf.backend.c.att;

import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.IRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Scope;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprLambda;
import net.opendf.ir.common.ExprProc;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.Variable;
import net.opendf.ir.net.Connection;
import net.opendf.ir.net.Node;

public class Names extends Module<Names.Decls> {

	public interface Decls {
		@Synthesized
		String functionName(IRNode n);

		@Inherited
		Scope variableScope(DeclVar varDecl);

		@Synthesized
		String variableName(IRNode var);

		@Synthesized
		String bufferName(Connection c);

		@Synthesized
		public String tempVariableName(Object o);

		int index(Object o);

		IRNode declaration(Variable var);

		IRNode parent(IRNode o);

		ActorMachine actorMachine(IRNode n);

		Node node(ActorMachine am);

	}

	public String functionName(DeclVar decl) {
		return e().variableName(decl);
	}

	public String functionName(ExprLambda lambda) {
		return e().functionName(e().parent(lambda));
	}

	public String functionName(ExprProc proc) {
		return e().functionName(e().parent(proc));
	}

	public Scope variableScope(Scope s) {
		return s;
	}

	public Scope variableScope(Object o) {
		return null;
	}

	public String variableName(DeclVar decl) {
		Scope s = e().variableScope(decl);
		if (s == null) {
			return decl.getName() + "_";
		} else {
			ActorMachine am = e().actorMachine(s);
			Node node = e().node(am);
			int n = e().index(node);
			int v = e().index(s);
			return decl.getName() + "_n" + n + "v" + v;
		}
	}

	public String variableName(ParDeclValue decl) {
		return decl.getName() + "_";
	}

	public String variableName(Variable var) {
		IRNode declaration = e().declaration(var);
		if (declaration != null) {
			return e().variableName(declaration);
		} else {
			return var.getName();
		}
	}

	public String bufferName(Connection c) {
		return "_b" + e().index(c);
	}

	private int tempVariableNumber = 0;

	public String tempVariableName(Object o) {
		return "temp_t" + (tempVariableNumber++);
	}
}
