package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.Attributable;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.ToolAttribute;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.EntityVisitor;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class NlNetwork extends Entity implements Attributable {
	private final ImmutableList<TypeDecl> typeDecls;
	private final ImmutableList<VarDecl> varDecls;
	private final ImmutableList<Entry<String, EntityExpr>> entities;
	private final ImmutableList<StructureStatement> structure;
	private final ImmutableList<ToolAttribute> attributes;

	public NlNetwork(List<TypeDecl> typePars,
					 List<VarDecl> valuePars, List<TypeDecl> typeDecls, List<VarDecl> varDecls,
					 List<PortDecl> inputPorts, List<PortDecl> outputPorts,
					 List<Entry<String, EntityExpr>> entities, List<StructureStatement> structure,
					 List<ToolAttribute> attributes) {
		this(null, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts, entities, structure,
				attributes);
	}

	private NlNetwork(NlNetwork original,
			List<TypeDecl> typePars, List<VarDecl> valuePars,
			List<TypeDecl> typeDecls, List<VarDecl> varDecls, List<PortDecl> inputPorts,
			List<PortDecl> outputPorts, List<Entry<String, EntityExpr>> entities,
			List<StructureStatement> structure, List<ToolAttribute> attributes) {

		super(original, inputPorts, outputPorts, typePars, valuePars);

		this.typeDecls = ImmutableList.from(typeDecls);
		this.varDecls = ImmutableList.from(varDecls);
		this.entities = ImmutableList.from(entities);
		this.structure = ImmutableList.from(structure);
		this.attributes = ImmutableList.from(attributes);
	}

	public NlNetwork copy(List<TypeDecl> typePars,
			List<VarDecl> valuePars, List<TypeDecl> typeDecls, List<VarDecl> varDecls,
			List<PortDecl> inputPorts, List<PortDecl> outputPorts,
			List<Entry<String, EntityExpr>> entities, List<StructureStatement> structure,
			List<ToolAttribute> toolAttributes) {
		if (Lists.sameElements(this.typeParameters, typePars)
				&& Lists.sameElements(this.valueParameters, valuePars)
				&& Lists.sameElements(this.typeDecls, typeDecls)
				&& Lists.sameElements(this.varDecls, varDecls)
				&& Lists.sameElements(this.inputPorts, inputPorts)
				&& Lists.sameElements(this.outputPorts, outputPorts)
				&& Lists.sameElements(this.entities, entities)
				&& Lists.sameElements(this.structure, structure)
				&& Lists.sameElements(this.attributes, toolAttributes)) {
			return this;
		}
		return new NlNetwork(this, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts,
				entities, structure, toolAttributes);
	}

	@Override
	public <R, P> R accept(EntityVisitor<R, P> visitor, P param) {
		return visitor.visitNlNetwork(this, param);
	}

	@Override
	@SuppressWarnings("unchecked")
	public NlNetwork transformChildren(Transformation transformation) {
		return copy(
				(ImmutableList) typeParameters.map(transformation),
				(ImmutableList) valueParameters.map(transformation),
				(ImmutableList) typeDecls.map(transformation),
				(ImmutableList) varDecls.map(transformation),
				(ImmutableList) inputPorts.map(transformation),
				(ImmutableList) outputPorts.map(transformation),
				(ImmutableList) entities.map(entry -> ImmutableEntry.of(entry.getKey(), (EntityExpr) transformation.apply(entry.getValue()))),
				(ImmutableList) structure.map(transformation),
				(ImmutableList) attributes.map(transformation)
		);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		typeParameters.forEach(action);
		valueParameters.forEach(action);
		typeDecls.forEach(action);
		varDecls.forEach(action);
		inputPorts.forEach(action);
		outputPorts.forEach(action);
		entities.forEach(entry -> action.accept(entry.getValue()));
		structure.forEach(action);
		attributes.forEach(action);
	}

	public ImmutableList<VarDecl> getVarDecls() {
		return varDecls;
	}

	public NlNetwork withVarDecls(List<VarDecl> varDecls) {
		if (Lists.sameElements(this.varDecls, varDecls)) {
			return this;
		} else {
			return new NlNetwork(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, entities, structure, attributes);
		}
	}
	public NlNetwork withValueParameters(List<VarDecl> valueParameters) {
		if (Lists.sameElements(this.valueParameters, valueParameters)) {
			return this;
		} else {
			return new NlNetwork(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, entities, structure, attributes);
		}
	}

	public ImmutableList<Entry<String, EntityExpr>> getEntities() {
		return entities;
	}

	public NlNetwork withEntities(List<Entry<String, EntityExpr>> entities) {
		if (Lists.sameElements(this.entities, entities)) {
			return this;
		} else {
			return new NlNetwork(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, entities, structure, attributes);
		}
	}

	public ImmutableList<StructureStatement> getStructure() {
		return structure;
	}

	public NlNetwork withStructure(List<StructureStatement> structure) {
		if (Lists.sameElements(this.structure, structure)) {
			return this;
		} else {
			return new NlNetwork(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, entities, structure, attributes);
		}
	}

	@Override
	public ImmutableList<ToolAttribute> getAttributes() {
		return attributes;
	}

	@Override
	public Attributable withAttributes(List<ToolAttribute> attributes) {
		if (Lists.sameElements(this.attributes, attributes)) {
			return this;
		} else {
			return new NlNetwork(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, entities, structure, attributes);
		}
	}
}
