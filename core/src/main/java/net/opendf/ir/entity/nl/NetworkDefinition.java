package net.opendf.ir.entity.nl;

import java.util.Map.Entry;

import net.opendf.ir.decl.LocalTypeDecl;
import net.opendf.ir.decl.LocalVarDecl;
import net.opendf.ir.decl.ParDeclType;
import net.opendf.ir.decl.ParDeclValue;
import net.opendf.ir.entity.EntityDefinition;
import net.opendf.ir.entity.EntityVisitor;
import net.opendf.ir.entity.PortDecl;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.ToolAttribute;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

/**
 * A NetworkTemplate the internal representation of a .nl file. Basically it is
 * a AST derived straight from the .nl grammar.
 * 
 * A NetworkDefinition is a flat structure, i.e. sub-networks are represented by their name.
 * NetworkDefinitions are instantiated to {@link Network}s.
 * {@link Network}s can be hierarchical, i.e. sub-networks are represented as {@link Network}s.
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public class NetworkDefinition extends EntityDefinition {
	public NetworkDefinition(ImmutableList<ParDeclType> typePars,
			ImmutableList<ParDeclValue> valuePars, ImmutableList<LocalTypeDecl> typeDecls, ImmutableList<LocalVarDecl> varDecls,
			ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<Entry<String, EntityExpr>> entities, ImmutableList<StructureStatement> structure,
			ImmutableList<ToolAttribute> toolAttributes) {
		this(null, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts, entities, structure,
				toolAttributes);
	}

	private NetworkDefinition(NetworkDefinition original,
			ImmutableList<ParDeclType> typePars, ImmutableList<ParDeclValue> valuePars,
			ImmutableList<LocalTypeDecl> typeDecls, ImmutableList<LocalVarDecl> varDecls, ImmutableList<PortDecl> inputPorts,
			ImmutableList<PortDecl> outputPorts, ImmutableList<Entry<String, EntityExpr>> entities,
			ImmutableList<StructureStatement> structure, ImmutableList<ToolAttribute> toolAttributes) {

		super(original, inputPorts, outputPorts, typePars, valuePars);
		this.typeDecls = typeDecls;
		this.varDecls = varDecls;
		this.entities = ImmutableList.copyOf(entities);
		this.toolAttributes = ImmutableList.copyOf(toolAttributes);
		this.structure = ImmutableList.copyOf(structure);
	}

	public NetworkDefinition copy(ImmutableList<ParDeclType> typePars,
			ImmutableList<ParDeclValue> valuePars, ImmutableList<LocalTypeDecl> typeDecls, ImmutableList<LocalVarDecl> varDecls,
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
		return new NetworkDefinition(this, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts,
				entities, structure, toolAttributes);
	}

	@Override
	public <R, P> R accept(EntityVisitor<R, P> visitor, P param) {
		return visitor.visitNetworkDefinition(this, param);
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

	public ImmutableList<LocalTypeDecl> getTypeDecls() {
		return typeDecls;
	}

	public ImmutableList<LocalVarDecl> getVarDecls() {
		return varDecls;
	}

	private final ImmutableList<LocalTypeDecl> typeDecls;
	private final ImmutableList<LocalVarDecl> varDecls;
	private final ImmutableList<Entry<String, EntityExpr>> entities;
	private final ImmutableList<ToolAttribute> toolAttributes;
	private final ImmutableList<StructureStatement> structure;
}
