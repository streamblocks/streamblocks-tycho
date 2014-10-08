package se.lth.cs.tycho.backend.c;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import se.lth.cs.tycho.backend.c.test.NetworkConstructor;
import se.lth.cs.tycho.backend.c.test.NodeReader;
import se.lth.cs.tycho.backend.c.test.ParserNetworkConstructor;
import se.lth.cs.tycho.backend.c.test.SingleInstrucitonActorMachineReader;
import se.lth.cs.tycho.ir.net.Network;


public class TestParser {

	public static void main(String[] args) throws FileNotFoundException {
		NetworkConstructor constr = new ParserNetworkConstructor();
		NodeReader reader = new SingleInstrucitonActorMachineReader(Paths.get("..", "orc-apps", "RVC", "src"));
		Network network = constr.constructNetwork(reader);
		PrintWriter writer = new PrintWriter("parser.c");
		Backend.generateCode(network, writer);
	}

}
