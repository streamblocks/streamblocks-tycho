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
import net.opendf.parser.SourceCodeOracle;
import net.opendf.transform.operators.ActorOpTransformer;

public class BasicNetworkSimulator implements Simulator{
	private int defaultStackSize;
	private int defaultChannelSize;
	Network net;
	private Simulator[] simList;
	int nextNodeToRun;

	private void warning(String msg) {
		System.err.println(msg);
	}

	public static Network prepareNetworkDefinition(NetworkDefinition net, DeclLoader declLoader){
		return prepareNetworkDefinition(net, ImmutableList.<Map.Entry<String,Expression>>empty(), declLoader);
	}

	/**
	 * Evaluate a {@link NetworkDefinition} to a {@link Network}. 
	 * In the evaluated network all {@link Actor}s are evaluated to {@link ActorMachine}s and embedded {@link NetworkDefinition}s to a {@link Network}s
	 * @param net
	 * @param paramAssigns
	 * @param declLoader
	 * @return
	 * @throws CALCompiletimeException if an error occurs
	 */
	public static Network prepareNetworkDefinition(NetworkDefinition net, ImmutableList<Map.Entry<String,Expression>> paramAssigns, DeclLoader declLoader) throws CALCompiletimeException {
		// order variable initializations
		net = VariableInitOrderTransformer.transformNetworkDefinition(net, declLoader);
		// replace operators with function calls
		net = ActorOpTransformer.transformNetworkDefinition(net, declLoader);
		// replace global variables with constants, i.e. $BinaryOperation.+ with ExprValue(ConstRef.of(new IntFunctions.Add()))
		net = EvaluateLiteralsTransformer.transformNetworkDefinition(net, declLoader);
		// compute variable offsets
		net = VariableOffsetTransformer.transformNetworkDefinition(net, declLoader);
		

		Interpreter interpreter = new BasicInterpreter(100);
		NetDefEvaluator eval = new NetDefEvaluator(net, interpreter, declLoader);
		eval.evaluate(paramAssigns);

		return eval.getNetwork();
	}
		
	public BasicNetworkSimulator(Network net, int defaultChannelSize, int defaultStackSize) {
		this(net, new Channel[net.getInputPorts().size()], new Channel[net.getOutputPorts().size()], defaultChannelSize, defaultStackSize);
	}

	public BasicNetworkSimulator(Network net, Channel[] externalSourcePortChannel, Channel[] externalSinkPortChannel, 
			int defaultChannelSize, int defaultStackSize) {
		this.net = net;
		this.defaultStackSize = defaultStackSize;
		this.defaultChannelSize = defaultChannelSize;
		ImmutableList<Node> nodeList = net.getNodes();
		int nbrNodes = nodeList.size();
		simList = new Simulator[nbrNodes];
		
		Channel[][] internalNodeSinkPortChannel = new Channel[nbrNodes][];            //[nodeIndex][portIndex]
		Channel[][] internalNodeSourcePortChannel = new Channel[nbrNodes][];         //[nodeIndex][portIndex]
		for(int i=0; i<nbrNodes; i++){
			PortContainer node = nodeList.get(i).getContent();
			internalNodeSourcePortChannel[i] = new Channel[node.getOutputPorts().size()];
			internalNodeSinkPortChannel[i] = new Channel[node.getInputPorts().size()];
		}

		instantiateChannels(internalNodeSourcePortChannel, internalNodeSinkPortChannel, 
				            externalSourcePortChannel, externalSinkPortChannel);
		instantiateNodes(internalNodeSourcePortChannel, internalNodeSinkPortChannel);
	}
	
	private void instantiateChannels(Channel[][] internalSourcePortChannel, Channel[][] internalSinkPortChannel,
			Channel[] externalSourcePortChannel, Channel[] externalSinkPortChannel){
		ImmutableList<Node> nodeList = net.getNodes();

		
		// if several connections attach to the same output port, they should share the same BasicCannel
		for(Connection con : net.getConnections()){
			int srcNodeIndex = -1;
			int srcPortIndex;
			int dstNodeIndex = -1;
			int dstPortIndex;
			
			//---- find the source node and port
			Identifier srcId = con.getSrcNodeId();
			if(srcId != null){
				srcNodeIndex = findNodeIndex(srcId, nodeList);
				assert srcNodeIndex >= 0;
				srcPortIndex = findPort(con.getSrcPort().getName(), nodeList.get(srcNodeIndex).getContent().getOutputPorts());
				assert srcPortIndex>=0;
			} else {
				// connect to an external port
				srcPortIndex = findPort(con.getSrcPort().getName(), net.getInputPorts());
				assert srcPortIndex>=0;
			}
			
			//---- find the sink node and port
			Identifier dstId = con.getDstNodeId();
			if(dstId != null){
				// internal node
				dstNodeIndex = findNodeIndex(con.getDstNodeId(), nodeList);
				assert dstNodeIndex >= 0;
				dstPortIndex = findPort(con.getDstPort().getName(), nodeList.get(dstNodeIndex).getContent().getInputPorts());
				assert dstPortIndex>=0;
			} else {
				// external port
				dstPortIndex = findPort(con.getDstPort().getName(), net.getOutputPorts());
				assert dstPortIndex>=0;
			}

			if(srcNodeIndex >=0){
				// source is internal node
				if(dstNodeIndex>=0){
					// internal -> internal
					assert internalSinkPortChannel[dstNodeIndex][dstPortIndex]==null;   // assert that this is the only connection writing to this port
					if(internalSourcePortChannel[srcNodeIndex][srcPortIndex]==null){
						internalSourcePortChannel[srcNodeIndex][srcPortIndex] = createChannel(con);
					}
					internalSinkPortChannel[dstNodeIndex][dstPortIndex] = internalSourcePortChannel[srcNodeIndex][srcPortIndex];
				} else {
					// internal -> external
					assert internalSourcePortChannel[srcNodeIndex][srcPortIndex]==null;
					if(externalSinkPortChannel[dstPortIndex]==null){  // the wrapping network has not connected to the port. This gives a warning in the wrapper so ignore it here
						externalSinkPortChannel[dstPortIndex] = createChannel(con);
					}
					internalSourcePortChannel[srcNodeIndex][srcPortIndex] = externalSinkPortChannel[dstPortIndex];
				}
			} else {
				// source is external port
				if(dstNodeIndex>=0){
					// external -> internal
					assert internalSinkPortChannel[dstNodeIndex][dstPortIndex] == null;   // assert that this is the only connection writing to this port
					if(externalSourcePortChannel[srcPortIndex]==null){      // the wrapping network has not connected to the port. This gives a warning in the wrapper so ignore it here
						externalSourcePortChannel[srcPortIndex] = createChannel(con);
					}
					internalSinkPortChannel[dstNodeIndex][dstPortIndex] = externalSourcePortChannel[srcPortIndex];
				} else {
					// external -> internal
					if(externalSourcePortChannel[srcPortIndex] != null){
						if(externalSinkPortChannel[dstPortIndex] != null){
							//TODO we have two channels instances to connect. Merge channels
							throw new CALCompiletimeException("multiple writers to the same port port.", null);
						} else {
							// mark the input port as writer to the output port to detect multiple writers.
							externalSourcePortChannel[srcPortIndex] = externalSinkPortChannel[dstPortIndex];
						}
						
					} else {
						// the wrapper has not connected to the input port
						if(externalSinkPortChannel[dstPortIndex] != null){
							externalSourcePortChannel[srcPortIndex] = externalSinkPortChannel[dstPortIndex];
						} else {
							// both external ports are unconnected in the wrapper, so let them hang loose
							throw new UnsupportedOperationException("connecting a network input port directly to a network output port.");
						}
					}
				}
			}
		}
	}

	private Channel createChannel(Connection con) {
		int size = defaultChannelSize;
		//TODO check for channel size among the tool attributes
		return new BasicChannel(size);
	}

	private void instantiateNodes(Channel[][] internalSourcePortChannel, Channel[][] intrnalSinkPortChannel){
		ImmutableList<Node> nodeList = net.getNodes();
		int nbrNodes = nodeList.size();
		// instantiate the nodes
		for(int nodeIndex=0; nodeIndex<nbrNodes; nodeIndex++){
			Node node = nodeList.get(nodeIndex);
			//--- the node is an Actor ---
			if(node.getContent() instanceof ActorMachine){
				ActorMachine am = (ActorMachine)node.getContent();
				// create the InputEnd[] for the Environment
				InputEnd[] channelInputEnds = new InputEnd[internalSourcePortChannel[nodeIndex].length];
				for(int portIndex=0; portIndex<channelInputEnds.length; portIndex++){
					Channel c = internalSourcePortChannel[nodeIndex][portIndex];
					if(c==null){
						Node n = net.getNodes().get(nodeIndex);
						warning("unconnected port. Node " + n.getName() + ", port: " + n.getContent().getOutputPorts().get(portIndex));
						internalSourcePortChannel[nodeIndex][portIndex] = new BasicChannel(defaultChannelSize);
					}
					channelInputEnds[portIndex] = internalSourcePortChannel[nodeIndex][portIndex].getInputEnd();
				}
				// create the OutputEnd[] for the Environment
				OutputEnd[] channelOutputEnds = new OutputEnd[intrnalSinkPortChannel[nodeIndex].length];
				for(int portIndex=0; portIndex<channelOutputEnds.length; portIndex++){
					Channel c = intrnalSinkPortChannel[nodeIndex][portIndex];
					if(c==null){
						Node n = net.getNodes().get(nodeIndex);
						warning("unconnected port. Node " + n.getName() + ", port: " + n.getContent().getInputPorts().get(portIndex));
						intrnalSinkPortChannel[nodeIndex][portIndex] = new BasicChannel(defaultChannelSize);
					}
					channelOutputEnds[portIndex] = intrnalSinkPortChannel[nodeIndex][portIndex].createOutputEnd();
				}
				Environment env = new BasicEnvironment(channelInputEnds, channelOutputEnds, am);
				Interpreter interp = new BasicInterpreter(defaultStackSize);
				simList[nodeIndex] = new BasicActorMachineSimulator(am, env, interp);
			} else if(node.getContent() instanceof Network){
				//--- the node is a Network ---
				Network net = (Network)node.getContent();
				simList[nodeIndex] = new BasicNetworkSimulator(net, intrnalSinkPortChannel[nodeIndex], internalSourcePortChannel[nodeIndex], 
						defaultChannelSize, defaultStackSize);				
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
