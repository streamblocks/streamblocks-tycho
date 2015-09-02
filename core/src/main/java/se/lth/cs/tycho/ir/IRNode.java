package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.function.Consumer;


public interface IRNode {

	//public ToolAttribute getToolAttribute(String name);
	
	//public ImmutableList<ToolAttribute> getToolAttributes();

	void forEachChild(Consumer<? super IRNode> action);

}
