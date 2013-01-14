package net.opendf.ir.common;


/**
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public interface DeclVisitor<R,P> {
	
	public R visitDeclEntity(DeclEntity d, P p);
	
	public R visitDeclType(DeclType d, P p);
	
	public R visitDeclVar(DeclVar d, P p);

}
