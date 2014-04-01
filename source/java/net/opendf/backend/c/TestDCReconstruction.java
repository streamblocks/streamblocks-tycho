package net.opendf.backend.c;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import net.opendf.backend.c.test.DCReconstructionNetworkConstructor;
import net.opendf.backend.c.test.NetworkConstructor;
import net.opendf.backend.c.test.NodeReader;
import net.opendf.backend.c.test.SingleInstrucitonActorMachineReader;
import net.opendf.ir.net.Network;
import net.opendf.transform.compose.Composer;


public class TestDCReconstruction {


	public static void main(String[] args) throws FileNotFoundException {
		NetworkConstructor constr = new DCReconstructionNetworkConstructor();
		NodeReader reader = new SingleInstrucitonActorMachineReader(Paths.get("..", "orc-apps-am", "RVC", "src"));
		Network network = constr.constructNetwork(reader);
		Composer composer = new Composer();
		Network composition = composer.composeNetwork(network, "DCReconstruction");
		PrintWriter compWriter = new PrintWriter("dcrecon_comp.c");
		Backend.generateCode(composition, compWriter);
		PrintWriter schedWriter = new PrintWriter("dcrecon_sched.c");
		Backend.generateCode(network, schedWriter);
	}
}
