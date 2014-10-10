package se.lth.cs.tycho.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.DeclKind;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.LocalTypeDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.messages.Message;
import se.lth.cs.tycho.messages.MessageListener;
import se.lth.cs.tycho.parsing.cal.CalParser;
import se.lth.cs.tycho.parsing.cal.ParseException;

public class FileSystemCalRepository implements SourceCodeRepository {
	
	private final Path basePath;
	private final Map<QID, List<SourceCodeUnit>> entities;
	private final Map<QID, List<SourceCodeUnit>> types;
	private final Map<QID, List<SourceCodeUnit>> vars;

	public FileSystemCalRepository(Path basePath) {
		this.basePath = basePath;
		this.entities = new HashMap<>();
		this.types = new HashMap<>();
		this.vars = new HashMap<>();
	}
	
	private List<SourceCodeUnit> get(Map<QID, List<SourceCodeUnit>> map, QID name) {
		List<SourceCodeUnit> result = map.get(name);
		if (result == null) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableList(result);
		}
	}

	@Override
	public List<SourceCodeUnit> findUnits(QID name, DeclKind kind) {
		return get(entities, name);
	}

	private void addToRepo(Map<QID, List<SourceCodeUnit>> repo, QID qid, SourceCodeUnit unit) {
		List<SourceCodeUnit> units = repo.get(qid);
		if (units == null) {
			units = new ArrayList<>();
			repo.put(qid, units);
		}
		if (!units.contains(unit)) {
			units.add(unit);
		}
	}
	
	private static NamespaceDecl parse(Path p, MessageListener m) {
		try {
			CalParser parser = new CalParser(Files.newBufferedReader(p));
			return parser.CompilationUnit();
		} catch (IOException e) {
			m.report(new Message(e.getMessage(), Message.Kind.ERROR));
			return null;
		} catch (ParseException e) {
			m.report(new Message("Could not parse " + p.toAbsolutePath() + ", " + e.getMessage(), Message.Kind.ERROR));
			return null;
		}
	}

	private boolean scanFile(Path f, MessageListener messages) {
		CalCompilationUnit unit = new CalCompilationUnit(f);
		NamespaceDecl ns = parse(f, messages);
		if (ns == null) {
			return false;
		} else {
			buildRepo(unit, QID.empty(), ns);
			return true;
		}
	}

	private void buildRepo(SourceCodeUnit unit, QID parent, NamespaceDecl ns) {
		QID qid = parent.concat(ns.getQID());
		for (Decl d : ns.getDecls()) {
			QID declId = qid.concat(QID.of(d.getName()));
			if (d instanceof GlobalEntityDecl) {
				addToRepo(entities, declId, unit);
			} else if (d instanceof LocalVarDecl) {
				addToRepo(vars, declId, unit);
			} else if (d instanceof LocalTypeDecl) {
				addToRepo(types, declId, unit);
			}
		}
		for (NamespaceDecl child : ns.getNamespaceDecls()) {
			buildRepo(unit, qid, child);
		}
	}

	@Override
	public boolean checkRepository(MessageListener messages) {
		try {
			return Files.walk(basePath).filter(f -> f.getFileName().toString().endsWith(".cal"))
					.allMatch(f -> scanFile(f, messages));
		} catch (IOException e) {
			messages.report(new Message(e.getMessage(), Message.Kind.ERROR));
			return false;
		}
	}

	private static class CalCompilationUnit implements SourceCodeUnit {
		private final Path path;

		public CalCompilationUnit(Path path) {
			this.path = path;
		}

		@Override
		public NamespaceDecl load(MessageListener messages) {
			return parse(path, messages);
		}

		@Override
		public String getLocationDescription() {
			return path.toAbsolutePath().toString();
		}

	}

}
