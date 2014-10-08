package net.opendf.backend.c;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import net.opendf.backend.c.test.NodeReader;
import net.opendf.backend.c.test.SingleInstrucitonActorMachineReader;
import net.opendf.ir.net.Network;


public class TestBackend {
	private static NodeReader reader = new SingleInstrucitonActorMachineReader(Paths.get("..", "orc-apps", "RVC", "src"));

	private static Network testNetwork(File file) {
		return NetworkFunctions.fromSingleNode(reader.fromFile(file), file.getName());
	}

	public static void main(String[] args) throws IOException {
		for (String arg : args) {
			File inFile = new File(arg);
			File outFile = new File(inFile.getName() + ".c");
			try {
				Network network = testNetwork(inFile);
				PrintWriter out = new PrintWriter(new java.io.FileWriter(outFile), true);
				long startNanos = System.nanoTime();
				Backend.generateCode(network, out);
				long timeMillis = (System.nanoTime() - startNanos) / 1000000;
				System.out.println("TIME " + timeMillis + " ms");
			} catch (Throwable e) {
				System.err.println("ERROR IN " + inFile.getName() + ":");
				e.printStackTrace();
				//System.err.println(e.getMessage());
				//System.err.println();
			}
			System.out.println("GENERATED FILE " + outFile.getPath());
		}
	}

}
