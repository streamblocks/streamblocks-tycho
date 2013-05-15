package net.opendf.transform.caltoam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opendf.ir.cal.Actor;
import net.opendf.ir.cal.InputPattern;
import net.opendf.ir.cal.OutputExpression;
import net.opendf.ir.common.Port;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.util.ImmutableList;
import net.opendf.transform.util.AbstractActorTransformer;

class AddNumberedPorts extends AbstractActorTransformer<AddNumberedPorts.PortMap> {
	
	public Actor addNumberedPorts(Actor actor) {
		return transformActor(actor, new PortMap());
	}

	@Override
	public PortDecl transformOutputPort(PortDecl port, PortMap map) {
		map.addOutputPort(port.getName());
		return port;
	}

	@Override
	public PortDecl transformInputPort(PortDecl port, PortMap map) {
		map.addInputPort(port.getName());
		return port;
	}

	@Override
	public Port transformPort(Port port, PortMap map) {
		assert port.hasLocation();
		return port;
	}
	
	@Override
	public ImmutableList<InputPattern> transformInputPatterns(ImmutableList<InputPattern> input, PortMap map) {
		ImmutableList.Builder<InputPattern> builder = ImmutableList.builder();
		int id = 0;
		for (InputPattern in : input) {
			if (in.getPort() == null) {
				builder.add(in.copy(new Port(map.getInputPortName(id)), in.getVariables(), in.getRepeatExpr()));
			} else {
				builder.add(in);
			}
			id += 1;
		}
		return super.transformInputPatterns(builder.build(), map);
	}
	
	@Override
	public InputPattern transformInputPattern(InputPattern input, PortMap map) {
		Port port = input.getPort();
		String name = port.getName();
		port = port.copy(name, map.getInputPort(name));
		return input.copy(
				port,
				input.getVariables(),
				transformExpression(input.getRepeatExpr(), map));
	}
	
	@Override
	public ImmutableList<OutputExpression> transformOutputExpressions(ImmutableList<OutputExpression> output, PortMap map) {
		ImmutableList.Builder<OutputExpression> builder = ImmutableList.builder();
		int id = 0;
		for (OutputExpression out : output) {
			if (out.getPort() == null) {
				builder.add(out.copy(new Port(map.getOutputPortName(id)), out.getExpressions(), out.getRepeatExpr()));
			} else {
				builder.add(out);
			}
			id += 1;
		}
		return super.transformOutputExpressions(builder.build(), map);
	}
	
	@Override
	public OutputExpression transformOutputExpression(OutputExpression output, PortMap map) {
		Port port = output.getPort();
		String name = port.getName();
		port = port.copy(name, map.getOutputPort(name));
		return output.copy(
				port,
				transformExpressions(output.getExpressions(), map),
				transformExpression(output.getRepeatExpr(), map));
	}

	public static class PortMap {
		private Map<String, Integer> inputMap;
		private List<String> inputList;
		private Map<String, Integer> outputMap;
		private List<String> outputList;
		
		public PortMap() {
			inputMap = new HashMap<>();
			inputList = new ArrayList<>();
			outputMap = new HashMap<>();
			outputList = new ArrayList<>();
		}

		public void addInputPort(String name) {
			inputList.add(name);
			inputMap.put(name, inputMap.size());
		}

		public void addOutputPort(String name) {
			outputList.add(name);
			outputMap.put(name, outputMap.size());
		}

		public int getInputPort(String name) {
			return inputMap.get(name);
		}
		
		public String getInputPortName(int id) {
			return inputList.get(id);
		}

		public int getOutputPort(String name) {
			return outputMap.get(name);
		}
		
		public String getOutputPortName(int id) {
			return outputList.get(id);
		}
	}
}
