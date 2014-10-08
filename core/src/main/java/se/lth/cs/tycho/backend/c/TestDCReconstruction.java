package se.lth.cs.tycho.backend.c;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import se.lth.cs.tycho.backend.c.test.DCReconstructionNetworkConstructor;
import se.lth.cs.tycho.backend.c.test.NetworkConstructor;
import se.lth.cs.tycho.backend.c.test.NodeReader;
import se.lth.cs.tycho.backend.c.test.SingleInstrucitonActorMachineReader;
import se.lth.cs.tycho.ir.net.Network;
import se.lth.cs.tycho.transform.compose.Composer;


public class TestDCReconstruction {


	public static void main(String[] args) throws FileNotFoundException {
		NetworkConstructor constr = new DCReconstructionNetworkConstructor();
		NodeReader reader = new SingleInstrucitonActorMachineReader(Paths.get("..", "orc-apps-am", "RVC", "src"));
		Network network = constr.constructNetwork(reader);
		Composer composer = new Composer();
		for (int i = 0; i < 100; i++) {
			Network composition = composer.composeNetwork(network, "DCReconstruction");
			String file = String.format("dcr-random/src/dcrecon_comp-%02d.c", i);
			PrintWriter compWriter = new PrintWriter(file);
			Backend.generateCode(composition, compWriter);
		}
		PrintWriter schedWriter = new PrintWriter("dcrecon_sched.c");
		Backend.generateCode(network, schedWriter);
	}
}
