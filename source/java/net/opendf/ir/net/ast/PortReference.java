package net.opendf.ir.net.ast;
/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.common.Expression;
import net.opendf.ir.util.ImmutableList;

public class PortReference extends AbstractIRNode {
	public PortReference(String entityName, ImmutableList<Expression> entityIndex, String portName){
		this.entityName = entityName;
		this.entityIndex = ImmutableList.copyOf(entityIndex);
		this.portName = portName;
	}
	public String getEntityName(){
		return entityName;
	}
	public ImmutableList<Expression> getEntityIndex(){
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
	private ImmutableList<Expression> entityIndex;
}
