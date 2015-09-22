package se.lth.cs.tycho.ir.entity.nl;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public class EntityInstanceExpr extends se.lth.cs.tycho.ir.entity.nl.EntityExpr {

	public EntityInstanceExpr(String entityName, ImmutableList<Entry<String, Expression>> parameterAssignments) {
		this.entityName = entityName;
		this.parameterAssignments = ImmutableList.copyOf(parameterAssignments);
	}

	public EntityInstanceExpr copy(String entityName, ImmutableList<Entry<String, Expression>> parameterAssignments) {
		if (Objects.equals(this.entityName, entityName)
				&& Lists.equals(this.parameterAssignments, parameterAssignments)) {
			return this;
		}
		return new EntityInstanceExpr(entityName, parameterAssignments);
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
	private String entityName; // the name of the calActor/network
	private ImmutableList<Entry<String, Expression>> parameterAssignments;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		parameterAssignments.forEach(entry -> action.accept(entry.getValue()));
	}

	@Override
	public EntityInstanceExpr transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return copy(entityName,
				(ImmutableList) parameterAssignments.map(entry -> ImmutableEntry.of(entry.getKey(), transformation.apply(entry.getValue()))));
	}
}
