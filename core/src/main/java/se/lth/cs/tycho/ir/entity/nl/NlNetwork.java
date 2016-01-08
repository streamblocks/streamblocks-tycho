package se.lth.cs.tycho.ir.entity.nl;

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

import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

public class NlNetwork extends Entity {
	public NlNetwork(ImmutableList<TypeDecl> typePars,
			ImmutableList<VarDecl> valuePars, ImmutableList<TypeDecl> typeDecls, ImmutableList<VarDecl> varDecls,
			ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<Entry<String, EntityExpr>> entities, ImmutableList<StructureStatement> structure,
			ImmutableList<ToolAttribute> toolAttributes) {
		this(null, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts, entities, structure,
				toolAttributes);
	}

	private NlNetwork(NlNetwork original,
			ImmutableList<TypeDecl> typePars, ImmutableList<VarDecl> valuePars,
			ImmutableList<TypeDecl> typeDecls, ImmutableList<VarDecl> varDecls, ImmutableList<PortDecl> inputPorts,
			ImmutableList<PortDecl> outputPorts, ImmutableList<Entry<String, EntityExpr>> entities,
			ImmutableList<StructureStatement> structure, ImmutableList<ToolAttribute> toolAttributes) {

		super(original, inputPorts, outputPorts, typePars, valuePars);
		this.typeDecls = ImmutableList.from(typeDecls);
		this.varDecls = ImmutableList.from(varDecls);
		this.entities = ImmutableList.from(entities);
		this.toolAttributes = ImmutableList.from(toolAttributes);
		this.structure = ImmutableList.from(structure);
	}

	public NlNetwork copy(ImmutableList<TypeDecl> typePars,
			ImmutableList<VarDecl> valuePars, ImmutableList<TypeDecl> typeDecls, ImmutableList<VarDecl> varDecls,
			ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<Entry<String, EntityExpr>> entities, ImmutableList<StructureStatement> structure,
			ImmutableList<ToolAttribute> toolAttributes) {
		if (Lists.equals(getTypeParameters(), typePars) && Lists.equals(getValueParameters(), valuePars)
				&& Lists.equals(getTypeDecls(), typeDecls) && Lists.equals(getVarDecls(), varDecls)
				&& Lists.equals(getInputPorts(), inputPorts) && Lists.equals(getOutputPorts(), outputPorts)
				&& Lists.equals(this.entities, entities) && Lists.equals(this.structure, structure)
				&& Lists.equals(this.toolAttributes, toolAttributes)) {
			return this;
		}
		return new NlNetwork(this, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts,
				entities, structure, toolAttributes);
	}

	@Override
	public <R, P> R accept(EntityVisitor<R, P> visitor, P param) {
		return visitor.visitNlNetwork(this, param);
	}


	public ImmutableList<Entry<String, EntityExpr>> getEntities() {
		return entities;
	}

	public ImmutableList<ToolAttribute> getToolAttributes() {
		return toolAttributes;
	}

	public ImmutableList<StructureStatement> getStructure() {
		return structure;
	}

	public ImmutableList<TypeDecl> getTypeDecls() {
		return typeDecls;
	}

	public ImmutableList<VarDecl> getVarDecls() {
		return varDecls;
	}

	private final ImmutableList<TypeDecl> typeDecls;
	private final ImmutableList<VarDecl> varDecls;
	private final ImmutableList<Entry<String, EntityExpr>> entities;
	private final ImmutableList<ToolAttribute> toolAttributes;
	private final ImmutableList<StructureStatement> structure;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		super.forEachChild(action);
		typeDecls.forEach(action);
		varDecls.forEach(action);
		entities.forEach(entry -> action.accept(entry.getValue()));
		structure.forEach(action);
	}

	@Override
	public NlNetwork transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return copy(
				(ImmutableList) getTypeParameters().map(transformation),
				(ImmutableList) getValueParameters().map(transformation),
				(ImmutableList) typeDecls.map(transformation),
				(ImmutableList) varDecls.map(transformation),
				(ImmutableList) getInputPorts().map(transformation),
				(ImmutableList) getOutputPorts().map(transformation),
				(ImmutableList) entities.map(entry -> ImmutableEntry.of(entry.getKey(), transformation.apply(entry.getValue()))),
				(ImmutableList) structure.map(transformation),
				(ImmutableList) toolAttributes.map(transformation)
		);
	}

	public NlNetwork withVarDecls(ImmutableList<VarDecl> varDecls) {
		if (Lists.elementIdentityEquals(this.varDecls, varDecls)) {
			return this;
		} else {
			return new NlNetwork(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, entities, structure, toolAttributes);
		}
	}
	public NlNetwork withValueParameters(ImmutableList<VarDecl> valueParameters) {
		if (Lists.elementIdentityEquals(this.valueParameters, valueParameters)) {
			return this;
		} else {
			return new NlNetwork(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, entities, structure, toolAttributes);
		}
	}
}
