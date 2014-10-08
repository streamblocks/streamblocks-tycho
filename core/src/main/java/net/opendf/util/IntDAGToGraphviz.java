package net.opendf.util;

import java.io.PrintWriter;

import net.opendf.util.DAG.Arc;

public class IntDAGToGraphviz {
	
	public static void print(PrintWriter pw, DAG dag, String name) {
		pw.println("digraph " + name + " {");
		for (int node = 0; node < dag.numberOfNodes(); node++) {
			pw.println("\t" + node + ";");
		}
		for (Arc e : dag.arcs()) {
			pw.print("\t" + e.getSource() + " -> " + e.getDestination() + ";");
		}
		pw.println("}");
		pw.flush();
	}

}
