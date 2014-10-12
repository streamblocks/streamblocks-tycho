package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.util.ImmutableList;


public interface IRNode {
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
	 *   as the source Actor.
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
	
	public final class Identifier{}
	
	public Identifier getIdentifier();

	public ToolAttribute getToolAttribute(String name);
	
	public ImmutableList<ToolAttribute> getToolAttributes();
}
