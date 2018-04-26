package se.lth.cs.tycho.compiler;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.parsing.cal.CalParser;
import se.lth.cs.tycho.parsing.cal.ParseException;
import se.lth.cs.tycho.parsing.cal.Token;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CalLoader implements Loader {
	private final Reporter reporter;
	private final Map<QID, List<SourceUnit>> sourceCache;
	private final List<Path> directories;
	private final boolean followLinks;
	private Map<QID, List<Path>> fileRegister;

	public CalLoader(Reporter reporter, List<Path> directories, boolean followLinks) {
		this.reporter = reporter;
		this.directories = directories;
		this.followLinks = followLinks;
		this.fileRegister = null;
		this.sourceCache = new HashMap<>();
	}

	private String tokenSequence(int[] tokens, String[] tokenImage) {
		return Arrays.stream(tokens)
				.mapToObj(t -> tokenImage[t])
				.collect(Collectors.joining(" "));
	}

	private int maxLen(int[][] data) {
		return Arrays.stream(data)
				.mapToInt(a -> a.length)
				.max().orElse(0);
	}

	private String tokenSeq(Token first, int n, String[] tokenImage) {
		if (n <= 0) {
			return "";
		} else if (n == 1) {
			return tokenImage[first.kind];
		} else {
			return tokenImage[first.kind] + " " + tokenSeq(first.next, n-1, tokenImage);
		}
	}

	private String parseExceptionMessage(ParseException e) {
		int maxLen = maxLen(e.expectedTokenSequences);
		String msg = "Encountered " + tokenSeq(e.currentToken.next, maxLen, e.tokenImage);
		if (e.expectedTokenSequences.length == 1) {
			msg += " but expected " + tokenSequence(e.expectedTokenSequences[0], e.tokenImage) + ".";
		} else {
			msg += " but expected one of the following:\n\t" + Arrays.stream(e.expectedTokenSequences)
					.map(seq -> tokenSequence(seq, e.tokenImage))
					.collect(Collectors.joining("\n\t"));
		}
		return msg;
	}

	private Diagnostic toDiagnostic(Path p, ParseException e) {
		return new Diagnostic(
				Diagnostic.Kind.ERROR,
				parseExceptionMessage(e),
				p.toString(),
				() -> Files.newInputStream(p),
				e.currentToken.next.beginLine,
				e.currentToken.next.beginColumn,
				e.currentToken.next.endLine,
				e.currentToken.next.endColumn
		);
	}

	private NamespaceDecl parse(Path p) {
		try {
			CalParser parser = new CalParser(Files.newBufferedReader(p));
			return parser.CompilationUnit();
		} catch (IOException e) {
			reporter.report(new Diagnostic(Diagnostic.Kind.ERROR, "Error while reading file " + p.toAbsolutePath()));
			return null;
		} catch (ParseException e) {
			reporter.report(toDiagnostic(p, e));
			return null;
		}
	}

	private QID scanNamespaceDecl(Path p) {
		NamespaceDecl ns = parse(p);
		return ns == null ? null : ns.getQID();
	}

	private void initFileRegister() {
		Map<QID, List<Path>> result = new HashMap<>();
		directories.stream()
				.flatMap(p -> {
					try {
						return followLinks ? Files.walk(p, FileVisitOption.FOLLOW_LINKS) : Files.walk(p);
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
						.map(p -> new SourceFile(p, parse(p), SourceUnit.InputLanguage.CAL))
						.collect(Collectors.toList()));
	}

}
