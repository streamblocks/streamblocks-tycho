package net.opendf.backend.c;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import net.opendf.backend.c.test.Texture8x8NetworkConstructor;
import net.opendf.backend.c.test.NetworkConstructor;
import net.opendf.backend.c.test.NodeReader;
import net.opendf.backend.c.test.SingleInstrucitonActorMachineReader;
import net.opendf.ir.net.Network;

public class TestTexture8x8 {

	public static void main(String[] args) throws FileNotFoundException {
		NetworkConstructor constr = new Texture8x8NetworkConstructor();
		NodeReader reader = new SingleInstrucitonActorMachineReader(Paths.get("..", "orc-apps", "RVC", "src"));
		Network network = constr.constructNetwork(reader);
		PrintWriter writer = new PrintWriter("texture8x8.c");
		Backend.generateCode(network, writer);
	}

}
