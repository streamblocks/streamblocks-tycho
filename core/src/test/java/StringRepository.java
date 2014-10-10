import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.DeclKind;
import se.lth.cs.tycho.ir.decl.GlobalDecl;
import se.lth.cs.tycho.loader.SourceCodeRepository;
import se.lth.cs.tycho.loader.SourceCodeUnit;
import se.lth.cs.tycho.messages.MessageListener;
import se.lth.cs.tycho.parsing.cal.CalParser;
import se.lth.cs.tycho.parsing.cal.ParseException;

public class StringRepository implements SourceCodeRepository {

	private final Map<QID, List<SourceCodeUnit>> entities = new HashMap<>();
	private final Map<QID, List<SourceCodeUnit>> types = new HashMap<>();
	private final Map<QID, List<SourceCodeUnit>> vars = new HashMap<>();

	private NamespaceDecl parse(String string) {
		CalParser parser = new CalParser(new StringReader(string));
		try {
			return parser.CompilationUnit();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void add(String input) {
		NamespaceDecl ns = parse(input);
		SourceCodeUnit unit = new TreeUnit(ns);
		add(ns, QID.empty(), unit);
	}

	private void add(NamespaceDecl ns, QID parent, SourceCodeUnit unit) {
		QID qid = parent.concat(ns.getQID());
		for (NamespaceDecl child : ns.getNamespaceDecls()) {
			add(child, qid, unit);
		}
		for (GlobalDecl decl : ns.getDecls()) {
			QID declQID = qid.concat(QID.of(decl.getName()));
			createOrGetList(declQID, decl.getKind()).add(unit);
		}
	}

	private Map<QID, List<SourceCodeUnit>> getMap(DeclKind kind) {
		switch (kind) {
		case ENTITY:
			return entities;
		case TYPE:
			return types;
		case VAR:
			return vars;
		}
		return null;
	}

	private List<SourceCodeUnit> createOrGetList(QID qid, DeclKind kind) {
		Map<QID, List<SourceCodeUnit>> map = getMap(kind);
		if (!map.containsKey(qid)) {
			map.put(qid, new ArrayList<>());
		}
		return map.get(qid);
	}

	@Override
	public List<SourceCodeUnit> findUnits(QID qid, DeclKind kind) {
		return getMap(kind).getOrDefault(qid, Collections.emptyList());
	}

	@Override
	public boolean checkRepository(MessageListener messages) {
		return true;
	}

	public static class TreeUnit implements SourceCodeUnit {
		private static long next = 0;
		private final long number;
		private final NamespaceDecl tree;

		public TreeUnit(NamespaceDecl tree) {
			this.tree = tree;
			this.number = next++;
		}

		@Override
		public String getLocationDescription() {
			return "location-" + number;
		}

		@Override
		public NamespaceDecl load(MessageListener listener) {
			return tree;
		}
	}

}
