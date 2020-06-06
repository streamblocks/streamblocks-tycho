package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.AttributableIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.ToolAttribute;
import se.lth.cs.tycho.ir.TypeParameter;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.expr.ExprTypeConstruction;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Per Andersson
 * 
 */

public class EntityInstanceExpr extends AttributableIRNode implements EntityExpr {

	public EntityInstanceExpr(EntityReference entity, List<TypeParameter> typeParameters, List<ValueParameter> valueParameters) {
		this(null, entity, typeParameters, valueParameters);
	}

	private EntityInstanceExpr(EntityInstanceExpr original, EntityReference entity, List<TypeParameter> typeParameters, List<ValueParameter> valueParameters) {
		super(original);
		this.entity = entity;
		this.typeParameters = ImmutableList.from(typeParameters);
		this.valueParameters = ImmutableList.from(valueParameters);
	}

	public EntityInstanceExpr copy(EntityReference entity, List<TypeParameter> typeParameters, List<ValueParameter> valueParameters) {
		if (Objects.equals(this.entity, entity)
				&& Lists.sameElements(this.typeParameters, typeParameters)
				&& Lists.sameElements(this.valueParameters, valueParameters)) {
			return this;
		}
		return new EntityInstanceExpr(this, entity, typeParameters, valueParameters);
	}

	public EntityReference getEntityName() {
		return entity;
	}

	public ImmutableList<TypeParameter> getTypeParameters() {
		return typeParameters;
	}

	public ImmutableList<ValueParameter> getValueParameters() {
		return valueParameters;
	}

	public EntityInstanceExpr withEntityName(EntityReference entity) {
		return copy(entity, getTypeParameters(), getValueParameters());
	}

	public EntityInstanceExpr withTypeParameters(ImmutableList<TypeParameter> typeParameters) {
		return copy(getEntityName(), typeParameters, getValueParameters());
	}

	public EntityInstanceExpr withValueParameters(ImmutableList<ValueParameter> valueParameters) {
		return copy(getEntityName(), getTypeParameters(), valueParameters);
	}

	private final EntityReference entity;
	private final ImmutableList<TypeParameter> typeParameters;
	private final ImmutableList<ValueParameter> valueParameters;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(entity);
		typeParameters.forEach(action);
		valueParameters.forEach(action);
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
				(ImmutableList) typeParameters.map(transformation),
				(ImmutableList) valueParameters.map(transformation)
		).withAttributes((List) getAttributes().map(transformation));
	}
}
