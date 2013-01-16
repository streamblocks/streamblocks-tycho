package net.opendf.ir.net.ast;

import net.opendf.ir.common.CompositePortDecl;
import net.opendf.ir.common.DeclType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.NamespaceDecl;
import net.opendf.ir.common.ParDeclType;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.net.ToolAttribute;

/**
 * A NetworkTemplate the internal representation of a .nl file. Basically it is a AST derived straight from the .nl grammar.
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

public class NetworkDefinition extends net.opendf.ir.common.DeclEntity{
	public NetworkDefinition(String name){	
		super(name, null, null, null, null, null, null, null);
	}
	public NetworkDefinition(String name, 
			               NamespaceDecl ns,
			               ParDeclType[] typePars,
			               ParDeclValue[] valuePars,
			               DeclType[] typeDecls,
			               DeclVar[] varDecls,
			               CompositePortDecl inputPorts,
			               CompositePortDecl outputPorts,
			               java.util.Map.Entry<String,EntityExpr>[] entities,
			               StructureStatement[] structure,
			               ToolAttribute[] toolAttributes){	
		super(name, ns, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts);
		this.entities = entities;
		this.toolAttributes = toolAttributes;
		this.structure = structure;
	}

	public java.util.Map.Entry<String,EntityExpr>[] getEntities(){
		return entities;
	}
	public ToolAttribute[] getToolAttributes(){
		return toolAttributes;
	}
	public StructureStatement[] getStructure(){
		return structure;
	}

	private java.util.Map.Entry<String,EntityExpr>[] entities;
	private ToolAttribute[] toolAttributes;
	private StructureStatement[] structure;
}
