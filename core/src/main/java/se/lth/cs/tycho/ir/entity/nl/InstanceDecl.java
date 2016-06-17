package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;

import java.util.function.Consumer;

public class InstanceDecl extends AbstractIRNode {
	private final String instanceName;
	private final EntityExpr entityExpr;

	public InstanceDecl(String instanceName, EntityExpr entityExpr) {
		this(null, instanceName, entityExpr);
	}

	private InstanceDecl(IRNode original, String instanceName, EntityExpr entityExpr) {
		super(original);
		this.instanceName = instanceName;
		this.entityExpr = entityExpr;
	}

	public InstanceDecl copy(String instanceName, EntityExpr entityExpr) {
		if (this.instanceName.equals(instanceName) && this.entityExpr == entityExpr) {
			return this;
		} else {
			return new InstanceDecl(this, instanceName, entityExpr);
		}
	}

	public String getInstanceName() {
		return instanceName;
	}

	public InstanceDecl withInstanceName(String instanceName) {
		return copy(instanceName, entityExpr);
	}

	public EntityExpr getEntityExpr() {
		return entityExpr;
	}

	public InstanceDecl withEntityExpr(EntityExpr entityExpr) {
		return copy(instanceName, entityExpr);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(entityExpr);
	}

	@Override
	public InstanceDecl transformChildren(Transformation transformation) {
		return withEntityExpr(transformation.applyChecked(EntityExpr.class, entityExpr));
	}

}
