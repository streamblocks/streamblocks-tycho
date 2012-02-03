package net.opendf.ir.common;


/**
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public interface DeclVisitor {
	
	public void  visitDeclEntity(DeclEntity d);
	
	public void  visitDeclType(DeclType d);
	
	public void  visitDeclVar(DeclVar d);

}
