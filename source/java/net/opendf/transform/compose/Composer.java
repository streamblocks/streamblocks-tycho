package net.opendf.transform.compose;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.RuntimeErrorException;

import javarag.AttributeEvaluator;
import javarag.AttributeRegister;
import javarag.impl.reg.BasicAttributeRegister;
import net.opendf.backend.c.IRNodeTraverser;
import net.opendf.backend.c.att.Ports;
import net.opendf.ir.Port;
import net.opendf.ir.Variable;
import net.opendf.ir.IRNode.Identifier;
import net.opendf.ir.entity.PortDecl;
import net.opendf.ir.entity.am.ActorMachine;
import net.opendf.ir.entity.am.Condition;
import net.opendf.ir.entity.am.Scope;
import net.opendf.ir.entity.am.Transition;
import net.opendf.ir.net.Connection;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;
import net.opendf.ir.util.ImmutableList;
import net.opendf.transform.compose.CompositionStateHandler.State;
import net.opendf.transform.filter.SelectFirstInstruction;
import net.opendf.transform.filter.SelectRandomInstruction;
import net.opendf.transform.reduction.FixedInstructionWeight;
import net.opendf.transform.reduction.PriorityListSelector;
import net.opendf.transform.reduction.Selector;
import net.opendf.transform.reduction.ShortestPathStateHandler;
import net.opendf.transform.reduction.TransitionPriorityStateHandler;
import net.opendf.transform.util.AbstractActorMachineTransformer;
import net.opendf.transform.util.ControllerGenerator;
import net.opendf.transform.util.StateHandler;

public class Composer {
	private final AttributeRegister register;
	private final IRNodeTraverser traverser;
	private final Transformer transformer;
	
	public Composer() {
		register = new BasicAttributeRegister();
		register.register(PortNames.class, ScopeNumbers.class, Ports.class);
		traverser = new IRNodeTraverser();
		transformer = new Transformer();
	}
	
	public Network composeNetwork(Network net, String name) {
		AttributeEvaluator evaluator = register.getEvaluator(net, traverser);
		ActorMachine composed = compose(net, evaluator);
		Node n = new Node(name, composed, null);
		ImmutableList<Connection> conns = connections(net, n, evaluator);
		return net.copy(ImmutableList.of(n), conns, net.getInputPorts(), net.getOutputPorts());
	}
	
	private ImmutableList<Connection> connections(Network net, Node n, AttributeEvaluator evaluator) {
		ImmutableList.Builder<Connection> connections = ImmutableList.builder();
		for (Connection c : net.getConnections()) {
			Identifier src = c.getSrcNodeId();
			Port srcPort = c.getSrcPort();
			Identifier dst = c.getDstNodeId();
			Port dstPort = c.getDstPort();
			if (src != null) {
				src = n.getIdentifier();
				srcPort = evaluator.evaluate("translatePort", srcPort);
			}
			if (dst != null) {
				dst = n.getIdentifier();
				dstPort = evaluator.evaluate("translatePort", dstPort);
			}
			connections.add(c.copy(src, srcPort, dst, dstPort, c.getToolAttributes()));
		}
		return connections.build();
	}

	private ActorMachine compose(Network net, AttributeEvaluator evaluator) {
		ImmutableList.Builder<PortDecl> inputPorts = ImmutableList.builder();
		ImmutableList.Builder<PortDecl> outputPorts = ImmutableList.builder();
		ImmutableList.Builder<Scope> scopes = ImmutableList.builder();
		ImmutableList.Builder<Transition> transitions = ImmutableList.builder();
		ImmutableList.Builder<Condition> conditions = ImmutableList.builder();
		for (Node n : net.getNodes()) {
			ActorMachine am = (ActorMachine) n.getContent();
			scopes.addAll(transformer.transformScopes(am.getScopes(), evaluator));
			transitions.addAll(transformer.transformTransitions(am.getTransitions(), evaluator));
			conditions.addAll(transformer.transformConditions(am.getConditions(), evaluator));
			inputPorts.addAll(transformer.transformInputPorts(am.getInputPorts(), evaluator));
			outputPorts.addAll(transformer.transformOutputPorts(am.getOutputPorts(), evaluator));
		}
		StateHandler<State> stateHandler = new CompositionStateHandler(net);
		//try {
		//	Selector<Integer> selector = new PriorityListSelector(PriorityListSelector.readIntsFromFile(new File("dcrecon.prio.txt")));
		//	stateHandler = new TransitionPriorityStateHandler<>(stateHandler, selector);
		//	stateHandler = new ShortestPathStateHandler<>(new FixedInstructionWeight<State>(1, 1, 1), stateHandler);
		//} catch (FileNotFoundException e) {
		//	throw new RuntimeException(e);
		//}
		stateHandler = new SelectRandomInstruction<>(stateHandler);
		//stateHandler = new SelectFirstInstruction<>(stateHandler);
		
		ControllerGenerator<State> controller = ControllerGenerator.generate(stateHandler);
		//printControllerInterpretation(controller);
		//System.out.println(controller.getInterpretation().size() + " states.");
		return new ActorMachine(inputPorts.build(), outputPorts.build(), scopes.build(), controller.getController(), transitions.build(), conditions.build());
	}
	
	private void printControllerInterpretation(ControllerGenerator<State> controller) {
		int i = 0;
		for (State s : controller.getInterpretation()) {
			System.out.println(i + ": " + s);
			i += 1;
		}
	}

	private class Transformer extends AbstractActorMachineTransformer<AttributeEvaluator> {
		@Override
		public PortDecl transformInputPort(PortDecl decl, AttributeEvaluator eval) {
			String name = eval.evaluate("uniquePortName", decl);
			return decl.copy(name, transformTypeExpr(decl.getType(), eval));
		}
		
		@Override
		public PortDecl transformOutputPort(PortDecl decl, AttributeEvaluator eval) {
			String name = eval.evaluate("uniquePortName", decl);
			return decl.copy(name, transformTypeExpr(decl.getType(), eval));
		}
		
		@Override
		public Port transformPort(Port port, AttributeEvaluator eval) {
			return eval.evaluate("translatePort", port);
		}
		@Override
		public Variable transformVariable(Variable var, AttributeEvaluator eval) {
			return eval.evaluate("translateVariable", var);
		}
		@Override
		public Transition transformTransition(Transition transition, AttributeEvaluator eval) {
			Map<Port, Integer> inputRates = transformTokenRates(transition.getInputRates(), eval);
			Map<Port, Integer> outputRates = transformTokenRates(transition.getOutputRates(), eval);
			return transition.copy(inputRates, outputRates,
					eval.<ImmutableList<Integer>> evaluate("scopesToKill", transition),
					transformStatement(transition.getBody(), eval));
		}
		private Map<Port, Integer> transformTokenRates(Map<Port, Integer> rates, AttributeEvaluator eval) {
			Map<Port, Integer> result = new HashMap<>();
			for (Entry<Port, Integer> entry : rates.entrySet()) {
				result.put(transformPort(entry.getKey(), eval), entry.getValue());
			}
			return result;
		}
	}

}
