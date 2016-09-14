package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;

public interface Import extends IRNode {
	Kind getKind();
	enum Kind {
		VAR("variable"), ENTITY("entity"), TYPE("type");

		private final String description;
		Kind(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}
}
