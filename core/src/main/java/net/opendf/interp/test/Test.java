package net.opendf.interp.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.opendf.interp.Simulator;
import net.opendf.interp.BasicActorMachineSimulator;
import net.opendf.interp.BasicChannel;
import net.opendf.interp.BasicInterpreter;
import net.opendf.interp.Channel;
import net.opendf.interp.Environment;
import net.opendf.interp.BasicEnvironment;
import net.opendf.interp.values.BasicRef;
import net.opendf.interp.values.ConstRef;
import net.opendf.ir.entity.am.ActorMachine;
import net.opendf.ir.entity.cal.Actor;
import net.opendf.parser.lth.CalParser;

public class Test {
	public static void main(String[] args) throws FileNotFoundException {
		File calFile = new File("../dataflow/examples/Test/My.cal");
		try {
			System.out.println(calFile.getCanonicalPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// File calFile = new
		// File("../dataflow/examples/MPEG4_SP_Decoder/ACPred.cal");

		CalParser parser = new CalParser();
		Actor actor = (Actor) parser.parse(calFile, null, null).getEntity();

//		List<Decl> actorArgs = new ArrayList<Decl>();
		// actorArgs.add(varDecl("MAXW_IN_MB", lit(121)));
		// actorArgs.add(varDecl("MB_COORD_SZ", lit(8)));
		// actorArgs.add(varDecl("SAMPLE_SZ", lit(13)));
//		Scope argScope = new Scope(ScopeKind.Persistent, actorArgs);

		ActorMachine actorMachine = BasicActorMachineSimulator.prepareActor(actor, null);

//		XMLWriter doc = new XMLWriter(actorMachine);		doc.print();

		Channel channelSource1 = new BasicChannel(3);
		Channel channelSource2 = new BasicChannel(3);
		Channel channelResult = new BasicChannel(30);
		Channel.InputEnd[] sinkChannelInputEnd = { channelResult.getInputEnd() };
		Channel.OutputEnd sinkChannelOutputEnd = channelResult.createOutputEnd();
		Channel.OutputEnd[] sourceChannelOutputEnd = { channelSource1.createOutputEnd(), channelSource2.createOutputEnd() };
		// initial tokens
		channelSource1.getInputEnd().write(ConstRef.of(0));
		channelSource1.getInputEnd().write(ConstRef.of(1));
		channelSource1.getInputEnd().write(ConstRef.of(2));
		channelSource2.getInputEnd().write(ConstRef.of(10));
		channelSource2.getInputEnd().write(ConstRef.of(11));
		channelSource2.getInputEnd().write(ConstRef.of(12));

		int stackSize = 100;
		Environment env = new BasicEnvironment(sinkChannelInputEnd, sourceChannelOutputEnd, actorMachine);
		Simulator runner = new BasicActorMachineSimulator(actorMachine, env, new BasicInterpreter(stackSize));

		while(runner.step()) { ; }
		
		System.out.println("Scopes: ");
		StringBuffer sb = new StringBuffer();
		runner.scopesToString(sb);
		System.out.println(sb);
		System.out.println("Output: ");
		String sep = "";
		int i = 0;
		while (sinkChannelOutputEnd.tokens(i+1)) {
			BasicRef r = new BasicRef();
			sinkChannelOutputEnd.peek(i++, r);
			System.out.print(sep + r);
			sep = ", ";
		}
		System.out.println();
	}
}
