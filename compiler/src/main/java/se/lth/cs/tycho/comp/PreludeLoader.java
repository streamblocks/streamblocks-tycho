package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.parsing.cal.CalParser;
import se.lth.cs.tycho.parsing.cal.ParseException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class PreludeLoader implements Loader {
	private static final QID prelude = QID.of("prelude");
	private final Reporter reporter;
	private List<SourceUnit> preludeUnits;

	public PreludeLoader(Reporter reporter) {
		this.reporter = reporter;
	}

	@Override
	public List<SourceUnit> loadNamespace(QID qid) {
		if (qid.equals(prelude)) {
			if (preludeUnits == null) {
				InputStream stream = getPreludeInputStream();
				try {
					PreludeUnit preludeUnit = new PreludeUnit(new CalParser(stream).CompilationUnit());
					preludeUnits = Collections.singletonList(preludeUnit);
				} catch (ParseException e) {
					reporter.report(new Diagnostic(Diagnostic.Kind.ERROR, "Could not parse the Cal prelude, " + e.getMessage()));
					preludeUnits = Collections.emptyList();
				}
			}
			return preludeUnits;
		} else {
			return Collections.emptyList();
		}
	}

	private static InputStream getPreludeInputStream() {
		return ClassLoader.getSystemResourceAsStream("cal_prelude/prelude.cal");
	}

	public static class PreludeUnit implements SourceUnit {
		private final NamespaceDecl namespace;

		public PreludeUnit(NamespaceDecl namespace) {
			this.namespace = namespace;
		}

		@Override
		public NamespaceDecl getTree() {
			return namespace;
		}

		@Override
		public SourceUnit withTree(NamespaceDecl ns) {
			return namespace == ns ? this : new PreludeUnit(ns);
		}

		@Override
		public String getLocation() {
			return "<prelude>";
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return getPreludeInputStream();
		}

		@Override
		public boolean isSynthetic() {
			return false;
		}

		@Override
		public InputLanguage getLanguage() {
			return InputLanguage.CAL;
		}

		@Override
		public PreludeUnit clone() {
			try {
				return (PreludeUnit) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
