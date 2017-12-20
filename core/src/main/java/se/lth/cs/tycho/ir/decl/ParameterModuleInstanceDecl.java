package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.module.ModuleExpr;

import java.util.function.Consumer;

public class ParameterModuleInstanceDecl extends ModuleInstanceDecl {
	private final ModuleExpr moduleExpr;

	private ParameterModuleInstanceDecl(ModuleInstanceDecl original, String name, ModuleExpr moduleExpr) {
		super(original, name);
		this.moduleExpr = moduleExpr;
	}

	public ParameterModuleInstanceDecl(String name, ModuleExpr moduleExpr) {
		this(null, name, moduleExpr);
	}

	@Override
	public Decl withName(String name) {
		return null;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {

	}

	@Override
	public Decl transformChildren(Transformation transformation) {
		return null;
	}
}
