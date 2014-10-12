package se.lth.cs.tycho.instance.net;

import java.util.Objects;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.PortContainer;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

/**
 * A Node is a basic element in a network. It is a wrapper for a {@link PortContainer}. Usually,
 * this will be either another {@link Network}, a (parameterless) {@link CalActor}, an {@link ActorMachine}.
 * 
 * {@link Connection}s identifies the ports it connects to by linking to the {@link se.lth.cs.tycho.ir.IRNode.Identifier} of the encapsulating {@link Node} and a {@link se.lth.cs.tycho.ir.Port} object. 
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
