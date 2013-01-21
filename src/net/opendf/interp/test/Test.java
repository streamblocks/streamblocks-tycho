package net.opendf.interp.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.opendf.interp.preprocess.SimulatorPreprocessor;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.util.ControllerToGraphviz;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.common.PortName;
import net.opendf.parser.lth.CalParser;
import net.opendf.trans.caltoam.ActorToActorMachine;

public class Test {
	public static void main(String[] args) throws FileNotFoundException {
		File calFile = new File("/Users/gustav/Programmering/dataflow/examples/MPEG4_SP_Decoder/ACPred.cal");
		
		CalParser parser = new CalParser();
		Actor actor = parser.parse(calFile);
		
		ActorToActorMachine trans = new ActorToActorMachine();
		ActorMachine actorMachine = trans.translate(actor);
		
		ControllerToGraphviz.print(new PrintStream("controller.gv"), actorMachine, "Controller");
		
		Map<PortName, Integer> portMap = new HashMap<PortName, Integer>();
		portMap.put(new PortName("AC"), 0);
		portMap.put(new PortName("PTR"), 1);
		portMap.put(new PortName("START"), 2);
		portMap.put(new PortName("OUT"), 3);
		
		SimulatorPreprocessor v = new SimulatorPreprocessor();
		v.process(actorMachine, portMap);
	}
}
