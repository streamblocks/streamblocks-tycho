package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.reporting.Reporter;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.OnOffSetting;

import java.util.Arrays;
import java.util.List;

public interface Loader {
	List<SourceUnit> loadNamespace(QID qid);

	static Loader instance(Configuration configuration, Reporter reporter) {
		return new CombinedLoader(Arrays.asList(
				new CalLoader(reporter, configuration.get(Compiler.sourcePaths), configuration.get(followLinks)),
				new OrccLoader(reporter, configuration.get(Compiler.orccSourcePaths), configuration.get(followLinks)),
				new XdfLoader(reporter, configuration.get(Compiler.xdfSourcePaths))));
	}

	OnOffSetting followLinks = new OnOffSetting() {
		@Override
		public String getKey() {
			return "follow-links";
		}

		@Override
		public String getDescription() {
			return "Follow links when looking for source files.";
		}

		@Override
		public Boolean defaultValue(Configuration configuration) {
			return false;
		}
	};

}
