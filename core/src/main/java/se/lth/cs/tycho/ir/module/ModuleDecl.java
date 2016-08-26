package se.lth.cs.tycho.ir.module;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ModuleDecl extends AbstractIRNode {
	private final boolean isAbstract;
	private final boolean isFinal;
	private final String name;
	private final ImmutableList<VarDecl> valueParameters;
	private final ImmutableList<TypeDecl> typeParameters;
	private final ImmutableList<ModuleExpr> superModules;
	private final ImmutableList<ModuleExpr> moduleConstraints;
	private final ImmutableList<Expression> valueConstraints;
	private final ImmutableList<VarDecl> valueComponents;
	private final ImmutableList<TypeDecl> typeComponents;

	private ModuleDecl(IRNode original, boolean isAbstract, boolean isFinal, String name, List<VarDecl> valueParameters, List<TypeDecl> typeParameters, List<ModuleExpr> superModules, List<ModuleExpr> moduleConstraints, List<Expression> valueConstraints, List<VarDecl> valueComponents, List<TypeDecl> typeComponents) {
		super(original);
		this.isAbstract = isAbstract;
		this.isFinal = isFinal;
		this.name = name;
		this.valueParameters = ImmutableList.from(valueParameters);
		this.typeParameters = ImmutableList.from(typeParameters);
		this.superModules = ImmutableList.from(superModules);
		this.moduleConstraints = ImmutableList.from(moduleConstraints);
		this.valueConstraints = ImmutableList.from(valueConstraints);
		this.valueComponents = ImmutableList.from(valueComponents);
		this.typeComponents = ImmutableList.from(typeComponents);
	}

	public ModuleDecl(boolean isAbstract, boolean isFinal, String name, List<VarDecl> valueParameters, List<TypeDecl> typeParameters, List<ModuleExpr> superModules, List<ModuleExpr> moduleConstraints, List<Expression> valueConstraints, List<VarDecl> valueComponents, List<TypeDecl> typeComponents) {
		this(null, isAbstract, isFinal, name, valueParameters, typeParameters, superModules, moduleConstraints, valueConstraints, valueComponents, typeComponents);
	}

	public ModuleDecl(String name) {
		this(false, false, name, null, null, null, null, null, null, null);
	}

	public ModuleDecl copy(boolean isAbstract, boolean isFinal, String name, List<VarDecl> valueParameters, List<TypeDecl> typeParameters, List<ModuleExpr> superModules, List<ModuleExpr> moduleConstraints, List<Expression> valueConstraints, List<VarDecl> valueComponents, List<TypeDecl> typeComponents) {
		if (this.isAbstract == isAbstract &&
				this.isFinal == isFinal &&
				Objects.equals(this.name, name) &&
				Lists.sameElements(this.valueParameters, valueParameters) &&
				Lists.sameElements(this.typeParameters, typeParameters) &&
				Lists.sameElements(this.superModules, superModules) &&
				Lists.sameElements(this.moduleConstraints, moduleConstraints) &&
				Lists.sameElements(this.valueConstraints, valueConstraints) &&
				Lists.sameElements(this.valueComponents, valueComponents) &&
				Lists.sameElements(this.typeComponents, typeComponents)) {
			return this;
		} else {
			return new ModuleDecl(this, isAbstract, isFinal, name, valueParameters, typeParameters, superModules, moduleConstraints, valueConstraints, valueComponents, typeComponents);
		}
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public ModuleDecl withAbstract(boolean isAbstract) {
		return copy(isAbstract, isFinal, name, valueParameters, typeParameters, superModules, moduleConstraints, valueConstraints, valueComponents, typeComponents);
	}

	public boolean isFinal() {
		return isFinal;
	}

	public ModuleDecl withFinal(boolean isFinal) {
		return copy(isAbstract, isFinal, name, valueParameters, typeParameters, superModules, moduleConstraints, valueConstraints, valueComponents, typeComponents);
	}

	public String getName() {
		return name;
	}

	public ModuleDecl withName(String name) {
		return copy(isAbstract, isFinal, name, valueParameters, typeParameters, superModules, moduleConstraints, valueConstraints, valueComponents, typeComponents);
	}

	public ImmutableList<VarDecl> getValueParameters() {
		return valueParameters;
	}

	public ModuleDecl withValueParameters(List<VarDecl> valueParameters) {
		return copy(isAbstract, isFinal, name, valueParameters, typeParameters, superModules, moduleConstraints, valueConstraints, valueComponents, typeComponents);
	}

	public ImmutableList<TypeDecl> getTypeParameters() {
		return typeParameters;
	}

	public ModuleDecl withTypeParameters(List<TypeDecl> typeParameters) {
		return copy(isAbstract, isFinal, name, valueParameters, typeParameters, superModules, moduleConstraints, valueConstraints, valueComponents, typeComponents);
	}

	public ImmutableList<ModuleExpr> getSuperModules() {
		return superModules;
	}

	public ModuleDecl withSuperModules(List<ModuleExpr> superModules) {
		return copy(isAbstract, isFinal, name, valueParameters, typeParameters, superModules, moduleConstraints, valueConstraints, valueComponents, typeComponents);
	}

	public ImmutableList<ModuleExpr> getModuleConstraints() {
		return moduleConstraints;
	}

	public ModuleDecl withModuleConstraints(List<ModuleExpr> moduleConstraints) {
		return copy(isAbstract, isFinal, name, valueParameters, typeParameters, superModules, moduleConstraints, valueConstraints, valueComponents, typeComponents);
	}

	public ImmutableList<Expression> getValueConstraints() {
		return valueConstraints;
	}

	public ModuleDecl withValueConstraints(List<Expression> valueConstraints) {
		return copy(isAbstract, isFinal, name, valueParameters, typeParameters, superModules, moduleConstraints, valueConstraints, valueComponents, typeComponents);
	}

	public ImmutableList<VarDecl> getValueComponents() {
		return valueComponents;
	}

	public ModuleDecl withValueComponents(List<VarDecl> valueComponents) {
		return copy(isAbstract, isFinal, name, valueParameters, typeParameters, superModules, moduleConstraints, valueConstraints, valueComponents, typeComponents);
	}

	public ImmutableList<TypeDecl> getTypeComponents() {
		return typeComponents;
	}

	public ModuleDecl withTypeComponents(List<TypeDecl> typeComponents) {
		return copy(isAbstract, isFinal, name, valueParameters, typeParameters, superModules, moduleConstraints, valueConstraints, valueComponents, typeComponents);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		valueParameters.forEach(action);
		typeParameters.forEach(action);
		superModules.forEach(action);
		moduleConstraints.forEach(action);
		valueConstraints.forEach(action);
		valueComponents.forEach(action);
		typeComponents.forEach(action);
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(isAbstract,
				isFinal,
				name,
				transformation.mapChecked(VarDecl.class ,valueParameters),
				transformation.mapChecked(TypeDecl.class ,typeParameters),
				transformation.mapChecked(ModuleExpr.class ,superModules),
				transformation.mapChecked(ModuleExpr.class ,moduleConstraints),
				transformation.mapChecked(Expression.class ,valueConstraints),
				transformation.mapChecked(VarDecl.class ,valueComponents),
				transformation.mapChecked(TypeDecl.class ,typeComponents));
	}
}
