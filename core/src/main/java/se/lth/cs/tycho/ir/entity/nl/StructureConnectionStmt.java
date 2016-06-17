package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.AttributableIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.ToolAttribute;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public class StructureConnectionStmt extends AttributableIRNode implements StructureStatement {

	public StructureConnectionStmt(PortReference src, PortReference dst) {
		this(null, src, dst);
	}
	public StructureConnectionStmt(StructureConnectionStmt original, PortReference src, PortReference dst) {
		super(original);
		this.src = src;
		this.dst = dst;
	}

	public StructureConnectionStmt copy(PortReference src, PortReference dst) {
		if (this.src == src && this.dst == dst) {
			return this;
		}
		return new StructureConnectionStmt(this, src, dst);
	}

	public StructureConnectionStmt withSrc(PortReference src) {
		return copy(src, dst);
	}

	public StructureConnectionStmt withDst(PortReference dst) {
		return copy(src, dst);
	}

	public PortReference getSrc() {
		return src;
	}

	public PortReference getDst() {
		return dst;
	}

	public String toString() {
		return src.toString() + " --> " + dst.toString();
	}

	@Override
	public <R, P> R accept(StructureStmtVisitor<R, P> v, P p) {
		return v.visitStructureConnectionStmt(this, p);
	}

	private final PortReference src, dst;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(src);
		action.accept(dst);
		getAttributes().forEach(action);
	}


	@Override
	public StructureConnectionStmt withAttributes(List<ToolAttribute> attributes) {
		return (StructureConnectionStmt) super.withAttributes(attributes);
	}

	@Override
	@SuppressWarnings("unchecked")
	public StructureConnectionStmt transformChildren(Transformation transformation) {
		return copy(
				(PortReference) transformation.apply(src),
				(PortReference) transformation.apply(dst)
		).withAttributes((List) getAttributes().map(transformation));
	}
}
