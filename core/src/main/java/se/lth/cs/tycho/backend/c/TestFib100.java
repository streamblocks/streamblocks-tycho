package se.lth.cs.tycho.backend.c;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import se.lth.cs.tycho.backend.c.test.Fib100NetworkConstructor;
import se.lth.cs.tycho.backend.c.test.NetworkConstructor;
import se.lth.cs.tycho.backend.c.test.NodeReader;
import se.lth.cs.tycho.backend.c.test.SingleInstrucitonActorMachineReader;
import se.lth.cs.tycho.ir.net.Network;
import se.lth.cs.tycho.transform.compose.Composer;

public class TestFib100 {

	public static void main(String[] args) throws FileNotFoundException {
		NetworkConstructor constr = new Fib100NetworkConstructor();
		NodeReader reader = new SingleInstrucitonActorMachineReader(Paths.get("..", "test_comp"));
		Network network = constr.constructNetwork(reader);
		PrintWriter writer = new PrintWriter("fib.c");
		Backend.generateCode(network, writer);
		Network composition = new Composer().composeNetwork(network, "fib");
		Backend.generateCode(composition, new PrintWriter("fib_comp.c"));
	}

}
