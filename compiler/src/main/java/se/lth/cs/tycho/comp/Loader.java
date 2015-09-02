package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.reporting.Reporter;
import se.lth.cs.tycho.settings.Configuration;

import java.util.List;

public interface Loader {
	List<SourceUnit> loadNamespace(QID qid);

	static Loader instance(Configuration configuration, Reporter reporter) {
		return new CalLoader(reporter, configuration.get(Compiler.sourcePaths));
	}

}
