package net.opendf.backend.c.att;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javarag.Module;
import javarag.Procedural;
import javarag.Synthesized;
import net.opendf.ir.net.Connection;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;

public class TranslationUnit extends Module<TranslationUnit.Decls> {

	public interface Decls {

		@Procedural
		public void translate(Network network, PrintWriter writer);

		@Procedural
		public void includes(Network network, PrintWriter writer);

		@Synthesized
		public String bufferDecl(Connection conn);

		@Procedural
		public void mainFunction(Network network, PrintWriter writer);

		@Procedural
		public void actors(Network network, PrintWriter writer);

		@Procedural
		public void bufferDecls(Network network, PrintWriter writer);

		public void borderActors(Network network, PrintWriter writer);

		public void translateNode(Object content, PrintWriter writer);

		public List<String> borderActorNames(Network network);

		public String bufferName(Connection conn);

	}

	public void bufferDecls(Network network, PrintWriter writer) {
		for (Connection conn : network.getConnections()) {
			writer.println(e().bufferDecl(conn));
		}

	}

	public void actors(Network network, PrintWriter writer) {
		for (Node n : network.getNodes()) {
			e().translateNode(n.getContent(), writer);
		}
	}

	public void mainFunction(Network network, PrintWriter writer) {
		List<String> inputFileVariables = new ArrayList<>();
		List<String> outputFileVariables = new ArrayList<>();
		StringBuilder argList = new StringBuilder();
		int numArgs = 1;
		for (Connection conn : network.getConnections()) {
			if (conn.getSrcNodeId() == null) {
				argList.append(" <input ");
				argList.append(conn.getSrcPort().getName());
				argList.append(">");
				numArgs += 1;
				inputFileVariables.add("input_file" + e().bufferName(conn));
			}
		}
		for (Connection conn : network.getConnections()) {
			if (conn.getDstNodeId() == null) {
				argList.append(" <output ");
				argList.append(conn.getDstPort().getName());
				argList.append(">");
				numArgs += 1;
				outputFileVariables.add("output_file" + e().bufferName(conn));
			}
		}

		writer.println("int main(int argc, char* argv[]) {");
		writer.println("	if (argc != " + numArgs + ") {");
		writer.println("		fprintf(stderr, \"Usage: %s" + argList.toString() + "\\n\", argv[0]);");
		writer.println("		exit(1);");
		writer.println("	}");

		int arg = 1;
		for (String variable : inputFileVariables) {
			writer.println("	" + variable + " = fopen(argv[" + arg + "], \"r\");");
			writer.println("	if (" + variable + " == NULL) {");
			writer.println("		fprintf(stderr, \"Error opening %s\\n\", argv[" + arg + "]);");
			writer.println("		exit(1);");
			writer.println("	}");
			arg += 1;
		}
		for (String variable : outputFileVariables) {
			writer.println("	" + variable + " = fopen(argv[" + arg + "], \"w\");");
			writer.println("	if (" + variable + " == NULL) {");
			writer.println("		fprintf(stderr, \"Error opening %s\\n\", argv[" + arg + "]);");
			writer.println("		exit(1);");
			writer.println("	}");
			arg += 1;
		}

		writer.println("	clock_t io_time = 0, actor_time = 0, t0, t1;");

		writer.println("	_Bool progress = true;");
		writer.println("	t0 = clock();");
		writer.println("	while (progress) {");
		writer.println("		progress = false;");
		for (String actor : e().borderActorNames(network)) {
			writer.println("		progress |= " + actor + "();");
		}
		writer.println("		t1 = clock();");
		writer.println("		io_time += t1 - t0;");
		writer.println("		t0 = t1;");
		for (int i = 0; i < network.getNodes().size(); i++) {
			writer.println("		progress |= actor_n" + i + "();");
		}
		writer.println("		t1 = clock();");
		writer.println("		actor_time += t1 - t0;");
		writer.println("		t0 = t1;");
		writer.println("	}");

		writer.println("	fprintf(stdout, \"Time spent in I/0 actors: %lu ns\\n\", io_time*1000*1000*1000 / CLOCKS_PER_SEC);");
		writer.println("	fprintf(stdout, \"Time spent in real actors: %lu ns\\n\", actor_time*1000*1000*1000 / CLOCKS_PER_SEC);");

		for (Connection conn : network.getConnections()) {
			String name = e().bufferName(conn);
			writer.println("	if (tokens" + name + " != 0) fprintf(stderr, \"WARNING: buffer" + name
					+ " contains %zu token(s).\\n\", tokens" + name + ");");
		}
		writer.println("	fprintf(stdout, \"DONE\\n\");");
		writer.println("}");

	}

	public void includes(Network network, PrintWriter writer) {
		writer.println("#include <stdio.h>");
		writer.println("#include <stdint.h>");
		writer.println("#include <stdbool.h>");
		writer.println("#include <stdlib.h>");
		writer.println("#include <time.h>");
		writer.println("#include \"mpeg_constants.h\"");
		writer.println();
		writer.println("#ifdef TRACE_ALL");
		writer.println("#define TRACE_STATE");
		writer.println("#define TRACE_INSTRUCTION");
		writer.println("#endif");
		writer.println();
		writer.println("#ifdef TRACE_INSTRUCTION");
		writer.println("#define TRACE_CALL");
		writer.println("#define TRACE_TEST");
		writer.println("#define TRACE_WAIT");
		writer.println("#endif");
		writer.println();
		writer.println("#ifdef TRACE_STATE");
		writer.println("#define AM_TRACE_STATE(s) printf(\"state %d\\n\", s)");
		writer.println("#else");
		writer.println("#define AM_TRACE_STATE(s)");
		writer.println("#endif");
		writer.println();
		writer.println("#ifdef TRACE_CALL");
		writer.println("#define AM_TRACE_CALL(s) printf(\"call %d\\n\", s)");
		writer.println("#else");
		writer.println("#define AM_TRACE_CALL(s)");
		writer.println("#endif");
		writer.println();
		writer.println("#ifdef TRACE_TEST");
		writer.println("#define AM_TRACE_TEST(s) printf(\"test %d\\n\", s)");
		writer.println("#else");
		writer.println("#define AM_TRACE_TEST(s)");
		writer.println("#endif");
		writer.println();
		writer.println("#ifdef TRACE_WAIT");
		writer.println("#define AM_TRACE_WAIT() printf(\"wait\\n\")");
		writer.println("#else");
		writer.println("#define AM_TRACE_WAIT()");
		writer.println("#endif");
	}

	public void translate(Network network, PrintWriter writer) {
		e().includes(network, writer);
		e().bufferDecls(network, writer);
		e().actors(network, writer);
		e().borderActors(network, writer);
		e().mainFunction(network, writer);
		writer.println();
	}

}
