package net.opendf.ir.net.ast;
/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.common.Expression;

public class PortReference extends AbstractIRNode {
	public PortReference(String entityName, Expression[] entityIndex, String portName){
		this.entityName = entityName;
		this.entityIndex = entityIndex;
		this.portName = portName;
	}
	public String getEntityName(){
		return entityName;
	}
	public Expression[] getEntityIndex(){
		return entityIndex;
	}
	public String getPortName(){
		return portName;
	}
	public String toString(){
		StringBuffer result = new StringBuffer();
		if(entityName != null){
			result.append(entityName);
			if(entityIndex != null){
				for(Expression e : entityIndex){
					result.append('[');
					result.append(e.toString());
					result.append(']');
				}
			}
			result.append('.');
		}
		result.append(portName);
		return result.toString();
	}

	private String entityName, portName;
	private Expression entityIndex[];
}
