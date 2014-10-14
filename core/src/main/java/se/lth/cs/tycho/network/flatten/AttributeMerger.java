package se.lth.cs.tycho.network.flatten;

import java.util.List;

import se.lth.cs.tycho.instance.net.ToolAttribute;

public interface AttributeMerger {
	public ToolAttribute merge(List<ToolAttribute> attributes);
}
