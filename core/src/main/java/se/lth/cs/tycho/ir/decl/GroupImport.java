package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

import java.util.Objects;
import java.util.function.Consumer;

public class GroupImport extends AbstractIRNode implements Import {
	private final Import.Kind kind;
	private final QID globalName;

	public GroupImport(Import.Kind kind, QID globalName) {
		this(null, kind, globalName);
	}

	private GroupImport(IRNode original, Import.Kind kind, QID globalName) {
		super(original);
		this.kind = kind;
		this.globalName = globalName;
	}

	public Import.Kind getKind() {
		return kind;
	}

	public QID getGlobalName() {
		return globalName;
	}

	public GroupImport withGlobalName(QID globalName) {
		if (Objects.equals(this.globalName, globalName)) {
			return this;
		} else {
			return new GroupImport(this, kind, globalName);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {

	}

	@Override
	public GroupImport transformChildren(Transformation transformation) {
		return this;
	}
}
