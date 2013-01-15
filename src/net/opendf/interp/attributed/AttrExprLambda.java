package net.opendf.interp.attributed;

import net.opendf.ir.common.ExprLambda;

public class AttrExprLambda extends ExprLambda {
	public final int[] memoryRefs;
	public final int[] stackRefs;

	public AttrExprLambda(ExprLambda base, int[] memoryRefs, int[] stackRefs) {
		super(base.getTypeParameters(), base.getValueParameters(), base.getTypeDecls(), base.getVarDecls(), base.getBody(), base.getReturnType());
		this.memoryRefs = memoryRefs;
		this.stackRefs = stackRefs;
	}

}
