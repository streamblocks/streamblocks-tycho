package se.lth.cs.tycho;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import se.lth.cs.tycho.ir.QID;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestDescription {
	private static final Gson gson = new GsonBuilder()
			.registerTypeAdapter(Path.class, new PathTypeAdapter())
			.registerTypeAdapter(QID.class, new QIDTypeAdapter())
			.create();

	private String description;
	@SerializedName("source-paths")
	private List<Path> sourcePaths;
	@SerializedName("check-paths")
	private List<Path> checkPaths;
	@SerializedName("orcc-source-paths")
	private List<Path> orccSourcePaths;
	@SerializedName("xdf-source-paths")
	private List<Path> xdfSourcePaths;
	@SerializedName("external-sources")
	private List<Path> externalSources;

	@SerializedName("experimental-network-elaboration")
	private String experimentalNetworkElaborationFlag;
	private QID entity;
	@SerializedName("test-data")
	private List<TestData> testData;

	public TestDescription() {
	}

	public String getDescription() {
		return description;
	}

	public String getExperimentalNetworkElaborationFlag() { return experimentalNetworkElaborationFlag == null ? "off" : experimentalNetworkElaborationFlag;};

	public List<Path> getSourcePaths() {
		return sourcePaths == null ? Collections.emptyList() : sourcePaths;
	}

	public List<Path> getCheckPaths() {
		return checkPaths == null ? Collections.emptyList() : checkPaths;
	}

	public List<Path> getOrccSourcePaths() {
		return orccSourcePaths == null ? Collections.emptyList() : orccSourcePaths;
	}

	public List<Path> getXDFSourcePaths() {
		return xdfSourcePaths == null ? Collections.emptyList() : xdfSourcePaths;
	}

	public List<Path> getExternalSources() {
		return externalSources == null ? Collections.emptyList() : externalSources;
	}

	public QID getEntity() {
		return entity;
	}

	public List<TestData> getTestData() {
		return testData == null ? Collections.emptyList() : testData;
	}

	public TestDescription resolvePaths(Path testFile) {
		Path directory = testFile.getParent();
		TestDescription result = new TestDescription();
		result.description = description;
		result.sourcePaths = getSourcePaths().stream().map(directory::resolve).collect(Collectors.toList());
		result.checkPaths = getCheckPaths().stream().map(directory::resolve).collect(Collectors.toList());
		result.xdfSourcePaths = getXDFSourcePaths().stream().map(directory::resolve).collect(Collectors.toList());
		result.orccSourcePaths = getOrccSourcePaths().stream().map(directory::resolve).collect(Collectors.toList());
		result.externalSources = getExternalSources().stream().map(directory::resolve).collect(Collectors.toList());
		result.entity = entity;
		result.testData = getTestData().stream().map(d -> d.resolvePaths(testFile)).collect(Collectors.toList());
		result.experimentalNetworkElaborationFlag = getExperimentalNetworkElaborationFlag();
		return result;
	}

	public static TestDescription fromFile(Path file) throws IOException {
		return gson.fromJson(Files.newBufferedReader(file), TestDescription.class);
	}

	public String toString() {
		return gson.toJson(this);
	}

	public static class TestData {
		private List<Path> input;
		private List<Path> reference;

		public TestData() {

		}

		public TestData(List<Path> input, List<Path> reference) {
			this.input = input;
			this.reference = reference;
		}

		public List<Path> getInput() {
			return input;
		}

		public List<Path> getReference() {
			return reference;
		}

		public TestData resolvePaths(Path testFile) {
			Path directory = testFile.getParent();
			TestData result = new TestData();
			result.input = input.stream().map(directory::resolve).collect(Collectors.toList());
			result.reference = reference.stream().map(directory::resolve).collect(Collectors.toList());
			return result;
		}
	}
}
