package se.lth.cs.tycho.ir.network;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.AttributableIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.ToolAttribute;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Instance extends AttributableIRNode {
	private final String instanceName;
	private final String entityName;
	private final ImmutableList<Parameter<Expression>> valueParameters;
	private final ImmutableList<Parameter<TypeExpr>> typeParameters;

	public Instance(String instanceName, String entityName, List<Parameter<Expression>> valueParameters, List<Parameter<TypeExpr>> typeParameters) {
		this(null, instanceName, entityName, valueParameters, typeParameters);
	}

	private Instance(Instance original, String instanceName, String entityName, List<Parameter<Expression>> valueParameters, List<Parameter<TypeExpr>> typeParameters) {
		super(original);
		this.instanceName = instanceName;
		this.entityName = entityName;
		this.valueParameters = ImmutableList.from(valueParameters);
		this.typeParameters = ImmutableList.from(typeParameters);
	}

	public Instance copy(String name, String entity, List<Parameter<Expression>> valueParameters, List<Parameter<TypeExpr>> typeParameters) {
		if (Objects.equals(this.instanceName, name)
				&& Objects.equals(this.entityName, entity)
				&& Lists.sameElements(this.valueParameters, valueParameters)
				&& Lists.sameElements(this.typeParameters, typeParameters)) {
			return this;
		} else {
			return new Instance(this, name, entity, valueParameters, typeParameters);
		}
	}

	public String getInstanceName() {
		return instanceName;
	}

	public Instance withName(String name) {
		return copy(name, entityName, valueParameters, typeParameters);
	}

	public String getEntityName() {
		return entityName;
	}

	public Instance withEntity(String entity) {
		return copy(instanceName, entity, valueParameters, typeParameters);
	}

	public ImmutableList<Parameter<Expression>> getValueParameters() {
		return valueParameters;
	}

	public Instance withValueParameters(List<Parameter<Expression>> valueParameters) {
		return copy(instanceName, entityName, valueParameters, typeParameters);
	}

	public ImmutableList<Parameter<TypeExpr>> getTypeParameters() {
		return typeParameters;
	}

	public Instance withTypeParameters(List<Parameter<TypeExpr>> typeParameters) {
		return copy(instanceName, entityName, valueParameters, typeParameters);
	}

	@Override
	public Instance withAttributes(List<ToolAttribute> attributes) {
		return (Instance) super.withAttributes(attributes);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		valueParameters.forEach(action);
		typeParameters.forEach(action);
		getAttributes().forEach(action);
	}

	@Override
	@SuppressWarnings("unchecked")
	public AbstractIRNode transformChildren(Transformation transformation) {
		return copy(
				instanceName,
				entityName,
				(List) valueParameters.map(transformation),
				(List) typeParameters.map(transformation)
		).withAttributes((List) getAttributes().map(transformation));
	}
}
