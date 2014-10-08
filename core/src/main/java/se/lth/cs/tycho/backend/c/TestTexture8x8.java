package se.lth.cs.tycho.backend.c;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import se.lth.cs.tycho.backend.c.test.NetworkConstructor;
import se.lth.cs.tycho.backend.c.test.NodeReader;
import se.lth.cs.tycho.backend.c.test.SingleInstrucitonActorMachineReader;
import se.lth.cs.tycho.backend.c.test.Texture8x8NetworkConstructor;
import se.lth.cs.tycho.ir.net.Network;

public class TestTexture8x8 {

	public static void main(String[] args) throws FileNotFoundException {
		NetworkConstructor constr = new Texture8x8NetworkConstructor();
		NodeReader reader = new SingleInstrucitonActorMachineReader(Paths.get("..", "orc-apps", "RVC", "src"));
		Network network = constr.constructNetwork(reader);
		PrintWriter writer = new PrintWriter("texture8x8.c");
		Backend.generateCode(network, writer);
	}

}
