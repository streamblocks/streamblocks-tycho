package net.opendf.ir.net;

import java.util.Objects;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.common.PortContainer;

/**
 * A Node is a basic element in a network. It can contain ports, and it also may contain some other {@link AbstractIRNode} as payload. Usually,
 * this will be either another {@link Network}, a (parameterless) {@link Actor}, an {@link ActorMachine}, or an 
 * entity instantiation expression (FIXME: add proper link).
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class Node extends AbstractIRNode {
	
	public PortContainer  getContent() { return content; }
	
	public String getName(){ return name; }

	//
	// Ctor
	//
	
	public Node(String name, PortContainer content) {
		this(null, name, content);
	}
	
	protected Node(Node original, String name, PortContainer content) {
		super (original);
		this.name = name;
		this.content = content;
	}
	
	public Node copy(String name, PortContainer content){
		if(Objects.equals(this.name, name) && Objects.equals(this.content, content)){
			return this;
		}
		return new Node(this, name, content);
	}
	
	private String name;
	private PortContainer content;
	
}
