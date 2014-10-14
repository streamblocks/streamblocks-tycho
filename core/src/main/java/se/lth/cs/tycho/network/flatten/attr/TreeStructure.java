package se.lth.cs.tycho.network.flatten.attr;

import java.util.Set;

import se.lth.cs.tycho.network.flatten.attr.TreeRoot;
import se.lth.cs.tycho.network.flatten.attr.TreeStructure;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import javarag.Collected;
import javarag.Inherited;
import javarag.Module;
import javarag.coll.Builder;
import javarag.coll.Builders;
import javarag.coll.Collector;

public class TreeStructure extends Module<TreeStructure.Attributes> {

	public interface Attributes {

		@Inherited
		Network enclosingNetwork(Object o);

		@Inherited
		Node enclosingNode(Object o);
		
		@Inherited
		TreeRoot<?> treeRoot(Object o);
		
		@Collected
		Set<Node> actorNodes(TreeRoot<?> root);

	}
	
	public Builder<Set<Node>, Node> actorNodes(TreeRoot<?> root) {
		return Builders.setBuilder();
	}
	
	public void actorNodes(Node node, Collector<Node> coll) {
		if (!(node.getContent() instanceof Network)) {
			coll.add(e().treeRoot(node), node);
		}
	}
	
	public TreeRoot<?> treeRoot(TreeRoot<?> root) {
		return root;
	}

	public Node enclosingNode(Node node) {
		return node;
	}

	public Network enclosingNetwork(Network n) {
		return n;
	}

}
