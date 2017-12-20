package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.module.ModuleExpr;

import java.util.Objects;

public class ModuleParameter extends AbstractIRNode implements Parameter<ModuleExpr, ModuleParameter> {

	private final String name;
	private final ModuleExpr moduleExpr;

	private ModuleParameter(IRNode original, String name, ModuleExpr moduleExpr) {
		super(original);
		this.name = name;
		this.moduleExpr = moduleExpr;
	}

	public ModuleParameter(String name, ModuleExpr moduleExpr) {
		this(null, name, moduleExpr);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ModuleExpr getValue() {
		return moduleExpr;
	}

	@Override
	public ModuleParameter copy(String name, ModuleExpr moduleExpr) {
		if (Objects.equals(this.name, name) && Objects.equals(this.moduleExpr, moduleExpr)) {
			return this;
		} else {
			return new ModuleParameter(this, name, moduleExpr);
		}
	}

	@Override
	public ModuleParameter clone() {
		return (ModuleParameter) super.clone();
	}
}
