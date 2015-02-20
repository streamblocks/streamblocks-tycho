package se.lth.cs.tycho.backend.c.att;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javarag.Module;
import javarag.Synthesized;
import se.lth.cs.tycho.backend.c.CArrayType;
import se.lth.cs.tycho.backend.c.CType;
import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.PortCondition;
import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.instance.net.ToolValueAttribute;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.expr.ExprIf;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtConsume;
import se.lth.cs.tycho.ir.stmt.StmtOutput;
import se.lth.cs.tycho.messages.util.Result;

public class Buffers extends Module<Buffers.Decls> {

	public interface Decls {
		@Synthesized
		String bufferFunctions(Network net);
		
		@Synthesized
		String bufferDecl(Connection conn);

		@Synthesized
		public String statement(Statement stmt);

		@Synthesized
		public String condition(Condition cond);

		@Synthesized
		public String varInit(Expression e, String name);

		@Synthesized
		int bufferSize(Connection conn);
		
		List<Connection> outgoingConnections(PortDecl port);

		Connection incomingConnection(PortDecl port);
		
		PortDecl portDeclaration(Port port);

		String bufferName(Connection conn);

		String simpleExpression(Expression value);

		String variableName(VarDecl decl);

		CType ctype(Connection conn);

		CType ctype(Expression expr);

		String tempVariableName(Object object);

		Result<VarDecl> variableDeclaration(Variable v);

		Optional<Object> constant(Expression e);

	}

	public int bufferSize(Connection conn) {
		ToolAttribute attribute = conn.getToolAttribute("buffer_size");
		if (attribute instanceof ToolValueAttribute) {
			ToolValueAttribute valueAttribute = (ToolValueAttribute) attribute;
			Expression value = valueAttribute.getValue();
			if (value instanceof ExprLiteral) {
				ExprLiteral literal = (ExprLiteral) value;
				if (literal.getKind() == ExprLiteral.Kind.Integer) {
					try {
						return Integer.parseInt(literal.getText());
					} catch (NumberFormatException e) {
						// ignore
					}
				}
			}
		}
		return 1024 * 8;
	}

	public String bufferDecl(Connection conn) {
		String name = e().bufferName(conn);
		int size = e().bufferSize(conn);
		CType elementType = e().ctype(conn);
		CType type = new CArrayType(elementType, "size" + name);
		return "// " + conn.getDstPort().getName() + "\n" +
				"#define size" + name + " " + size + "\n" +
				"static " + type.variableType("buffer" + name) + ";\n" +
				"static size_t head" + name + " = 0;\n" +
				"static size_t tokens" + name + " = 0;\n";
	}
	
	private String initIfExpr(ExprIf i, String var) {
		StringBuilder builder = new StringBuilder();
		builder.append("if (" + e().simpleExpression(i.getCondition()) + ") {\n");
		String thn = e().simpleExpression(i.getThenExpr());
		if (thn == null) {
			builder.append(e().varInit(i.getThenExpr(), var));
		} else {
			builder.append(var + " = " + thn + "\n");
		}
		builder.append("} else {\n");
		String els = e().simpleExpression(i.getElseExpr());
		if (els == null) {
			builder.append(e().varInit(i.getThenExpr(), var));
		} else {
			builder.append(var + " = " + els + "\n");
		}
		builder.append("}\n");
		return builder.toString();
	}
	
	public String bufferFunctions(Network net) {
		StringBuilder builder = new StringBuilder();
		builder.append("#define WRITE(id, value) buffer##id[((head##id) + tokens##id++) % (size##id)] = (value)\n");
		builder.append("#define PEEK(id, pos) (buffer##id[(head##id + pos) % size##id])\n");
		builder.append("#define CONSUME(id, n) tokens##id -= n; head##id = ((head##id + n) % (size##id))\n");
		builder.append("#define SPACE(id, n) (size##id - tokens##id >= n)\n");
		builder.append("#define TOKENS(id, n) (tokens##id >= n)");
		return builder.toString();
	}

	public String statement(StmtOutput output) {
		if (output.hasRepeat()) {
			StringBuilder result = new StringBuilder();
			for (Connection conn : e().outgoingConnections(e().portDeclaration(output.getPort()))) {
				String name = e().bufferName(conn);
				List<String> vars = new ArrayList<>();
				for (Expression e : output.getValues()) {
					if (e instanceof ExprVariable) {
						ExprVariable var = (ExprVariable) e;
						Result<VarDecl> decl = e().variableDeclaration(var.getVariable());
						vars.add(e().variableName(decl.get()));
					} else {
						String temp = e().tempVariableName(e);
						vars.add(temp);
						result.append(e().ctype(e).variableType(temp) + ";\n");
						String value = e().simpleExpression(e);
						if (e instanceof ExprIf) {
							result.append(initIfExpr((ExprIf) e, temp));
						} else if (value == null) {						
							result.append(e().varInit(e, temp));
						} else {
							result.append(temp + "=" + value + ";\n");
						}
					}
				}
				for (int i = 0; i < output.getRepeat(); i++) {
					for (String v : vars) {
						result.append(String.format("WRITE(%s, %s[%d]);\n", name, v, i));
					}
				}
			}
			return result.toString();
		} else {
			StringBuilder sb = new StringBuilder();
			for (Connection conn : e().outgoingConnections(e().portDeclaration(output.getPort()))) {
				String name = e().bufferName(conn);
				for (Expression value : output.getValues()) {
					String val = e().simpleExpression(value);
					sb.append("WRITE("+name+", "+val+");\n");
				}
			}
			return sb.toString();
		}
	}

	public String statement(StmtConsume consume) {
		String name = e().bufferName(e().incomingConnection(e().portDeclaration(consume.getPort())));
		int tokens = consume.getNumberOfTokens();
		return "CONSUME("+name+", "+tokens+");\n";
	}

	public String condition(PortCondition cond) {
		if (cond.isInputCondition()) {
			Connection conn = e().incomingConnection(e().portDeclaration(cond.getPortName()));
			String name = e().bufferName(conn);
			return "TOKENS(" + name + ", " + cond.N() + ")";
		} else {
			StringBuilder result = new StringBuilder();
			List<Connection> connections = e().outgoingConnections(e().portDeclaration(cond.getPortName()));
			if (connections.isEmpty()) {
				System.nanoTime();
			}
			boolean first = true;
			for (Connection conn : connections) {
				if (first) {
					first = false;
				} else {
					result.append(" && ");
				}
				String name = e().bufferName(conn);
				result.append("SPACE(" + name + ", " + cond.N() + ")");
			}
			return result.toString();
		}
	}

	public String varInit(ExprInput input, String name) {
		Connection conn = e().incomingConnection(e().portDeclaration(input.getPort()));
		String buffer = e().bufferName(conn);
		if (input.hasRepeat()) {
			StringBuilder result = new StringBuilder();
			final int offset = input.getOffset();
			final int repeat = input.getRepeat();
			final int pattern = input.getPatternLength();
			for (int i = 0; i < repeat; i++) {
				result.append(name + "[" + i + "] = PEEK(" + buffer + ", " + (i * pattern + offset) + ");\n");
			}
			return result.toString();
		} else {
			return name + " = PEEK(" + buffer + ", " + input.getOffset() + ");\n";
		}
	}

}
