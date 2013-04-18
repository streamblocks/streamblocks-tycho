package net.opendf.ir.net.ast;

import java.util.Map.Entry;
import java.util.Objects;

import net.opendf.ir.common.DeclType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.NamespaceDecl;
import net.opendf.ir.common.ParDeclType;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.net.ToolAttribute;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

/**
 * A NetworkTemplate the internal representation of a .nl file. Basically it is
 * a AST derived straight from the .nl grammar.
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public class NetworkDefinition extends net.opendf.ir.common.DeclEntity {

	public NetworkDefinition(String name) {
		super(null, name, null, null, null, null, null, null, null);
	}

	public NetworkDefinition(String name, NamespaceDecl ns, ImmutableList<ParDeclType> typePars,
			ImmutableList<ParDeclValue> valuePars, ImmutableList<DeclType> typeDecls, ImmutableList<DeclVar> varDecls,
			ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<Entry<String, EntityExpr>> entities, ImmutableList<StructureStatement> structure,
			ImmutableList<ToolAttribute> toolAttributes) {
		this(null, name, ns, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts, entities, structure,
				toolAttributes);
	}

	private NetworkDefinition(NetworkDefinition original, String name, NamespaceDecl ns,
			ImmutableList<ParDeclType> typePars, ImmutableList<ParDeclValue> valuePars,
			ImmutableList<DeclType> typeDecls, ImmutableList<DeclVar> varDecls, ImmutableList<PortDecl> inputPorts,
			ImmutableList<PortDecl> outputPorts, ImmutableList<Entry<String, EntityExpr>> entities,
			ImmutableList<StructureStatement> structure, ImmutableList<ToolAttribute> toolAttributes) {

		super(original, name, ns, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts);
		this.entities = ImmutableList.copyOf(entities);
		this.toolAttributes = ImmutableList.copyOf(toolAttributes);
		this.structure = ImmutableList.copyOf(structure);
	}

	public NetworkDefinition copy(String name, NamespaceDecl ns, ImmutableList<ParDeclType> typePars,
			ImmutableList<ParDeclValue> valuePars, ImmutableList<DeclType> typeDecls, ImmutableList<DeclVar> varDecls,
			ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<Entry<String, EntityExpr>> entities, ImmutableList<StructureStatement> structure,
			ImmutableList<ToolAttribute> toolAttributes) {
		if (Objects.equals(getName(), name) && Objects.equals(getNamespaceDecl(), ns)
				&& Lists.equals(getTypeParameters(), typePars) && Lists.equals(getValueParameters(), valuePars)
				&& Lists.equals(getTypeDecls(), typeDecls) && Lists.equals(getVarDecls(), varDecls)
				&& Lists.equals(getInputPorts(), inputPorts) && Lists.equals(getOutputPorts(), outputPorts)
				&& Lists.equals(this.entities, entities) && Lists.equals(this.structure, structure)
				&& Lists.equals(this.toolAttributes, toolAttributes)) {
			return this;
		}
		return new NetworkDefinition(this, name, ns, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts,
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
