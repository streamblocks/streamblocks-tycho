package se.lth.cs.tycho.ir.entity.nl;

import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.EntityVisitor;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

/**
 * A NetworkTemplate the internal representation of a .nl file. Basically it is
 * a AST derived straight from the .nl grammar.
 * 
 * A NlNetwork is a flat structure, i.e. sub-networks are represented by their name.
 * NetworkDefinitions are instantiated to {@link Network}s.
 * {@link Network}s can be hierarchical, i.e. sub-networks are represented as {@link Network}s.
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

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
		this.typeDecls = typeDecls;
		this.varDecls = varDecls;
		this.entities = ImmutableList.copyOf(entities);
		this.toolAttributes = ImmutableList.copyOf(toolAttributes);
		this.structure = ImmutableList.copyOf(structure);
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
}
