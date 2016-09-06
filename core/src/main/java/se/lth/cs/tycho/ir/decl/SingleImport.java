package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

import java.util.Objects;
import java.util.function.Consumer;

public class SingleImport extends AbstractIRNode implements Import {
	private final Kind kind;
	private final QID globalName;
	private final String localName;

	private SingleImport(IRNode original, Kind kind, QID globalName, String localName) {
		super(original);
		this.kind = kind;
		this.globalName = globalName;
		this.localName = localName;
	}

	public SingleImport(Kind kind, QID globalName, String localName) {
		this(null, kind, globalName, localName);
	}

	public SingleImport copy(Kind kind, QID globalName, String localName) {
		if (this.kind == kind && Objects.equals(this.globalName, globalName) && Objects.equals(this.localName, localName)) {
			return this;
		} else {
			return new SingleImport(this, kind, globalName, localName);
		}
	}

	public Kind getKind() {
		return kind;
	}

	public QID getGlobalName() {
		return globalName;
	}

	public SingleImport withGlobalName(QID globalName) {
		return copy(kind, globalName, localName);
	}

	public String getLocalName() {
		return localName;
	}

	public SingleImport withLocalName(String localName) {
		return copy(kind, globalName, localName);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return this;
	}
}
