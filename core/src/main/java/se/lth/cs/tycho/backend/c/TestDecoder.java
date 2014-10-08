package se.lth.cs.tycho.backend.c;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import se.lth.cs.tycho.backend.c.test.DecoderNetworkConstructor;
import se.lth.cs.tycho.backend.c.test.NetworkConstructor;
import se.lth.cs.tycho.backend.c.test.NodeReader;
import se.lth.cs.tycho.backend.c.test.SingleInstrucitonActorMachineReader;
import se.lth.cs.tycho.ir.net.Network;

public class TestDecoder {

	public static void main(String[] args) throws FileNotFoundException {
		NetworkConstructor constr = new DecoderNetworkConstructor();
		NodeReader reader = new SingleInstrucitonActorMachineReader(Paths.get("..", "orc-apps-am", "RVC", "src"));
		Network network = constr.constructNetwork(reader);
		PrintWriter writer = new PrintWriter("decoder.c");
		Backend.generateCode(network, writer);
	}

}
