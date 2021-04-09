package se.lth.cs.tycho.ir.network;

import se.lth.cs.tycho.ir.*;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Instance extends AttributableIRNode {
    private final String instanceName;
    private final QID entityName;
    private final ImmutableList<ValueParameter> valueParameters;
    private final ImmutableList<TypeParameter> typeParameters;
    private final ImmutableList<Annotation> annotations;

    public Instance(String instanceName, QID entityName, List<Annotation> annotations, List<ValueParameter> valueParameters, List<TypeParameter> typeParameters) {
        this(null, instanceName, entityName, annotations, valueParameters, typeParameters);
    }

    public Instance(String instanceName, QID entityName, List<ValueParameter> valueParameters, List<TypeParameter> typeParameters) {
        this(null, instanceName, entityName, ImmutableList.empty(), valueParameters, typeParameters);
    }

    private Instance(Instance original, String instanceName, QID entityName, List<Annotation> annotations, List<ValueParameter> valueParameters, List<TypeParameter> typeParameters) {
        super(original);
        this.instanceName = instanceName;
        this.entityName = entityName;
        this.annotations = ImmutableList.from(annotations);
        this.valueParameters = ImmutableList.from(valueParameters);
        this.typeParameters = ImmutableList.from(typeParameters);
    }

    public Instance copy(String instanceName, QID entityName, List<Annotation> annotations, List<ValueParameter> valueParameters, List<TypeParameter> typeParameters) {
        if (Objects.equals(this.instanceName, instanceName)
                && Objects.equals(this.entityName, entityName)
                && Lists.sameElements(this.annotations, annotations)
                && Lists.sameElements(this.valueParameters, valueParameters)
                && Lists.sameElements(this.typeParameters, typeParameters)) {
            return this;
        } else {
            return new Instance(this, instanceName, entityName, annotations, valueParameters, typeParameters);
        }
    }

    public String getInstanceName() {
        return instanceName;
    }

    public Instance withInstanceName(String name) {
        return copy(name, entityName, annotations, valueParameters, typeParameters);
    }

    public QID getEntityName() {
        return entityName;
    }

    public Instance withEntityName(QID entity) {
        return copy(instanceName, entity, annotations, valueParameters, typeParameters);
    }

    public ImmutableList<ValueParameter> getValueParameters() {
        return valueParameters;
    }

    public Instance withValueParameters(List<ValueParameter> valueParameters) {
        return copy(instanceName, entityName, annotations, valueParameters, typeParameters);
    }

    public ImmutableList<TypeParameter> getTypeParameters() {
        return typeParameters;
    }

    public Instance withTypeParameters(List<TypeParameter> typeParameters) {
        return copy(instanceName, entityName, annotations, valueParameters, typeParameters);
    }

    public ImmutableList<Annotation> getAnnotations() {
        return annotations;
    }

    public Instance withAnnotations(List<Annotation> annotations) {
        return copy(instanceName, entityName, annotations, valueParameters, typeParameters);
    }

    @Override
    public Instance withAttributes(List<ToolAttribute> attributes) {
        return (Instance) super.withAttributes(attributes);
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        annotations.forEach(action);
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
                (List) annotations.map(transformation),
                (List) valueParameters.map(transformation),
                (List) typeParameters.map(transformation)
        ).withAttributes((List) getAttributes().map(transformation));
    }
}
