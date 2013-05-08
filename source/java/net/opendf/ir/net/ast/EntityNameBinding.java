package net.opendf.ir.net.ast;

import java.util.Map.Entry;

import net.opendf.ir.util.ImmutableList;

/**
 * Binds all uses of a entity instance name in the structure part of a .nl file to the corresponding EntityExpr.
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

public class EntityNameBinding implements StructureStmtVisitor<Object, ImmutableList<Entry<String,EntityExpr>>>{

	public EntityNameBinding(NetworkDefinition network){
		ImmutableList<Entry<String,EntityExpr>> names = network.getEntities();
		for(StructureStatement s : network.getStructure()){
			s.accept(this, names);
		}
	}

	public Object visitStructureConnectionStmt(StructureConnectionStmt stmt, ImmutableList<Entry<String,EntityExpr>> names) {
		lookupEntity(stmt.getSrc(), names);
		lookupEntity(stmt.getDst(), names);
		return null;
	}

	public Object visitStructureIfStmt(StructureIfStmt stmt, ImmutableList<Entry<String,EntityExpr>> names) {
		for(StructureStatement s : stmt.getTrueStmt()){
			s.accept(this, names);
		}
		if(stmt.getFalseStmt() != null){
			for(StructureStatement s : stmt.getFalseStmt()){
				s.accept(this, names);
			}
		}
		return null;
	}

	public Object visitStructureForeachStmt(StructureForeachStmt stmt, ImmutableList<Entry<String,EntityExpr>> names) {
		for(StructureStatement s : stmt.getStatements()){
			s.accept(this, names);
		}
		return null;
	}
	private void lookupEntity(PortReference ref, ImmutableList<Entry<String,EntityExpr>> names){
		EntityExpr entity = null;
		String entityName = ref.getEntityName();
		if(entityName != null){
			for(Entry<String, EntityExpr> binding : names){
				if(binding.getKey().equals(entityName)){
					entity = binding.getValue();
					break;
				}
			}
			//TODO store entity expression somewhere !!!
			if(entity == null){
				System.err.println("Unknown name: " + entityName);
			} else {
				System.err.println("bound: " + entityName + " -> " + entity.getIdentifier());				
			}
		} else{
			//local port
		}
	}
}
