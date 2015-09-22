package se.lth.cs.tycho.ir.entity.nl;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.AttributableIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public class StructureConnectionStmt extends StructureStatement {

	public StructureConnectionStmt(PortReference src, PortReference dst, ImmutableList<ToolAttribute> toolAttributes) {
		super(toolAttributes);
		this.src = src;
		this.dst = dst;
	}

	public StructureConnectionStmt copy(PortReference src, PortReference dst,
			ImmutableList<ToolAttribute> toolAttributes) {
		if (Objects.equals(this.src, src) && Objects.equals(this.dst, dst)
				&& Lists.equals(getToolAttributes(), toolAttributes)) {
			return this;
		}
		return new StructureConnectionStmt(src, dst, toolAttributes);
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

	private PortReference src, dst;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(src);
		action.accept(dst);
	}

	@Override
	public StructureConnectionStmt transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return copy(
				(PortReference) transformation.apply(src),
				(PortReference) transformation.apply(dst),
				(ImmutableList) getToolAttributes().map(transformation)
		);
	}
}
