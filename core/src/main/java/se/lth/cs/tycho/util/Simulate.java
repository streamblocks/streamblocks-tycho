package se.lth.cs.tycho.util;

import se.lth.cs.tycho.errorhandling.ErrorModule;
import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.interp.BasicActorMachineSimulator;
import se.lth.cs.tycho.interp.BasicChannel;
import se.lth.cs.tycho.interp.BasicEnvironment;
import se.lth.cs.tycho.interp.BasicInterpreter;
import se.lth.cs.tycho.interp.BasicNetworkSimulator;
import se.lth.cs.tycho.interp.Channel;
import se.lth.cs.tycho.interp.Environment;
import se.lth.cs.tycho.interp.Simulator;
import se.lth.cs.tycho.interp.exception.CALCompiletimeException;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.util.DeclLoader;

public class Simulate {
	static final String usage = "Correct use: java se.lth.cs.tycho.util.Simulate path entityName" +
			"\nThe entityName should not include the file extension, i.e. use 'Top', not 'Top.nl'";

	public static void main(String[] args){
		int index = 0;
		int defaultStackSize = 100;
		int defaultChannelSize = 10;

		if(index+2 != args.length){
			System.err.println(usage);				
			return;
		}

		String path = args[index++];
		String entityName = args[index++];
		DeclLoader declLoader= new DeclLoader(path);
		Simulator simulator;
		try{
			Decl e = declLoader.getDecl(entityName);
			if(e instanceof NlNetwork){
				NlNetwork netDef = (NlNetwork)e;
				Network net = BasicNetworkSimulator.prepareNetworkDefinition(netDef, declLoader);
				simulator = new BasicNetworkSimulator(net, defaultChannelSize, defaultStackSize);
			} else if(e instanceof CalActor){
				CalActor calActor = (CalActor)e;
				ActorMachine actorMachine = BasicActorMachineSimulator.prepareActor(calActor, declLoader);

				Channel.OutputEnd[] sourceChannelOutputEnd = new Channel.OutputEnd[calActor.getInputPorts().size()];
				for(int i=0; i<sourceChannelOutputEnd.length; i++){
					Channel channel = new BasicChannel(0);  // no one is writing to this channel, so set size to 0
					sourceChannelOutputEnd[i] = channel.createOutputEnd();
				}
				Channel.InputEnd[] sinkChannelInputEnd = new Channel.InputEnd[calActor.getOutputPorts().size()];
				for(int i=0; i<sinkChannelInputEnd.length; i++){
					Channel channel = new BasicChannel(defaultChannelSize);
					sinkChannelInputEnd[i] = channel.getInputEnd();
				}
				Environment env = new BasicEnvironment(sinkChannelInputEnd, sourceChannelOutputEnd, actorMachine);
				simulator = new BasicActorMachineSimulator(actorMachine, env, new BasicInterpreter(defaultStackSize));
			} else {
				System.err.println(entityName + " is not a network or calActor.");
				return;
			}
		} catch(CALCompiletimeException error){
			ErrorModule em = error.getErrorModule();
			if(em != null){
				em.printErrors();
			} else {
				System.err.println("ERROR: " + error.getMessage());
			}
			System.err.println("Problems occured while compiling, aborting before simulation.");
			return;
		}

		// run the simulation
		try{
			while(simulator.step()){ 			}
		} catch(Exception error){
			StringBuffer sb = new StringBuffer();
			simulator.scopesToString(sb);
			System.out.println(sb);
			throw error;
		}

		// PRINT RESULT
		StringBuffer sb = new StringBuffer();
		simulator.scopesToString(sb);
		System.out.println(sb);
	}
}
