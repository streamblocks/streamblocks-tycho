package net.opendf.ir.net.ast;

import java.util.Map.Entry;

import net.opendf.ir.common.Expression;
import net.opendf.ir.net.ToolAttribute;
import net.opendf.ir.util.ImmutableList;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public class EntityInstanceExpr extends net.opendf.ir.net.ast.EntityExpr {

	public EntityInstanceExpr(String entityName, ImmutableList<Entry<String, Expression>> parameterAssignments,
			ImmutableList<ToolAttribute> toolAttributes) {
		this.entityName = entityName;
		this.parameterAssignments = ImmutableList.copyOf(parameterAssignments);
		this.toolAttributes = ImmutableList.copyOf(toolAttributes);
	}

	public String getEntityName() {
		return entityName;
	}

	public ImmutableList<Entry<String, Expression>> getParameterAssignments() {
		return parameterAssignments;
	}

	public ImmutableList<ToolAttribute> getToolAttributes() {
		return toolAttributes;
	}

	@Override
	public <R, P> R accept(EntityExprVisitor<R, P> v, P p) {
		return v.visitEntityInstanceExpr(this, p);
	}

	private String entityName; // the name of the actor/network
	private ImmutableList<Entry<String, Expression>> parameterAssignments;
	private ImmutableList<ToolAttribute> toolAttributes;
}
