package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.util.ImmutableList;

/**
 * Binds all uses of a entity instance name in the structure part of a .nl file to the corresponding EntityExpr.
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

public class EntityNameBinding implements StructureStmtVisitor<Object, ImmutableList<InstanceDecl>>{

	public EntityNameBinding(NlNetwork network){
		ImmutableList<InstanceDecl> names = network.getEntities();
		for(StructureStatement s : network.getStructure()){
			s.accept(this, names);
		}
	}

	public Object visitStructureConnectionStmt(StructureConnectionStmt stmt, ImmutableList<InstanceDecl> names) {
		lookupEntity(stmt.getSrc(), names);
		lookupEntity(stmt.getDst(), names);
		return null;
	}

	public Object visitStructureIfStmt(StructureIfStmt stmt, ImmutableList<InstanceDecl> names) {
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

	public Object visitStructureForeachStmt(StructureForeachStmt stmt, ImmutableList<InstanceDecl> names) {
		for(StructureStatement s : stmt.getStatements()){
			s.accept(this, names);
		}
		return null;
	}
	private void lookupEntity(PortReference ref, ImmutableList<InstanceDecl> names){
		EntityExpr entity = null;
		String entityName = ref.getEntityName();
		if(entityName != null){
			for(InstanceDecl binding : names){
				if(binding.getInstanceName().equals(entityName)){
					entity = binding.getEntityExpr();
					break;
				}
			}
			//TODO store entity expression somewhere !!!
//			if(entity == null){
//				System.err.println("Unknown name: " + entityName);
//			} else {
//				System.err.println("bound: " + entityName + " -> " + entity.getIdentifier());
//			}
		} else{
			//local port
		}
	}
}
