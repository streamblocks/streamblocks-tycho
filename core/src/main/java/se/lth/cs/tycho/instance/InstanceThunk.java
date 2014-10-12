package se.lth.cs.tycho.instance;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.values.Environment;
import se.lth.cs.tycho.values.TypeThunk;
import se.lth.cs.tycho.values.ValueThunk;

public class InstanceThunk extends Instance {
	private final Entity entity;
	private final Environment<ValueThunk> valueEnv;
	private final Environment<TypeThunk> typeEnv;
	private final NamespaceDecl origin;

	public InstanceThunk(Entity entity, Environment<ValueThunk> valueEnv,
			Environment<TypeThunk> typeEnv, NamespaceDecl origin) {
		this(null, entity, valueEnv, typeEnv, origin);
	}
	
	
	public InstanceThunk(IRNode original, Entity entity, Environment<ValueThunk> valueEnv,
			Environment<TypeThunk> typeEnv, NamespaceDecl origin) {
		super(original);
		this.entity = entity;
		this.valueEnv = valueEnv;
		this.typeEnv = typeEnv;
		this.origin = origin;
	}

	public Entity getEntity() {
		return entity;
	}

	public Environment<ValueThunk> getValueEnvironment() {
		return valueEnv;
	}

	public Environment<TypeThunk> getTypeEnvironment() {
		return typeEnv;
	}
	
	public NamespaceDecl getOrigin() {
		return origin;
	}


	@Override
	public <R, P> R accept(InstanceVisitor<R, P> visitor, P param) {
		return visitor.visitEntityInstanceThunk(this, param);
	}

}
