package net.opendf.ir.net;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.IRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.common.PortContainer;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.util.ImmutableList;

/**
 * A Node is a basic element in a network. It can contain ports, and it also may contain some other {@link AbstractIRNode} as payload. Usually,
 * this will be either another {@link Network}, a (parameterless) {@link Actor}, an {@link ActorMachine}, or an 
 * entity instantiation expression (FIXME: add proper link).
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class Node extends AbstractIRNode implements PortContainer {
	
	public IRNode  getContent() { return content; }
	
	// PortContainer
	
	public ImmutableList<PortDecl> getInputPorts() {
		return inputPorts;
	}
	
	public ImmutableList<PortDecl> getOutputPorts() {
		return outputPorts;
	}
	
	
	//
	// Ctor
	//
	
	public Node(IRNode content) {
		super (null);
		
		this.content = content;
		this.inputPorts = inputPorts; //FIXME
		this.outputPorts = outputPorts; //FIXME
	}
	
	private IRNode content;

	private ImmutableList<PortDecl>  inputPorts;
	private ImmutableList<PortDecl>  outputPorts;
}
