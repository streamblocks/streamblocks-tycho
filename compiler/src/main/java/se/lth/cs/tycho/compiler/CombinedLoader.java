package se.lth.cs.tycho.compiler;

import se.lth.cs.tycho.ir.QID;

import java.util.List;
import java.util.stream.Collectors;

public class CombinedLoader implements Loader {

	private final List<Loader> loaders;

	public CombinedLoader(List<Loader> loaders) {
		this.loaders = loaders;
	}

	@Override
	public List<SourceUnit> loadNamespace(QID qid) {
		return loaders.stream()
				.flatMap(loader -> loader.loadNamespace(qid).stream())
				.collect(Collectors.toList());
	}
}
