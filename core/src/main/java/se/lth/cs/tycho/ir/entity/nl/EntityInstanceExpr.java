package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.AttributableIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.ToolAttribute;
import se.lth.cs.tycho.ir.ValueParameter;
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

	public EntityInstanceExpr(EntityReference entity, List<ValueParameter> parameterAssignments) {
		this(null, entity, parameterAssignments);
	}

	private EntityInstanceExpr(EntityInstanceExpr original, EntityReference entity, List<ValueParameter> parameterAssignments) {
		super(original);
		this.entity = entity;
		this.parameterAssignments = ImmutableList.from(parameterAssignments);
	}

	public EntityInstanceExpr copy(EntityReference entity, List<ValueParameter> parameterAssignments) {
		if (Objects.equals(this.entity, entity)
				&& Lists.sameElements(this.parameterAssignments, parameterAssignments)) {
			return this;
		}
		return new EntityInstanceExpr(this, entity, parameterAssignments);
	}

	public EntityReference getEntityName() {
		return entity;
	}

	public ImmutableList<ValueParameter> getParameterAssignments() {
		return parameterAssignments;
	}

	private final EntityReference entity;
	private final ImmutableList<ValueParameter> parameterAssignments;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(entity);
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
		return copy(transformation.applyChecked(EntityReference.class, entity),
				(ImmutableList) parameterAssignments.map(transformation)
		).withAttributes((List) getAttributes().map(transformation));
	}
}
