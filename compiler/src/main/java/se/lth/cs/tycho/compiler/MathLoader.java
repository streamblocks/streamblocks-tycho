//  @author Gareh Callanan
//
//  This class is a straight copy from the PreludeLoader.java class. 

package se.lth.cs.tycho.compiler;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.parsing.cal.CalParser;
import se.lth.cs.tycho.parsing.cal.ParseException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MathLoader implements Loader {
	private static final QID math = QID.of("math");
	private final Reporter reporter;
	private List<SourceUnit> mathUnits;

	public MathLoader(Reporter reporter) {
		this.reporter = reporter;
	}

	@Override
	public List<SourceUnit> loadNamespace(QID qid) {
		if (qid.equals(math)) {
			if (mathUnits == null) {
				InputStream mathStream = getMathInputStream();
				try {
					MathUnit mathUnit = new MathUnit(new CalParser(mathStream,"UTF-8").CompilationUnit());
                    mathUnits = new ArrayList<>();
					mathUnits.add(mathUnit);
				} catch (ParseException e) {
					reporter.report(new Diagnostic(Diagnostic.Kind.ERROR, "Could not parse the Cal math prelude, " + e.getMessage()));
				}
			}
			return mathUnits;
		} else {
			return Collections.emptyList();
		}
	}

	private static InputStream getMathInputStream() {
		return ClassLoader.getSystemResourceAsStream("cal_prelude/math_prelude.cal");
	}

    public static class MathUnit implements SourceUnit {
		private final NamespaceDecl namespace;

		public MathUnit(NamespaceDecl namespace) {
			this.namespace = namespace;
		}

		@Override
		public NamespaceDecl getTree() {
			return namespace;
		}

		@Override
		public SourceUnit withTree(NamespaceDecl ns) {
			return namespace == ns ? this : new MathUnit(ns);
		}

		@Override
		public String getLocation() {
			return "<math>";
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return getMathInputStream();
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
		public MathUnit clone() {
			try {
				return (MathUnit) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
