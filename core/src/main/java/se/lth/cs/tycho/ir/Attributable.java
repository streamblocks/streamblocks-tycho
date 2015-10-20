package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.util.ImmutableList;

public interface Attributable {
	ToolAttribute getToolAttribute(String name);

	ImmutableList<ToolAttribute> getToolAttributes();

	Attributable withToolAttributes(ImmutableList<ToolAttribute> attributes);

}
