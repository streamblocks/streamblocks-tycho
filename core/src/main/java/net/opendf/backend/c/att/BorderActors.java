package net.opendf.backend.c.att;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javarag.Module;
import javarag.Procedural;
import javarag.Synthesized;
import net.opendf.backend.c.CType;
import net.opendf.ir.net.Connection;
import net.opendf.ir.net.Network;

public class BorderActors extends Module<BorderActors.Decls> {
	
	public interface Decls {

		@Procedural
		void inputActor(Connection conn, PrintWriter writer);

		@Procedural
		void outputActor(Connection conn, PrintWriter writer);

		@Synthesized
		List<Connection> borderConnections(Network net);

		@Procedural
		void borderActors(Network net, PrintWriter writer);

		@Synthesized
		public List<String> borderActorNames(Network net);

		String bufferName(Connection conn);

		CType ctype(Connection conn);

		String plainType(CType type);
	}

	public void inputActor(Connection conn, PrintWriter writer) {
		String name = e().bufferName(conn);
		String type = e().ctype(conn).plainType();
		writer.println("static FILE *input_file" + name + ";");
		writer.println("static _Bool input_to" + name + "(void) {");
		writer.println("	if (tokens" + name + " == size" + name + ") return false;");
		writer.println("	size_t start = (head" + name + " + tokens" + name + ") % size" + name + ";");
		writer.println("	size_t length = start < head" + name + " ? head" + name + " - start : size" + name
				+ " - start;");
		writer.println("	size_t count = fread(&buffer" + name + "[start], sizeof(" + type + "), length, input_file"
				+ name + ");");
		writer.println("	tokens" + name + " += count;");
		writer.println("	if (count < length) {");
		writer.println("		if (feof(input_file" + name + ")) {");
		writer.println("			return count > 0;");
		writer.println("		} else {");
		writer.println("			fprintf(stderr, \"IO error\\n\");");
		writer.println("			exit(1);");
		writer.println("		}");
		writer.println("	} else {");
		writer.println("		return true;");
		writer.println("	}");
		writer.println("}");
	}

	public void outputActor(Connection conn, PrintWriter writer) {
		String name = e().bufferName(conn);
		String type = e().ctype(conn).plainType();
		writer.println("static FILE *output_file" + name + ";");
		writer.println("static _Bool output_from" + name + "(void) {");
		writer.println("	if (tokens" + name + " == 0) return false;");
		writer.println("	size_t end_no_wrap = head" + name + " + tokens" + name + ";");
		writer.println("	size_t length = end_no_wrap > size" + name + " ? size" + name + " : end_no_wrap;");
		writer.println("	size_t count = fwrite(&buffer" + name + "[head" + name + "], sizeof(" + type
				+ "), length, output_file" + name + ");");
		writer.println("	tokens" + name + " -= count;");
		writer.println("	if (count < length) {");
		writer.println("		fprintf(stderr, \"IO error\\n\");");
		writer.println("		exit(1);");
		writer.println("	}");
		writer.println("	return true;");
		writer.println("}");
	}

	public List<Connection> borderConnections(Network net) {
		List<Connection> result = new ArrayList<>();
		for (Connection conn : net.getConnections()) {
			if (conn.getSrcNodeId() == null || conn.getDstNodeId() == null) {
				result.add(conn);
			}
		}
		return result;
	}

	public void borderActors(Network net, PrintWriter writer) {
		for (Connection conn : net.getConnections()) {
			if (conn.getSrcNodeId() == null) {
				e().inputActor(conn, writer);
			}
			if (conn.getDstNodeId() == null) {
				e().outputActor(conn, writer);
			}
		}
	}

	public List<String> borderActorNames(Network net) {
		List<String> names = new ArrayList<>();
		for (Connection conn : net.getConnections()) {
			if (conn.getSrcNodeId() == null) {
				names.add("input_to" + e().bufferName(conn));
			}
			if (conn.getDstNodeId() == null) {
				names.add("output_from" + e().bufferName(conn));
			}
		}
		return names;
	}

}
