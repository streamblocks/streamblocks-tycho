package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.AttributableIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.ToolAttribute;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public class EntityInstanceExpr extends AttributableIRNode implements EntityExpr {

	public EntityInstanceExpr(String entityName, List<Parameter<Expression>> parameterAssignments) {
		this(null, entityName, parameterAssignments);
	}

	private EntityInstanceExpr(EntityInstanceExpr original, String entityName, List<Parameter<Expression>> parameterAssignments) {
		super(original);
		this.entityName = entityName;
		this.parameterAssignments = ImmutableList.from(parameterAssignments);
	}

	public EntityInstanceExpr copy(String entityName, List<Parameter<Expression>> parameterAssignments) {
		if (Objects.equals(this.entityName, entityName)
				&& Lists.sameElements(this.parameterAssignments, parameterAssignments)) {
			return this;
		}
		return new EntityInstanceExpr(this, entityName, parameterAssignments);
	}

	public String getEntityName() {
		return entityName;
	}

	public ImmutableList<Parameter<Expression>> getParameterAssignments() {
		return parameterAssignments;
	}

	@Override
	public <R, P> R accept(EntityExprVisitor<R, P> v, P p) {
		return v.visitEntityInstanceExpr(this, p);
	}

	private final String entityName; // the name of the calActor/network
	private final ImmutableList<Parameter<Expression>> parameterAssignments;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		parameterAssignments.forEach(action);
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
				(ImmutableList) parameterAssignments.map(transformation)
		).withAttributes((List) getAttributes().map(transformation));
	}
}
