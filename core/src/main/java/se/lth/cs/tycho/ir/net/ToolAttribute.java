package se.lth.cs.tycho.ir.net;

import se.lth.cs.tycho.ir.AbstractIRNode;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

public abstract class ToolAttribute extends AbstractIRNode {
	enum Kind{value, type}
	abstract Kind getKind();
	
	public ToolAttribute(String name){
		this(null, name);
	}
	public ToolAttribute(ToolAttribute original, String name){
		super(original);
		this.name = name;
	}
	public String getName(){
		return name;
	}
	abstract public void print(java.io.PrintStream out);

	String name;
}
