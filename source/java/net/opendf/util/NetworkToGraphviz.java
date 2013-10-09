package net.opendf.util;

import java.io.PrintWriter;

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
		printWriter.println("digraph " + name + " {");
		for (Node node : net.getNodes()) {
			printWriter.println("  \"" + node.getName() + "\" [shape=circle,style=filled];");			
		}
		for(Connection con : net.getConnections()){
			String src = net.getNode(con.getSrcNodeId()).getName();
			String dst = net.getNode(con.getDstNodeId()).getName();
			printWriter.println("  \"" + src + "\" -> \"" + dst + "\";");			
		}
		printWriter.println("}");
		printWriter.flush();
	}

}
