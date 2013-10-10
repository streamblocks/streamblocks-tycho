package net.opendf.util;

import java.io.PrintWriter;

import net.opendf.ir.common.PortDecl;
import net.opendf.ir.net.Connection;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;

public class NetworkToGraphviz {
	private PrintWriter printWriter;
	private String name;

	public static void print(Network net, String name, PrintWriter pw) {
		new NetworkToGraphviz(net, name, pw).print(net);
	}

	public NetworkToGraphviz(Network net, String name, PrintWriter pw){
		this.name = name;
		this.printWriter = pw;
	}

	private void print(Network net) {
		printWriter.println("digraph \"" + name + "\" {");
		printBody(net);
		printWriter.println("}");
		printWriter.flush();
	}
	
	private void printSubgraph(Network net, String label) {
		printWriter.println("subgraph \"cluster" + label + "\" {");
		printWriter.println("  label = \"" + label + "\";");
		printBody(net);
		printWriter.println(" }");
		printWriter.flush();
	}
	
	private void printBody(Network net) {

		for (Node node : net.getNodes()) {
			if(node.getContent() instanceof Network){
				printSubgraph((Network)node.getContent(), node.getName());
				printWriter.println("  \"" + node.getIdentifier() + "\" [ label=\"" + node.getName() + "\", shape=circle];");
			} else {
				printWriter.println("  \"" + node.getIdentifier() + "\" [ label=\"" + node.getName() + "\", shape=circle];");
			}
		}
		for(PortDecl p : net.getInputPorts()){
			printWriter.println("  \"" + p.getName() + "\" [ label=\"" + p.getName() + "\", shape=box, style=filled];");
		}
		for(PortDecl p : net.getOutputPorts()){
			printWriter.println("  \"" + p.getName() + "\" [ label=\"" + p.getName() + "\", shape=box, style=filled];");
		}
		for(Connection con : net.getConnections()){
			String src;
			if(con.getSrcNodeId() != null){
				src = con.getSrcNodeId().toString();
			} else {
				src = con.getSrcPort().getName();
			}

			String dst;
			if(con.getDstNodeId() != null){
				dst = con.getDstNodeId().toString();
			} else {
				dst = con.getDstPort().getName();
			}
			printWriter.println("  \"" + src + "\" -> \"" + dst + "\";");			
		}
	}
}
