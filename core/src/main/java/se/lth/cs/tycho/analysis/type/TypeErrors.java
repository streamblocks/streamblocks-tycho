package se.lth.cs.tycho.analysis.type;

import java.util.Set;

import se.lth.cs.tycho.analysis.types.Type;
import se.lth.cs.tycho.analysis.util.TreeRoot;
import se.lth.cs.tycho.analysis.util.TreeRootModule;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.stmt.StmtAssignment;
import se.lth.cs.tycho.messages.Message;
import javarag.Collected;
import javarag.Module;
import javarag.coll.Builder;
import javarag.coll.Builders;
import javarag.coll.Collector;

public class TypeErrors extends Module<TypeErrors.Attributes> {

	public interface Attributes extends Declarations, TypeAnalysis.Declarations, TreeRootModule.Declarations {
	}

	public interface Declarations {
		@Collected
		public Set<Message> typeErrors(TreeRoot root);
	}
	
	public Builder<Set<Message>, Message> typeErrors(TreeRoot root) {
		return Builders.setBuilder();
	}
	
	public void typeErrors(TreeRoot root, Collector<Message> coll) {
		coll.collectFrom(e().mainTree(root));
	}
	
	public void typeErrors(StmtAssignment assign, Collector<Message> coll) {
		Type lType = e().type(assign.getLValue());
		Type rType = e().type(assign.getExpression());
		if (!lType.isAssignableFrom(rType)) {
			coll.add(e().treeRoot(assign), Message.error("Type error: expected " + lType + " but was " + rType));
		}
	}
	
	public void typeErrors(VarDecl decl, Collector<Message> coll) {
		if (decl.getType() != null && decl.getValue() != null) {
			Type declType = e().type(decl.getType());
			Type valueType = e().type(decl.getValue());
			if (!declType.isAssignableFrom(valueType)) {
				coll.add(e().treeRoot(decl), Message.error("Type error: expected " + declType + " but was " + valueType));
			}
		}
	}

}
