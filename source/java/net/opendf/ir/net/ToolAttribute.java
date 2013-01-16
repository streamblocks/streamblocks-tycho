package net.opendf.ir.net;
/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

public abstract class ToolAttribute {
	enum Kind{value, type}
	abstract Kind getKind();
	
	public ToolAttribute(String name){
		this.name = name;
	}
	public String getName(){
		return name;
	}
	abstract public void print(java.io.PrintStream out);

	String name;
}
