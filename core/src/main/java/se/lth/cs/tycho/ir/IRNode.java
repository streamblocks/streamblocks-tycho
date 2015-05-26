package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.util.ImmutableList;


public interface IRNode {

	public ToolAttribute getToolAttribute(String name);
	
	public ImmutableList<ToolAttribute> getToolAttributes();
}
