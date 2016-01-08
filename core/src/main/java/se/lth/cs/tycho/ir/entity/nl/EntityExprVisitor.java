package se.lth.cs.tycho.ir.entity.nl;


/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

public interface EntityExprVisitor<R, P> {

	R visitEntityInstanceExpr(EntityInstanceExpr e, P p);
	R visitEntityIfExpr(EntityIfExpr e, P p);
	R visitEntityListExpr(EntityListExpr e, P p);

}
