package net.opendf.interp.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.opendf.interp.Simulator;
import net.opendf.interp.BasicSimulator;
import net.opendf.interp.BasicChannel;
import net.opendf.interp.BasicInterpreter;
import net.opendf.interp.BasicStack;
import net.opendf.interp.Channel;
import net.opendf.interp.Environment;
import net.opendf.interp.BasicEnvironment;
import net.opendf.interp.values.BasicRef;
import net.opendf.interp.values.ConstRef;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.Expression;
import net.opendf.parser.lth.CalParser;
import net.opendf.transform.caltoam.ActorToActorMachine;
import net.opendf.transform.caltoam.ActorStates.State;
import net.opendf.transform.filter.InstructionFilterFactory;
import net.opendf.transform.filter.PrioritizeCallInstructions;
import net.opendf.transform.filter.SelectRandomInstruction;
import net.opendf.transform.operators.ActorOpTransformer;

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
		Actor actor = parser.parse(calFile);
		// replace BinOp and UnaryOp in all expressions with function calls
		ActorOpTransformer transformer = new ActorOpTransformer();
		actor = transformer.transformActor(actor);

		List<Decl> actorArgs = new ArrayList<Decl>();
		// actorArgs.add(varDecl("MAXW_IN_MB", lit(121)));
		// actorArgs.add(varDecl("MB_COORD_SZ", lit(8)));
		// actorArgs.add(varDecl("SAMPLE_SZ", lit(13)));
//		Scope argScope = new Scope(ScopeKind.Persistent, actorArgs);

		// translate the actor to an actor machine
		List<InstructionFilterFactory<State>> instructionFilters = new ArrayList<InstructionFilterFactory<State>>();
		InstructionFilterFactory<State> f = PrioritizeCallInstructions.getFactory();
		instructionFilters.add(f);
		f = SelectRandomInstruction.getFactory();
		instructionFilters.add(f);
		ActorToActorMachine trans = new ActorToActorMachine(instructionFilters);
		ActorMachine actorMachine = trans.translate(actor);

//		XMLWriter doc = new XMLWriter(actorMachine);		doc.print();

		
		// net.opendf.ir.am.util.ControllerToGraphviz.print(new
		// PrintStream("controller.gv"), actorMachine, "Controller");

/*		Map<PortName, Integer> portMap = new HashMap<PortName, Integer>();
		{
			int i = 0;
			for (PortDecl in : actorMachine.getInputPorts().getChildren()) {
				portMap.put(new PortName(in.getLocalName()), i++);
			}
		}
		{
			int i = 0;
			for (PortDecl out : actorMachine.getOutputPorts().getChildren()) {
				portMap.put(new PortName(out.getLocalName()), i++);
			}
		}
*/
		Channel channelSource1 = new BasicChannel(3);
		Channel channelSource2 = new BasicChannel(3);
		Channel channelResult = new BasicChannel(3);
		Channel.InputEnd[] channelWriteEnd = { channelResult.getInputEnd() };
		Channel.OutputEnd channelResultEnd = channelResult.createOutputEnd();
		Channel.OutputEnd[] channelReadEnd = { channelSource1.createOutputEnd(), channelSource2.createOutputEnd() };
		// initial tokens
		channelSource1.getInputEnd().write(ConstRef.of(0));
		channelSource1.getInputEnd().write(ConstRef.of(1));
		channelSource1.getInputEnd().write(ConstRef.of(3));
		channelSource2.getInputEnd().write(ConstRef.of(10));
		channelSource2.getInputEnd().write(ConstRef.of(11));
		channelSource2.getInputEnd().write(ConstRef.of(12));

		Simulator runner = createActorMachineRunner(actorMachine, channelWriteEnd, channelReadEnd, 100);

		while(runner.step()) { ; }
		
		System.out.println("Scopes: ");
		System.out.println(runner.scopesToString());
		System.out.println("Output: ");
		String sep = "";
		int i = 0;
		while (channelResultEnd.tokens(i+1)) {
			BasicRef r = new BasicRef();
			channelResultEnd.peek(i++, r);
			System.out.print(sep + r.getLong());
			sep = ", ";
		}
		System.out.println();
	}

	/**
	 * @param actorMachine
	 * @param sinkChannelEnds - tokens produced by the actor is written to these channels.
	 * @param sourceChannelEnds - the actor reads tokens from these channels.
	 * @param stackSize
	 * @return
	 */
	private static Simulator createActorMachineRunner(ActorMachine actorMachine, Channel.InputEnd[] sinkChannelEnds,
			Channel.OutputEnd[] sourceChannelEnds, int stackSize) {
//		VariableBindings varBind = new VariableBindings();
//		VariableBindings.Bindings b = varBind.bindVariables(actorMachine);

//		SetVariablePositions setVarPos = new SetVariablePositions();
//		int memPos = setVarPos.setVariablePositions(actorMachine);
		
		Environment env = new BasicEnvironment(sinkChannelEnds, sourceChannelEnds, actorMachine);

		/*		for (Entry<IRNode, IRNode> binding : b.getVariableBindings().entrySet()) {
			VariableDeclaration decl = (VariableDeclaration) binding.getValue();
			int pos = decl.getVariablePosition();
			boolean stack = decl.isVariableOnStack();
			VariableUse use = (VariableUse) binding.getKey();
			use.setVariablePosition(pos, stack);
		}
		Map<String, RefView> predef = Predef.predef();
		for (IRNode n : b.getFreeVariables()) {
			ExprVariable e = (ExprVariable) n;
			e.setVariablePosition(memPos, false);
			predef.get(e.getName()).assignTo(env.getMemory().declare(memPos));
		}

		EvaluateLiterals evalLit = new EvaluateLiterals();
		evalLit.evaluateLiterals(actorMachine);

		SetScopeInitializers si = new SetScopeInitializers();
		si.setScopeInitializers(actorMachine);

		SetChannelIds ci = new SetChannelIds();
		ci.setChannelIds(actorMachine, portMap);
*/
		return new BasicSimulator(actorMachine, env, new BasicInterpreter(new BasicStack(stackSize)));
	}

	private static DeclVar varDecl(String name, Expression expr) {
		return new DeclVar(null, name, null, expr, false);
	}
/*
	private static ExprLiteral lit(int i) {
		return new ExprLiteral(ExprLiteral.litInteger, Integer.toString(i));
	}
*/
}
