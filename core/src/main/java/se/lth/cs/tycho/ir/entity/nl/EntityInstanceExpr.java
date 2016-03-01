package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.ToolAttribute;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public class EntityInstanceExpr extends EntityExpr {

	public EntityInstanceExpr(String entityName, ImmutableList<Entry<String, Expression>> parameterAssignments) {
		this(null, entityName, parameterAssignments);
	}

	private EntityInstanceExpr(EntityExpr original, String entityName, ImmutableList<Entry<String, Expression>> parameterAssignments) {
		super(original);
		this.entityName = entityName;
		this.parameterAssignments = ImmutableList.from(parameterAssignments);
	}

	public EntityInstanceExpr copy(String entityName, ImmutableList<Entry<String, Expression>> parameterAssignments) {
		if (Objects.equals(this.entityName, entityName)
				&& Lists.sameElements(this.parameterAssignments, parameterAssignments)) {
			return this;
		}
		return new EntityInstanceExpr(this, entityName, parameterAssignments);
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
		StringBuilder sb = new StringBuilder();
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
	private final String entityName; // the name of the calActor/network
	private final ImmutableList<Entry<String, Expression>> parameterAssignments;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		parameterAssignments.forEach(entry -> action.accept(entry.getValue()));
		getAttributes().forEach(action);
	}

	@Override
	public EntityInstanceExpr withAttributes(List<ToolAttribute> attributes) {
		return (EntityInstanceExpr) super.withAttributes(attributes);
	}

	@Override
	@SuppressWarnings("unchecked")
	public EntityInstanceExpr transformChildren(Transformation transformation) {
		return copy(entityName,
				(ImmutableList) parameterAssignments.map(entry -> ImmutableEntry.of(entry.getKey(), transformation.apply(entry.getValue())))
		).withAttributes((List) getAttributes().map(transformation));
	}
}
