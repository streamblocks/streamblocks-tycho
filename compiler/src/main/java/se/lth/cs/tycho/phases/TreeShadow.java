package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.phases.attributes.ModuleKey;

public interface TreeShadow {
	ModuleKey<TreeShadow> key = (unit, manager) -> TreeShadowImpl.of(unit);

	IRNode parent(IRNode node);

	IRNode root();

}
