package net.opendf.ir.net;

import java.util.Objects;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.IRNode;
import net.opendf.ir.entity.PortContainer;
import net.opendf.ir.entity.am.ActorMachine;
import net.opendf.ir.entity.cal.Actor;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

/**
 * A Node is a basic element in a network. It is a wrapper for a {@link PortContainer}. Usually,
 * this will be either another {@link Network}, a (parameterless) {@link Actor}, an {@link ActorMachine}.
 * 
 * {@link Connection}s identifies the ports it connects to by linking to the {@link net.opendf.ir.IRNode.Identifier} of the encapsulating {@link Node} and a {@link net.opendf.ir.Port} object. 
 * 
 * The name is for human readability and should not be used by tools.
 * Names are not guaranteed to be unique.
 */

public class Node extends AbstractIRNode {
	
	public PortContainer  getContent() { return content; }
	
	public String getName(){ return name; }
	//
	// Ctor
	//
	
	public Node(String name, PortContainer content, ImmutableList<ToolAttribute> ta) {
		this(null, name, content, ta);
	}
	
	protected Node(IRNode original, String name, PortContainer content, ImmutableList<ToolAttribute> ta) {
		super (original, ta);
		this.name = name;
		this.content = content;
	}
	
	public Node copy(String name, PortContainer content, ImmutableList<ToolAttribute> ta){
		if(Objects.equals(this.name, name) && Objects.equals(this.content, content) && Lists.equals(getToolAttributes(), ta)){
			return this;
		}
		return new Node(this, name, content, ta);
	}
	
	private String name;
	private PortContainer content;

	public String toString(){
		return name;
	}
}
