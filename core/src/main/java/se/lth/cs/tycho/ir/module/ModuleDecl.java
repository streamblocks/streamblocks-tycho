package se.lth.cs.tycho.ir.module;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParameterModuleInstanceDecl;
import se.lth.cs.tycho.ir.decl.ParameterTypeDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ModuleDecl extends AbstractIRNode {
	private final String name;
	private final ImmutableList<ParameterVarDecl> valueParameters;
	private final ImmutableList<ParameterTypeDecl> typeParameters;
	private final ImmutableList<ParameterModuleInstanceDecl> moduleParameters;
	private final ImmutableList<ModuleExpr> superModules;
	private final ImmutableList<LocalVarDecl> valueComponents;
	private final ImmutableList<TypeDecl> typeComponents;

	private ModuleDecl(IRNode original, String name, List<ParameterVarDecl> valueParameters, List<ParameterTypeDecl> typeParameters, List<ParameterModuleInstanceDecl> moduleParameters, List<ModuleExpr> superModules, List<LocalVarDecl> valueComponents, List<TypeDecl> typeComponents) {
		super(original);
		this.name = name;
		this.valueParameters = ImmutableList.from(valueParameters);
		this.typeParameters = ImmutableList.from(typeParameters);
		this.moduleParameters = ImmutableList.from(moduleParameters);
		this.superModules = ImmutableList.from(superModules);
		this.valueComponents = ImmutableList.from(valueComponents);
		this.typeComponents = ImmutableList.from(typeComponents);
	}

	public ModuleDecl(String name, List<ParameterVarDecl> valueParameters, List<ParameterTypeDecl> typeParameters, List<ParameterModuleInstanceDecl> moduleParameters, List<ModuleExpr> superModules, List<LocalVarDecl> valueComponents, List<TypeDecl> typeComponents) {
		this(null, name, valueParameters, typeParameters, moduleParameters, superModules, valueComponents, typeComponents);
	}

	public ModuleDecl copy(String name, List<ParameterVarDecl> valueParameters, List<ParameterTypeDecl> typeParameters, List<ParameterModuleInstanceDecl> moduleParameters, List<ModuleExpr> superModules, List<LocalVarDecl> valueComponents, List<TypeDecl> typeComponents) {
		if (Objects.equals(this.name, name) &&
				Lists.sameElements(this.valueParameters, valueParameters) &&
				Lists.sameElements(this.typeParameters, typeParameters) &&
				Lists.sameElements(this.moduleParameters, moduleParameters) &&
				Lists.sameElements(this.superModules, superModules) &&
				Lists.sameElements(this.valueComponents, valueComponents) &&
				Lists.sameElements(this.typeComponents, typeComponents)) {
			return this;
		} else {
			return new ModuleDecl(this, name, valueParameters, typeParameters, moduleParameters, superModules, valueComponents, typeComponents);
		}
	}

	public String getName() {
		return name;
	}

	public ModuleDecl withName(String name) {
		return copy(name, valueParameters, typeParameters, moduleParameters, superModules, valueComponents, typeComponents);
	}

	public ImmutableList<ParameterVarDecl> getValueParameters() {
		return valueParameters;
	}

	public ModuleDecl withValueParameters(List<ParameterVarDecl> valueParameters) {
		return copy(name, valueParameters, typeParameters, moduleParameters, superModules, valueComponents, typeComponents);
	}

	public ImmutableList<ParameterTypeDecl> getTypeParameters() {
		return typeParameters;
	}

	public ModuleDecl withTypeParameters(List<ParameterTypeDecl> typeParameters) {
		return copy(name, valueParameters, typeParameters, moduleParameters, superModules, valueComponents, typeComponents);
	}

	public ImmutableList<ParameterModuleInstanceDecl> getModuleParameters() {
		return moduleParameters;
	}

	public ModuleDecl withModuleParameters(List<ParameterModuleInstanceDecl> moduleConstraints) {
		return copy(name, valueParameters, typeParameters, moduleConstraints, superModules, valueComponents, typeComponents);
	}

	public ImmutableList<ModuleExpr> getSuperModules() {
		return superModules;
	}

	public ModuleDecl withSuperModules(List<ModuleExpr> superModules) {
		return copy(name, valueParameters, typeParameters, moduleParameters, superModules, valueComponents, typeComponents);
	}

	public ImmutableList<LocalVarDecl> getValueComponents() {
		return valueComponents;
	}

	public ModuleDecl withValueComponents(List<LocalVarDecl> valueComponents) {
		return copy(name, valueParameters, typeParameters, moduleParameters, superModules, valueComponents, typeComponents);
	}

	public ImmutableList<TypeDecl> getTypeComponents() {
		return typeComponents;
	}

	public ModuleDecl withTypeComponents(List<TypeDecl> typeComponents) {
		return copy(name, valueParameters, typeParameters, moduleParameters, superModules, valueComponents, typeComponents);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		valueParameters.forEach(action);
		typeParameters.forEach(action);
		moduleParameters.forEach(action);
		superModules.forEach(action);
		valueComponents.forEach(action);
		typeComponents.forEach(action);
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(name,
				transformation.mapChecked(ParameterVarDecl.class ,valueParameters),
				transformation.mapChecked(ParameterTypeDecl.class ,typeParameters),
				transformation.mapChecked(ParameterModuleInstanceDecl.class , moduleParameters),
				transformation.mapChecked(ModuleExpr.class ,superModules),
				transformation.mapChecked(LocalVarDecl.class ,valueComponents),
				transformation.mapChecked(TypeDecl.class ,typeComponents));
	}
}
