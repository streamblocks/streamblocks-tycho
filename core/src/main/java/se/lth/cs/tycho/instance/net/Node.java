package se.lth.cs.tycho.instance.net;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.PortContainer;
import se.lth.cs.tycho.ir.entity.cal.CalActor;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A Node is a basic element in a network. It is a wrapper for a {@link PortContainer}. Usually,
 * this will be either another {@link Network}, a (parameterless) {@link CalActor}, an {@link ActorMachine}.
 * 
 * {@link Connection}s identifies the ports it connects to by linking to the {@link Identifier} of the encapsulating {@link Node} and a {@link se.lth.cs.tycho.ir.Port} object.
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
	
	public Node(String name, PortContainer content) {
		this(null, name, content);
	}

	protected Node(Node original, String name, PortContainer content) {
		super (original);
		if (original == null) {
			identifier = new Identifier();
		} else {
			identifier = original.identifier;
		}
		this.name = name;
		this.content = content;
	}
	
	public Node copy(String name, PortContainer content){
		if(Objects.equals(this.name, name) && Objects.equals(this.content, content)){
			return this;
		}
		return new Node(this, name, content);
	}

	public Identifier getIdentifier() {
		return identifier;
	}
	
	private String name;
	private PortContainer content;
	private final Identifier identifier;

	public String toString(){
		return name;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(content);
	}

	/**
	 * Each node in the internal representation has an Identifier.
	 * The following properties holds:
	 *
	 * - a new IRNode object gets a unique Identifier.
	 *
	 * - when a copy of an IRNode is created, the copy has the same Identifier
	 *   as the original. Use the copy(...) method.
	 *
	 * - When a IRNode is constructed by transforming another IRNode, then the
	 *   new construct should have the same Identifier as the source of the
	 *   transformation. For example an ActorMachine has the same Identifier
	 *   as the source CalActor.
	 *
	 * The third property is not a strict requirement. There exist
	 * situations where the transformed structure should not "inherit" all
	 * properties from the source. If a transformation breaks this property
	 * it should clearly state it in the documentation.
	 *
	 * The third property implies that there must exist a constructor
	 * MyIRClass(IRNode original, ...) in all classes implementing IRNode.
	 *
	 */

	public static final class Identifier{}
}
