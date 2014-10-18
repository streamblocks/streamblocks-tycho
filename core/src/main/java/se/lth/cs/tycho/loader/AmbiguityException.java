package se.lth.cs.tycho.loader;

import java.util.List;
import java.util.stream.Collectors;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.DeclKind;

public class AmbiguityException extends RuntimeException {
	private static final long serialVersionUID = 2335537397415197940L;
	private final DeclKind kind;
	private final QID qid;
	private final List<String> units;

	public AmbiguityException(DeclKind kind, QID qid, List<String> units) {
		this.kind = kind;
		this.qid = qid;
		this.units = units;
	}

	public String getMessage() {
		String list = units.stream().distinct().collect(Collectors.joining("\n"));
		return "There are several definitions available for " + kind + " " + qid + " in the following unit\n" + list;
	}

}
