package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.parsing.orcc.ParseException;
import se.lth.cs.tycho.parsing.orcc.OrccParser;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrccLoader implements Loader {
	private final Reporter reporter;
	private final Map<QID, List<SourceUnit>> sourceCache;
	private final List<Path> directories;
	private Map<QID, List<Path>> fileRegister;

	public OrccLoader(Reporter reporter, List<Path> directories) {
		this.reporter = reporter;
		this.directories = directories;
		this.fileRegister = null;
		this.sourceCache = new HashMap<>();
	}

	private NamespaceDecl parse(Path p) {

		try(Reader reader = Files.newBufferedReader(p, Charset.forName("Latin1"))) {
			OrccParser parser = new OrccParser(reader);
			//parser.setOperatorPriorities(OrccParser.defaultPriorities());
			return parser.CompilationUnit();
		} catch (IOException e) {
			reporter.report(new Diagnostic(Diagnostic.Kind.ERROR, e.getMessage()));
			return null;
		} catch (ParseException e) {
			reporter.report(new Diagnostic(Diagnostic.Kind.ERROR, "Could not parse " + p + ", " + e.getMessage()));
			return null;
		}
	}

	private QID scanNamespaceDecl__temporarily_disabled(Path p) {
		NamespaceDecl ns = parse(p);
		if (ns != null) {
			return ns.getQID();
		} else {
			return null;
		}
	}

	private QID scanNamespaceDecl(Path p) {
		try (Reader reader = Files.newBufferedReader(p, Charset.forName("Latin1"))) {
			OrccParser parser = new OrccParser(reader);
			return parser.NamespaceScan();
		} catch (IOException e) {
			return null;
		} catch (ParseException e) {
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
					QID qid = scanNamespaceDecl(path);
					if (qid != null) {
						result.computeIfAbsent(qid, x -> new ArrayList<>()).add(path);
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
						.map(p -> new SourceFile(p, parse(p), SourceUnit.InputLanguage.ORCC))
						.collect(Collectors.toList()));
	}

}
