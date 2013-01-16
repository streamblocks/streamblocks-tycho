package net.opendf.ir.net.ast;


/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

public interface EntityExprVisitor<R, P> {

	public R visitEntityInstanceExpr(EntityInstanceExpr e, P p);
	public R visitEntityIfExpr(EntityIfExpr e, P p);
	public R visitEntityListExpr(EntityListExpr e, P p);

}
