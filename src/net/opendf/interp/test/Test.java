package net.opendf.interp.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.opendf.analysis.BinOpToFunc;
import net.opendf.analysis.UnOpToFunc;
import net.opendf.analysis.VariableBindings;
import net.opendf.interp.ActorMachineRunner;
import net.opendf.interp.BasicActorMachineRunner;
import net.opendf.interp.BasicChannel;
import net.opendf.interp.BasicEnvironment;
import net.opendf.interp.BasicProceduralExecutor;
import net.opendf.interp.BasicStack;
import net.opendf.interp.Channel;
import net.opendf.interp.Environment;
import net.opendf.interp.attr.Variables.VariableDeclaration;
import net.opendf.interp.attr.Variables.VariableUse;
import net.opendf.interp.preprocess.EvaluateLiterals;
import net.opendf.interp.preprocess.SetChannelIds;
import net.opendf.interp.preprocess.SetScopeInitializers;
import net.opendf.interp.preprocess.SetVariablePositions;
import net.opendf.interp.values.BasicRef;
import net.opendf.interp.values.ConstRef;
import net.opendf.interp.values.RefView;
import net.opendf.interp.values.predef.Predef;
import net.opendf.ir.IRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Scope;
import net.opendf.ir.am.Scope.ScopeKind;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprLiteral;
import net.opendf.ir.common.ExprVariable;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.common.PortName;
import net.opendf.parser.lth.CalParser;
import net.opendf.trans.caltoam.ActorToActorMachine;

public class Test {
	public static void main(String[] args) throws FileNotFoundException {
		File calFile = new File("../dataflow/examples/SimpleExamples/Add.cal");
		// File calFile = new
		// File("../dataflow/examples/MPEG4_SP_Decoder/ACPred.cal");

		CalParser parser = new CalParser();
		Actor actor = parser.parse(calFile);
		// net.opendf.util.PrettyPrint print = new
		// net.opendf.util.PrettyPrint();
		// print.print(actor);

		List<Decl> actorArgs = new ArrayList<Decl>();
		// actorArgs.add(varDecl("MAXW_IN_MB", lit(121)));
		// actorArgs.add(varDecl("MB_COORD_SZ", lit(8)));
		// actorArgs.add(varDecl("SAMPLE_SZ", lit(13)));
		Scope argScope = new Scope(ScopeKind.Persistent, actorArgs);

		ActorToActorMachine trans = new ActorToActorMachine();
		ActorMachine actorMachine = trans.translate(actor, argScope);

		// net.opendf.ir.am.util.ControllerToGraphviz.print(new
		// PrintStream("controller.gv"), actorMachine, "Controller");

		BinOpToFunc binOpToFunc = new BinOpToFunc();
		binOpToFunc.transformActorMachine(actorMachine);

		UnOpToFunc unOpToFunc = new UnOpToFunc();
		unOpToFunc.transformActorMachine(actorMachine);

		Map<PortName, Integer> portMap = new HashMap<PortName, Integer>();
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

		Channel[] channels = { new BasicChannel(1), new BasicChannel(1), new BasicChannel(1) };
		Channel.InputEnd[] channelIn = { channels[2].getInputEnd() };
		Channel.OutputEnd[] channelOut = { channels[0].createOutputEnd(), channels[1].createOutputEnd() };
		channels[0].getInputEnd().write(ConstRef.of(3));
		channels[1].getInputEnd().write(ConstRef.of(5));
		Channel.OutputEnd channelResult = channels[2].createOutputEnd();

		ActorMachineRunner runner = createActorMachineRunner(actorMachine, channelIn, channelOut, portMap, 100);

		runner.step();
		if (channelResult.tokens(1)) {
			BasicRef r = new BasicRef();
			channelResult.peek(0, r);
			System.out.println(r.getLong());
		} else {
			System.out.println("error");
		}
	}

	private static ActorMachineRunner createActorMachineRunner(ActorMachine actorMachine, Channel.InputEnd[] channelIn,
			Channel.OutputEnd[] channelOut, Map<PortName, Integer> portMap, int stackSize) {
		VariableBindings varBind = new VariableBindings();
		VariableBindings.Bindings b = varBind.bindVariables(actorMachine);

		SetVariablePositions setVarPos = new SetVariablePositions();
		int memPos = setVarPos.setVariablePositions(actorMachine);
		Environment env = new BasicEnvironment(channelIn, channelOut, memPos + b.getFreeVariables().size());
		for (Entry<IRNode, IRNode> binding : b.getVariableBindings().entrySet()) {
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

		return new BasicActorMachineRunner(actorMachine, env, new BasicProceduralExecutor(new BasicStack(stackSize)));
	}

	private static DeclVar varDecl(String name, Expression expr) {
		return new DeclVar(null, name, null, expr, false);
	}

	private static ExprLiteral lit(int i) {
		return new ExprLiteral(ExprLiteral.litInteger, Integer.toString(i));
	}

}
