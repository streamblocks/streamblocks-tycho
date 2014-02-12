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
import net.opendf.ir.common.Port;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.common.Variable;
import net.opendf.ir.net.Connection;
import net.opendf.ir.net.Node;

public class Names extends Module<Names.Required> {

	public interface Required {

		Scope variableScope(DeclVar varDecl);

		int index(Object o);

		String variableName(IRNode declaration);

		IRNode declaration(Variable var);

		PortDecl declaration(Port p);

		String bufferName(PortDecl decl);

		Connection connection(PortDecl d);

		String bufferName(Connection connection);

		IRNode parent(IRNode o);

		String functionName(IRNode n);

		ActorMachine actorMachine(IRNode n);
		
		Node node(ActorMachine am);

	}

	@Synthesized
	public String functionName(DeclVar decl) {
		return get().variableName(decl);
	}

	@Synthesized
	public String functionName(ExprLambda lambda) {
		return get().functionName(get().parent(lambda));
	}

	@Synthesized
	public String functionName(ExprProc proc) {
		return get().functionName(get().parent(proc));
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
	public String variableName(DeclVar decl) {
		Scope s = get().variableScope(decl);
		if (s == null) {
			return decl.getName() + "_";
		} else {
			ActorMachine am = get().actorMachine(s);
			Node node = get().node(am);
			int n = get().index(node);
			int v = get().index(s);
			return decl.getName()+"_n"+n+"v"+v;
		}
	}
	
	@Synthesized
	public String variableName(ParDeclValue decl) {
		return decl.getName() + "_";
	}

	@Synthesized
	public String variableName(Variable var) {
		IRNode declaration = get().declaration(var);
		if (declaration != null) {
			return get().variableName(declaration);
		} else {
			return var.getName();
		}
	}
	
	@Synthesized
	public String bufferName(Connection c) {
		return "_b" + get().index(c);
	}

	private int tempVariableNumber = 0;
	@Synthesized
	public String tempVariableName(Object o) {
		return "temp_t"+(tempVariableNumber++);
	}
}
