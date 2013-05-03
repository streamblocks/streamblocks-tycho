

package net.opendf.ir.common;

import net.opendf.ir.AbstractIRNode;

/**
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public abstract class ParDecl extends AbstractIRNode {
	
	public static enum ParameterKind {value, type};
	
	public String  getName() { return name; }
	
	abstract public ParameterKind  parameterKind();
	
	//
	//  Ctor
	//
	
	public ParDecl(ParDecl original, String name) {
		super(original);
		this.name = name;
	}
	
	private String name;
}
