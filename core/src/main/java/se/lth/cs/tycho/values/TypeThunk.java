package se.lth.cs.tycho.values;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.TypeExpr;

public class TypeThunk {

	private final TypeExpr type;
	private final Environment<ValueThunk> valueEnv;
	private final Environment<TypeThunk> typeEnv;
	private final NamespaceDecl origin;

	public TypeThunk(TypeExpr type, Environment<ValueThunk> valueEnv, Environment<TypeThunk> typeEnv, NamespaceDecl origin) {
		this.type = type;
		this.valueEnv = valueEnv;
		this.typeEnv = typeEnv;
		this.origin = origin;
	}

	public TypeExpr getType() {
		return type;
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

}
