package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.parsing.cal.CalParser;
import se.lth.cs.tycho.parsing.cal.ParseException;
import se.lth.cs.tycho.parsing.cal.SimpleCharStream;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CalLoader implements Loader {
	private final Reporter reporter;
	private Map<QID, List<Path>> fileRegister;
	private final Map<QID, List<SourceUnit>> sourceCache;
	private final List<Path> directories;

	public CalLoader(Reporter reporter, List<Path> directories) {
		this.reporter = reporter;
		this.directories = directories;
		this.fileRegister = null;
		this.sourceCache = new HashMap<>();
	}

	private NamespaceDecl parse(Path p, Diagnostic.Kind errorKind, boolean withOpParsing) {

		try {
			CalParser parser = new CalParser(Files.newBufferedReader(p));
			if (withOpParsing) parser.setOperatorPriorities(CalParser.defaultPriorities());
			return parser.CompilationUnit();
		} catch (IOException e) {
			reporter.report(new Diagnostic(errorKind, e.getMessage()));
			return null;
		} catch (ParseException e) {
			reporter.report(new Diagnostic(errorKind, "Could not parse " + p.toAbsolutePath() + ", " + e.getMessage()));
			return null;
		}
	}

	private void initFileRegister() {
		Map<QID, List<Path>> result = new HashMap<>();
		directories.stream()
				.flatMap(p -> {
					try {
						return Files.walk(p);
					} catch (IOException e) {
						reporter.report(new Diagnostic(Diagnostic.Kind.WARNING, e.getMessage()));
						return Stream.empty();
					}
				})
				.filter(Files::isRegularFile)
				.filter(p -> p.toString().endsWith(".cal"))
				.distinct()
				.forEach(path -> {
					NamespaceDecl root = parse(path, Diagnostic.Kind.WARNING, false);
					if (root != null) {
						result.computeIfAbsent(root.getQID(), x -> new ArrayList<>()).add(path);
					}
				});
		this.fileRegister = result;
	}

	@Override
	public List<SourceUnit> loadNamespace(QID qid) {
		if (fileRegister == null) {
			initFileRegister();
		}
		return sourceCache.computeIfAbsent(qid, key ->
				fileRegister.getOrDefault(key, Collections.emptyList()).stream()
						.map(p -> new SourceFile(p, parse(p, Diagnostic.Kind.ERROR, true)))
						.collect(Collectors.toList()));
	}

}
