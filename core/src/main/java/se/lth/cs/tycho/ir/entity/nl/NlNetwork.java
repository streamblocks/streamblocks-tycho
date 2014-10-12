package se.lth.cs.tycho.ir.entity.nl;

import java.util.Map.Entry;

import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.decl.LocalTypeDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParDeclType;
import se.lth.cs.tycho.ir.decl.ParDeclValue;
import se.lth.cs.tycho.ir.entity.EntityDefinition;
import se.lth.cs.tycho.ir.entity.EntityVisitor;
import se.lth.cs.tycho.ir.entity.PortDecl;
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

public class NlNetwork extends EntityDefinition {
	public NlNetwork(ImmutableList<ParDeclType> typePars,
			ImmutableList<ParDeclValue> valuePars, ImmutableList<LocalTypeDecl> typeDecls, ImmutableList<LocalVarDecl> varDecls,
			ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<Entry<String, EntityExpr>> entities, ImmutableList<StructureStatement> structure,
			ImmutableList<ToolAttribute> toolAttributes) {
		this(null, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts, entities, structure,
				toolAttributes);
	}

	private NlNetwork(NlNetwork original,
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

	public NlNetwork copy(ImmutableList<ParDeclType> typePars,
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
