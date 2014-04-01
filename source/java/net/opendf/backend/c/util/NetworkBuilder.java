package net.opendf.backend.c.util;

import java.util.LinkedHashMap;
import java.util.Map;
import net.opendf.ir.IRNode.Identifier;
import net.opendf.ir.common.Port;
import net.opendf.ir.common.PortContainer;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.common.TypeExpr;
import net.opendf.ir.net.Connection;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.net.ToolAttribute;



public class NetworkBuilder {
    private final Map<String, PortDecl> inputPorts;
    private final Map<String, PortDecl> outputPorts;
    private final Map<String, Node> nodes;
    private final ImmutableList.Builder<Connection> connections;

    public NetworkBuilder() {
        inputPorts = new LinkedHashMap<>();
        outputPorts = new LinkedHashMap<>();
        nodes = new LinkedHashMap<>();
        connections = new ImmutableList.Builder<>();
    }

    public void addInputPort(String name, TypeExpr type) {
        inputPorts.put(name, new PortDecl(name, type));
    }
    
    public void addOutputPort(String name, TypeExpr type) {
        outputPorts.put(name, new PortDecl(name, type));
    }

    public void addNode(String name, PortContainer node) {
    	System.out.println(name);
        addNode(name, node, null);
    }

    public void addNode(String name, PortContainer node, ImmutableList<ToolAttribute> toolAttributes) {
        nodes.put(name, new Node(name, node, toolAttributes));
    }

    public void addConnection(String src, String srcPort, String dst, String dstPort) {
        addConnection(src, srcPort, dst, dstPort, null);
    }

    public void addConnection(String src, String srcPort, String dst, String dstPort, ImmutableList<ToolAttribute> toolAttributes) {
        Identifier source = src == null ? null : nodes.get(src).getIdentifier();
        Identifier destination = dst == null ? null : nodes.get(dst).getIdentifier();
        Port sourcePort = new Port(srcPort);
        Port destinationPort = new Port(dstPort);
        connections.add(new Connection(source, sourcePort, destination, destinationPort, toolAttributes));
    }

    public Network build() {
        return new Network(
            ImmutableList.copyOf(nodes.values()),
            connections.build(),
            ImmutableList.copyOf(inputPorts.values()),
            ImmutableList.copyOf(outputPorts.values()));
    }
}
