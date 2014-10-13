package se.lth.cs.tycho.backend.c.att;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.Scope;
import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.ParDeclValue;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprProc;
import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;

public class Names extends Module<Names.Decls> {

	public interface Decls {
		@Synthesized
		String functionName(IRNode n);

		@Inherited
		Scope variableScope(VarDecl varDecl);

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

	public String functionName(VarDecl decl) {
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

	public String variableName(VarDecl decl) {
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
