package net.opendf.ir.net.ast;

import java.util.Objects;

import net.opendf.ir.net.ToolAttribute;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public class StructureConnectionStmt extends StructureStatement {

	public StructureConnectionStmt(PortReference src, PortReference dst, ImmutableList<ToolAttribute> toolAttributes) {
		this(null, src, dst, toolAttributes);
	}

	private StructureConnectionStmt(StructureConnectionStmt original, PortReference src, PortReference dst,
			ImmutableList<ToolAttribute> toolAttributes) {
		super(original, toolAttributes);
		this.src = src;
		this.dst = dst;
	}

	public StructureConnectionStmt copy(PortReference src, PortReference dst,
			ImmutableList<ToolAttribute> toolAttributes) {
		if (Objects.equals(this.src, src) && Objects.equals(this.dst, dst)
				&& Lists.equals(getToolAttributes(), toolAttributes)) {
			return this;
		}
		return new StructureConnectionStmt(this, src, dst, toolAttributes);
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
}
