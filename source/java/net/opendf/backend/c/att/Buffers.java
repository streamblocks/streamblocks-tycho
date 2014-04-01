package net.opendf.backend.c.att;

import java.util.List;

import javarag.Module;
import javarag.Synthesized;
import net.opendf.backend.c.CArrayType;
import net.opendf.backend.c.CType;
import net.opendf.ir.am.PortCondition;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprInput;
import net.opendf.ir.common.ExprLiteral;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.Port;
import net.opendf.ir.common.StmtConsume;
import net.opendf.ir.common.StmtOutput;
import net.opendf.ir.net.Connection;
import net.opendf.ir.net.ToolAttribute;
import net.opendf.ir.net.ToolValueAttribute;

public class Buffers extends Module<Buffers.Required> {

	public interface Required {

		List<Connection> connections(Port port);

		Connection connection(Port port);

		String bufferName(Connection conn);

		int bufferSize(Connection conn);

		String simpleExpression(Expression value);

		String variableName(DeclVar decl);

		CType ctype(Connection conn);

	}

	@Synthesized
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
		return 1024*8;
	}

	@Synthesized
	public String bufferDecl(Connection conn) {
		String name = get().bufferName(conn);
		int size = get().bufferSize(conn);
		CType elementType = get().ctype(conn);
		CType type = new CArrayType(elementType, "size"+name);
		return "// " + conn.getDstPort().getName() + "\n" +
				"#define size" + name + " " + size + "\n" + 
				"static "+type.variableType("buffer"+name)+";\n" +
				"static size_t head" + name + " = 0;\n" +
				"static size_t tokens" + name + " = 0;\n";
	}

	@Synthesized
	public String statement(StmtOutput output) {
		if (output.hasRepeat()) {
			StringBuilder result = new StringBuilder();
			for (Connection conn : get().connections(output.getPort())) {
				String name = get().bufferName(conn);
				int j = 0;
				String format = "buffer%1$s[(head%1$s + tokens%1$s + %2$d) %% size%1$s] = %3$s[%4$d];\n";
				for (int i = 0; i < output.getRepeat(); i++) {
					for (Expression e : output.getValues()) {
						String value = get().simpleExpression(e);
						result.append(String.format(format, name, j, value, i));
						j++;
					}
				}
				result.append("tokens").append(name).append(" += ").append(j).append(";\n");
			}
			return result.toString();
		} else {
			StringBuilder sb = new StringBuilder();
			for (Connection conn : get().connections(output.getPort())) {
				String name = get().bufferName(conn);
				int index = 0;
				String pattern = "buffer%1$s[(head%1$s + tokens%1$s + %2$d) %% size%1$s] = %3$s;\n";
				for (Expression value : output.getValues()) {
					sb.append(String.format(pattern, name, index, get().simpleExpression(value)));
					index += 1;
				}
				sb.append("tokens" + name + " += " + output.getValues().size() + ";\n");
			}
			return sb.toString();
		}
	}

	@Synthesized
	public String statement(StmtConsume output) {
		String name = get().bufferName(get().connection(output.getPort()));
		int tokens = output.getNumberOfTokens();
		return "tokens" + name + " -= " + tokens + ";\n" +
			"head" + name + " = (head" + name + " + " + tokens + ") % size" + name + ";\n";
	}

	@Synthesized
	public String condition(PortCondition cond) {
		if (cond.isInputCondition()) {
			Connection conn = get().connection(cond.getPortName());
			String name = get().bufferName(conn);
			return "tokens" + name + " >= " + cond.N();
		} else {
			StringBuilder result = new StringBuilder();
			List<Connection> connections = get().connections(cond.getPortName());
			if (connections.isEmpty()) {
				System.nanoTime();
			}
			for (Connection conn : connections) {
				String name = get().bufferName(conn);
				result.append("(tokens").append(name).append(" + ")
					.append(cond.N()).append(" <= size").append(name).append(") && ");
			}
			result.append("true");
			return result.toString();
		}
	}

	@Synthesized
	public String scopeVarInit(ExprInput input, DeclVar decl) {
		Connection conn = get().connection(input.getPort());
		String buffer = get().bufferName(conn);
		String name = get().variableName(decl);
		if (input.hasRepeat()) {
			StringBuilder result = new StringBuilder();
			final int offset = input.getOffset();
			final int repeat = input.getRepeat();
			final int pattern = input.getPatternLength();
			for (int i = 0; i < repeat; i++) {
				result.append(name).append("[").append(i).append("] = buffer")
					.append(buffer).append("[(head").append(buffer).append(" + ").append(i*pattern+offset)
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
