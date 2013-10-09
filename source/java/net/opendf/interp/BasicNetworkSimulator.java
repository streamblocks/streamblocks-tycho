package net.opendf.interp;

import java.util.Map;

import net.opendf.analyze.memory.VariableInitOrderTransformer;
import net.opendf.interp.Channel.InputEnd;
import net.opendf.interp.Channel.OutputEnd;
import net.opendf.interp.exception.CALCompiletimeException;
import net.opendf.interp.preprocess.EvaluateLiteralsTransformer;
import net.opendf.interp.preprocess.VariableOffsetTransformer;
import net.opendf.ir.IRNode.Identifier;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.PortContainer;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.net.Connection;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;
import net.opendf.ir.net.ast.NetworkDefinition;
import net.opendf.ir.net.ast.evaluate.NetDefEvaluator;
import net.opendf.ir.util.DeclLoader;
import net.opendf.ir.util.ImmutableList;
import net.opendf.transform.operators.ActorOpTransformer;

public class BasicNetworkSimulator implements Simulator{
	Network net;
	private Simulator[] simList;
	int nextNodeToRun;

	public static Network prepareNetworkDefinition(NetworkDefinition net, DeclLoader declLoader){
		return prepareNetworkDefinition(net, ImmutableList.<Map.Entry<String,Expression>>empty(), declLoader);
	}
	
	public static Network prepareNetworkDefinition(NetworkDefinition net, ImmutableList<Map.Entry<String,Expression>> paramAssigns, DeclLoader declLoader){
		// order variable initializations
		net = VariableInitOrderTransformer.transformNetworkDefinition(net);
		// replace operators with function calls
		net = ActorOpTransformer.transformNetworkDefinition(net);
		// replace global variables with constants, i.e. $BinaryOperation.+ with ExprValue(ConstRef.of(new IntFunctions.Add()))
		net = EvaluateLiteralsTransformer.transformNetworkDefinition(net);
		// compute variable offsets
		VariableOffsetTransformer varT = new VariableOffsetTransformer();
		net = varT.transformNetworkDefinition(net);
		

		Interpreter interpreter = new BasicInterpreter(100);
		NetDefEvaluator eval = new NetDefEvaluator(net, interpreter, declLoader);
		eval.evaluate(paramAssigns);

		return eval.getNetwork();
	}
		
	public BasicNetworkSimulator(Network net, int defaultChannelSize) {
		this.net = net;
		ImmutableList<Node> nodeList = net.getNodes();
		int nbrNodes = nodeList.size();
		simList = new Simulator[nbrNodes];
		
		InputEnd[][] channelInputEnds = new InputEnd[nbrNodes][];            //[nodeIndex][portIndex]
		OutputEnd[][] channelOutputEnds = new OutputEnd[nbrNodes][];         //[nodeIndex][portIndex]
		for(int i=0; i<nbrNodes; i++){
			PortContainer node = nodeList.get(i).getContent();
			channelInputEnds[i] = new InputEnd[node.getOutputPorts().size()];
			channelOutputEnds[i] = new OutputEnd[node.getInputPorts().size()];
		}

		instantiateChannels(channelInputEnds, channelOutputEnds, defaultChannelSize);
		instantiateNodes(channelInputEnds, channelOutputEnds);
	}
	
	private void instantiateChannels(InputEnd[][] channelInputEnds, OutputEnd[][] channelOutputEnds, int defaultChannelSize){
		ImmutableList<Node> nodeList = net.getNodes();
		int nbrNodes = nodeList.size();

		Channel[][] channels = new Channel[nbrNodes][];                      //[nodeIndex][portIndex]
		for(int i=0; i<nbrNodes; i++){
			PortContainer node = nodeList.get(i).getContent();
			channels[i] = new Channel[node.getOutputPorts().size()];
		}

		//TODO external channels should be passed from the wrapping network. There the BasicChannels have already been created.
		//FIXME this do not work, where are the channels created? here, or in the wrapping network
		Channel[] externalChannels = new Channel[net.getInputPorts().size()];

		InputEnd[] externalChannelInputEnds = new InputEnd[net.getInputPorts().size()];
		OutputEnd[] externalChannelOutputEnds = new OutputEnd[net.getOutputPorts().size()];

		
		// if several connections attach to the same output port, they should share the same BasicCannel
		for(Connection con : net.getConnections()){
			Channel channel;
			// connect to the source node
			Identifier srcId = con.getSrcNodeId();
			if(srcId != null){
				// connect to an internal node
				int srcNodeIndex = findNodeIndex(srcId, nodeList);
				assert srcNodeIndex >= 0;
				int srcPortIndex = findPort(con.getSrcPort().getName(), nodeList.get(srcNodeIndex).getContent().getOutputPorts());
				assert srcPortIndex>=0;
				if(channels[srcNodeIndex][srcPortIndex] == null){
					channel = new BasicChannel(defaultChannelSize);
					channels[srcNodeIndex][srcPortIndex] = channel;
				} else {
					// another connection to the source port exists, share the BasicChannel
					channel = channels[srcNodeIndex][srcPortIndex];
				}
				channelInputEnds[srcNodeIndex][srcPortIndex] = channel.getInputEnd();
			} else {
				// connect to an external port
				int srcPortIndex = findPort(con.getSrcPort().getName(), net.getInputPorts());
				assert srcPortIndex>=0;
				if(externalChannels[srcPortIndex] == null){
					// This should not happen
					channel = new BasicChannel(defaultChannelSize);
					externalChannels[srcPortIndex] = channel;
					throw new CALCompiletimeException("You are connectiong to an external port with the intent to read from it, but no one is writing to this channel");
				} else {
					channel = externalChannels[srcPortIndex];
					// another connection to the source port exists, share the BasicCahnnel
				}
				externalChannelInputEnds[srcPortIndex] = channel.getInputEnd();
			}
			
			// connect to the sink port
			Identifier dstId = con.getDstNodeId();
			if(dstId != null){
				// internal node
				int dstNodeIndex = findNodeIndex(con.getDstNodeId(), nodeList);
				assert dstNodeIndex >= 0;
				int dstPortIndex = findPort(con.getDstPort().getName(), nodeList.get(dstNodeIndex).getContent().getInputPorts());
				assert dstPortIndex>=0;
				if(channelOutputEnds[dstNodeIndex][dstPortIndex] != null){
					throw new CALCompiletimeException("Connceting multiple channels to the same sink port. Node " + net.getNodes().get(dstNodeIndex).getName() + " port " + dstPortIndex);
				}
				channelOutputEnds[dstNodeIndex][dstPortIndex] = channel.createOutputEnd();
			} else {
				// external port
				int dstPortIndex = findPort(con.getDstPort().getName(), net.getOutputPorts());
				assert dstPortIndex>=0;
				if(externalChannelOutputEnds[dstPortIndex] != null){
					throw new CALCompiletimeException("Connceting multiple channels to the same external sink port.");
				}
				externalChannelOutputEnds[dstPortIndex] = channel.createOutputEnd();
			}
		}
	}

	private void instantiateNodes(InputEnd[][] channelInputEnds, OutputEnd[][] channelOutputEnds){
		ImmutableList<Node> nodeList = net.getNodes();
		int nbrNodes = nodeList.size();
		// instantiate the nodes
		for(int i=0; i<nbrNodes; i++){
			Node node = nodeList.get(i);
			if(node.getContent() instanceof ActorMachine){
				ActorMachine am = (ActorMachine)node.getContent();
				Environment env = new BasicEnvironment(channelInputEnds[i], channelOutputEnds[i], am);
				Interpreter interp = new BasicInterpreter(100);
				Simulator amSim = new BasicActorMachineSimulator(am, env, interp);
				simList[i] = amSim;
			} else if(node.getContent() instanceof Network){
				//TODO
				throw new java.lang.UnsupportedOperationException("Nested network in not supported by the simulator.");
			} else {
				throw new java.lang.UnsupportedOperationException("Network node must be instances of ActorMachine or Network in the simulator. " + node.getName() + " is instance of class "+ node.getContent().getClass().getCanonicalName());
			}
		}
	}

	private int findPort(String name, ImmutableList<PortDecl> ports) {
		for(int i=0; i<ports.size(); i++){
			if(ports.get(i).getName().equals(name)){
				return i;
			}
		}
		return -1;
	}

	private int findNodeIndex(Identifier nodeId, ImmutableList<Node> nodeList) {
		for(int i=0; i<nodeList.size(); i++){
			if(nodeList.get(i).getIdentifier() == nodeId){
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean step() {
		int waits = 0;
		boolean progress = false;
		while(!progress && waits<simList.length){
			progress = simList[nextNodeToRun].step();
			nextNodeToRun = (nextNodeToRun + 1) % simList.length;
			waits++;
		}
		return progress;
	}

	@Override
	public void scopesToString(StringBuffer sb) {
		ImmutableList<Node> names = net.getNodes();
		for(int i =0; i<simList.length; i++){
			sb.append("-- Node: " + names.get(i).getName() + "--\n");
			simList[i].scopesToString(sb);
		}
	}

}
