package se.lth.cs.tycho.phase;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.attribute.ModuleKey;

public interface TreeShadow {
	ModuleKey<TreeShadow> key = TreeShadowImpl::of;

	IRNode parent(IRNode node);

	IRNode root();

}
