package se.lth.cs.tycho.backend.c.att;

import java.util.List;

import se.lth.cs.tycho.backend.c.CArrayType;
import se.lth.cs.tycho.backend.c.CType;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.net.Connection;
import se.lth.cs.tycho.ir.net.ToolAttribute;
import se.lth.cs.tycho.ir.net.ToolValueAttribute;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtConsume;
import se.lth.cs.tycho.ir.stmt.StmtOutput;
import javarag.Module;
import javarag.Synthesized;

public class Buffers extends Module<Buffers.Decls> {

	public interface Decls {
		@Synthesized
		String bufferDecl(Connection conn);

		@Synthesized
		public String statement(Statement stmt);

		@Synthesized
		public String condition(Condition cond);

		@Synthesized
		public String scopeVarInit(ExprInput input, VarDecl decl);

		@Synthesized
		int bufferSize(Connection conn);
		
		List<Connection> connections(Port port);

		Connection connection(Port port);

		String bufferName(Connection conn);

		String simpleExpression(Expression value);

		String variableName(VarDecl decl);

		CType ctype(Connection conn);

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

	public String statement(StmtOutput output) {
		if (output.hasRepeat()) {
			StringBuilder result = new StringBuilder();
			for (Connection conn : e().connections(output.getPort())) {
				String name = e().bufferName(conn);
				int j = 0;
				String format = "buffer%1$s[(head%1$s + tokens%1$s + %2$d) %% size%1$s] = %3$s[%4$d];\n";
				for (int i = 0; i < output.getRepeat(); i++) {
					for (Expression e : output.getValues()) {
						String value = e().simpleExpression(e);
						result.append(String.format(format, name, j, value, i));
						j++;
					}
				}
				result.append("tokens").append(name).append(" += ").append(j).append(";\n");
			}
			return result.toString();
		} else {
			StringBuilder sb = new StringBuilder();
			for (Connection conn : e().connections(output.getPort())) {
				String name = e().bufferName(conn);
				int index = 0;
				String pattern = "buffer%1$s[(head%1$s + tokens%1$s + %2$d) %% size%1$s] = %3$s;\n";
				for (Expression value : output.getValues()) {
					sb.append(String.format(pattern, name, index, e().simpleExpression(value)));
					index += 1;
				}
				sb.append("tokens" + name + " += " + output.getValues().size() + ";\n");
			}
			return sb.toString();
		}
	}

	public String statement(StmtConsume output) {
		String name = e().bufferName(e().connection(output.getPort()));
		int tokens = output.getNumberOfTokens();
		return "tokens" + name + " -= " + tokens + ";\n" +
				"head" + name + " = (head" + name + " + " + tokens + ") % size" + name + ";\n";
	}

	public String condition(PortCondition cond) {
		if (cond.isInputCondition()) {
			Connection conn = e().connection(cond.getPortName());
			String name = e().bufferName(conn);
			return "tokens" + name + " >= " + cond.N();
		} else {
			StringBuilder result = new StringBuilder();
			List<Connection> connections = e().connections(cond.getPortName());
			if (connections.isEmpty()) {
				System.nanoTime();
			}
			for (Connection conn : connections) {
				String name = e().bufferName(conn);
				result.append("(tokens").append(name).append(" + ")
						.append(cond.N()).append(" <= size").append(name).append(") && ");
			}
			result.append("true");
			return result.toString();
		}
	}

	public String scopeVarInit(ExprInput input, VarDecl decl) {
		Connection conn = e().connection(input.getPort());
		String buffer = e().bufferName(conn);
		String name = e().variableName(decl);
		if (input.hasRepeat()) {
			StringBuilder result = new StringBuilder();
			final int offset = input.getOffset();
			final int repeat = input.getRepeat();
			final int pattern = input.getPatternLength();
			for (int i = 0; i < repeat; i++) {
				result.append(name).append("[").append(i).append("] = buffer")
						.append(buffer).append("[(head").append(buffer).append(" + ").append(i * pattern + offset)
						.append(") % size").append(buffer).append("];\n");
			}
			return result.toString();
		} else {
			String index = null;
			int offset = input.getOffset();
			if (offset > 0) {
				index = "(head" + buffer + " + " + offset + ") % size" + buffer;
			} else {
				index = "head" + buffer;
			}
			return name + " = buffer" + buffer + "[" + index + "];\n";
		}
	}

}
