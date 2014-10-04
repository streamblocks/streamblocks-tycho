package net.opendf.ir.net.ast;

import java.util.Map.Entry;
import java.util.Objects;

import net.opendf.ir.common.NamespaceDecl;
import net.opendf.ir.common.PortContainer;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.net.ToolAttribute;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;
import net.opendf.ir.common.decl.GlobalEntityDecl;
import net.opendf.ir.common.decl.LocalTypeDecl;
import net.opendf.ir.common.decl.LocalVarDecl;
import net.opendf.ir.common.decl.ParDeclType;
import net.opendf.ir.common.decl.ParDeclValue;

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

public class NetworkDefinition extends GlobalEntityDecl implements PortContainer{
	public NetworkDefinition(String name) {
		super(null, name, null, null, null, null, null, null);
	}

	public NetworkDefinition(String name, ImmutableList<ParDeclType> typePars,
			ImmutableList<ParDeclValue> valuePars, ImmutableList<LocalTypeDecl> typeDecls, ImmutableList<LocalVarDecl> varDecls,
			ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<Entry<String, EntityExpr>> entities, ImmutableList<StructureStatement> structure,
			ImmutableList<ToolAttribute> toolAttributes) {
		this(null, name, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts, entities, structure,
				toolAttributes);
	}

	private NetworkDefinition(NetworkDefinition original, String name,
			ImmutableList<ParDeclType> typePars, ImmutableList<ParDeclValue> valuePars,
			ImmutableList<LocalTypeDecl> typeDecls, ImmutableList<LocalVarDecl> varDecls, ImmutableList<PortDecl> inputPorts,
			ImmutableList<PortDecl> outputPorts, ImmutableList<Entry<String, EntityExpr>> entities,
			ImmutableList<StructureStatement> structure, ImmutableList<ToolAttribute> toolAttributes) {

		super(original, name, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts);
		this.entities = ImmutableList.copyOf(entities);
		this.toolAttributes = ImmutableList.copyOf(toolAttributes);
		this.structure = ImmutableList.copyOf(structure);
	}

	public NetworkDefinition copy(String name, ImmutableList<ParDeclType> typePars,
			ImmutableList<ParDeclValue> valuePars, ImmutableList<LocalTypeDecl> typeDecls, ImmutableList<LocalVarDecl> varDecls,
			ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<Entry<String, EntityExpr>> entities, ImmutableList<StructureStatement> structure,
			ImmutableList<ToolAttribute> toolAttributes) {
		if (Objects.equals(getName(), name)
				&& Lists.equals(getTypeParameters(), typePars) && Lists.equals(getValueParameters(), valuePars)
				&& Lists.equals(getTypeDecls(), typeDecls) && Lists.equals(getVarDecls(), varDecls)
				&& Lists.equals(getInputPorts(), inputPorts) && Lists.equals(getOutputPorts(), outputPorts)
				&& Lists.equals(this.entities, entities) && Lists.equals(this.structure, structure)
				&& Lists.equals(this.toolAttributes, toolAttributes)) {
			return this;
		}
		return new NetworkDefinition(this, name, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts,
				entities, structure, toolAttributes);
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

	private ImmutableList<Entry<String, EntityExpr>> entities;
	private ImmutableList<ToolAttribute> toolAttributes;
	private ImmutableList<StructureStatement> structure;
}
