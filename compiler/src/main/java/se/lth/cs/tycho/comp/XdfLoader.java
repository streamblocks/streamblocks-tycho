package se.lth.cs.tycho.comp;

import org.xml.sax.SAXException;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.parsing.xdf.XDF2NLReader;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XdfLoader implements Loader {
	private final XDF2NLReader reader;
	private final Reporter reporter;
	private final List<Path> sourcePaths;
	private final Map<QID, List<SourceUnit>> sourceCache;

	public XdfLoader(Reporter reporter, List<Path> sourcePaths) {
		this.reader = new XDF2NLReader();
		this.reporter = reporter;
		this.sourcePaths = sourcePaths;
		this.sourceCache = new HashMap<>();
	}

	public List<SourceUnit> loadNamespace(QID qid) {
		return sourceCache.computeIfAbsent(qid, ns ->
				sourcePaths.stream()
						.flatMap(path -> loadNamespace(ns, path))
						.collect(Collectors.toList()));
	}

	private Stream<SourceUnit> loadNamespace(QID qid, Path path) {
		Path nsDir = path.resolve(qid.toPath());
		if (Files.isDirectory(nsDir)) {
			try {
				return Files.list(nsDir)
						.filter(Files::isRegularFile)
						.filter(f -> f.toString().endsWith(".xdf"))
						.map(f -> {
							String fileName = f.getFileName().toString();
							String id = fileName.substring(0, fileName.length() - ".xdf" .length());
							return loadNetworkFile(qid.concat(QID.of(id)), f);
						})
						.filter(Optional::isPresent)
						.map(Optional::get);
			} catch (IOException e) {
				reporter.report(new Diagnostic(Diagnostic.Kind.ERROR, e.getMessage()));
				return Stream.empty();
			}
		} else {
			return Stream.empty();
		}
	}

	private Optional<SourceUnit> loadNetworkFile(QID qid, Path file) {
		try (InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
			NamespaceDecl nsDecl = reader.read(is, qid);
			return Optional.of(new SourceFile(file, nsDecl, SourceUnit.InputLanguage.XDF));
		} catch (IOException | ParserConfigurationException | SAXException e) {
			reporter.report(new Diagnostic(Diagnostic.Kind.ERROR, e.getMessage()));
			return Optional.empty();
		}
	}

}
