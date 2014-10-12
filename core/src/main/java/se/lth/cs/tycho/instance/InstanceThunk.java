package se.lth.cs.tycho.instance;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.values.TypeThunk;
import se.lth.cs.tycho.values.ValueThunk;

public class InstanceThunk extends Instance {
	private final Entity entity;
	private final ImmutableList<Parameter<ValueThunk>> valueEnv;
	private final ImmutableList<Parameter<TypeThunk>> typeEnv;
	private final NamespaceDecl location;

	public InstanceThunk(Entity entity, ImmutableList<Parameter<ValueThunk>> valueEnv,
			ImmutableList<Parameter<TypeThunk>> typeEnv, NamespaceDecl origin) {
		this(null, entity, valueEnv, typeEnv, origin);
	}
	
	
	public InstanceThunk(IRNode original, Entity entity, ImmutableList<Parameter<ValueThunk>> valueEnv,
			ImmutableList<Parameter<TypeThunk>> typeEnv, NamespaceDecl origin) {
		super(original);
		this.entity = entity;
		this.valueEnv = valueEnv;
		this.typeEnv = typeEnv;
		this.location = origin;
	}

	public Entity getEntity() {
		return entity;
	}

	public ImmutableList<Parameter<ValueThunk>> getValueEnvironment() {
		return valueEnv;
	}

	public ImmutableList<Parameter<TypeThunk>> getTypeEnvironment() {
		return typeEnv;
	}
	
	public NamespaceDecl getLocation() {
		return location;
	}


	@Override
	public <R, P> R accept(InstanceVisitor<R, P> visitor, P param) {
		return visitor.visitEntityInstanceThunk(this, param);
	}


	@Override
	public ImmutableList<PortDecl> getInputPorts() {
		throw new UnsupportedOperationException();
	}


	@Override
	public ImmutableList<PortDecl> getOutputPorts() {
		throw new UnsupportedOperationException();
	}

}
