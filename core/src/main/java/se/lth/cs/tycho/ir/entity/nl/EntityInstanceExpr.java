package se.lth.cs.tycho.ir.entity.nl;

import java.util.Map.Entry;
import java.util.Objects;

import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public class EntityInstanceExpr extends se.lth.cs.tycho.ir.entity.nl.EntityExpr {

	public EntityInstanceExpr(String entityName, ImmutableList<Entry<String, Expression>> parameterAssignments,
			ImmutableList<ToolAttribute> toolAttributes) {
		this(null, entityName, parameterAssignments, toolAttributes);
	}

	private EntityInstanceExpr(EntityInstanceExpr original, String entityName,
			ImmutableList<Entry<String, Expression>> parameterAssignments, ImmutableList<ToolAttribute> toolAttributes) {
		super(original, toolAttributes);
		this.entityName = entityName;
		this.parameterAssignments = ImmutableList.copyOf(parameterAssignments);
	}

	public EntityInstanceExpr copy(String entityName, ImmutableList<Entry<String, Expression>> parameterAssignments,
			ImmutableList<ToolAttribute> toolAttributes) {
		if (Objects.equals(this.entityName, entityName)
				&& Lists.equals(this.parameterAssignments, parameterAssignments)
				&& Lists.equals(getToolAttributes(), toolAttributes)) {
			return this;
		}
		return new EntityInstanceExpr(this, entityName, parameterAssignments, toolAttributes);
	}

	public String getEntityName() {
		return entityName;
	}

	public ImmutableList<Entry<String, Expression>> getParameterAssignments() {
		return parameterAssignments;
	}

	@Override
	public <R, P> R accept(EntityExprVisitor<R, P> v, P p) {
		return v.visitEntityInstanceExpr(this, p);
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(entityName);
		String sep = "";
		sb.append("(");
		for(Entry<String, Expression> pa : parameterAssignments){
			sb.append(pa.getKey());
			sb.append("=");
			sb.append(pa.getValue());
			sb.append(sep);
			sep = ", ";
		}
		sb.append(")");
		return sb.toString();
	}
	private String entityName; // the name of the actor/network
	private ImmutableList<Entry<String, Expression>> parameterAssignments;
}
