package net.opendf.ir.net.ast;

import net.opendf.ir.common.Expression;
import net.opendf.ir.net.ToolAttribute;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

public class EntityInstanceExpr extends net.opendf.ir.net.ast.EntityExpr {

	public EntityInstanceExpr(String entityName, java.util.Map.Entry<String, Expression>[] parameterAssignments, ToolAttribute[] toolAttributes){
		this.entityName = entityName;
		this.parameterAssignments = parameterAssignments;
		this.toolAttributes = toolAttributes;
	}
	public String getEntityName(){
		return entityName;
	}
	public java.util.Map.Entry<String, Expression>[] getParameterAssignments(){
		return parameterAssignments;
	}
	public ToolAttribute[] getToolAttributes(){
		return toolAttributes;
	}

	@Override
	public <R, P> R accept(EntityExprVisitor<R, P> v, P p) {
		return v.visitEntityInstanceExpr(this, p);
	}

	private String entityName;  // the name of the actor/network
	private java.util.Map.Entry<String, Expression>[] parameterAssignments;
	private ToolAttribute[] toolAttributes;
}
