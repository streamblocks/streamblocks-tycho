package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

import java.util.function.Consumer;
import java.util.function.Function;

public class StarImport extends AbstractIRNode {
	private final QID qid;

	public StarImport(QID qid) {
		this(null, qid);
	}

	private StarImport(IRNode original, QID qid) {
		super(original);
		this.qid = qid;
	}

	public QID getQID() {
		return qid;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {

	}

	@Override
	public StarImport transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return this;
	}
}
