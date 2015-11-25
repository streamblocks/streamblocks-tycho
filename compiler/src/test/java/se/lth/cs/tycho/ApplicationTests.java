package se.lth.cs.tycho;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import se.lth.cs.tycho.settings.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class ApplicationTests {
	private final TestDescription test;

	@Parameterized.Parameters(name = "{0}")
	public static Iterable<Object[]> data() throws IOException {
		List<Path> testFiles = Files.walk(Paths.get("testdata"))
				.filter(Files::isRegularFile)
				.filter(path -> path.getFileName().toString().endsWith(".test"))
				.collect(Collectors.toList());
		List<Object[]> testParameters = new ArrayList<>();
		for (Path test : testFiles) {
			TestDescription testDescription = TestDescription.fromFile(test).resolvePaths(test);
			testParameters.add(new Object[] {testDescription.getDescription(), testDescription});
		}
		return testParameters;
	}

	public ApplicationTests(String description, TestDescription test) {
		this.test = test;
	}

	@Test
	public void test() throws IOException, Configuration.Builder.UnknownKeyException, InterruptedException {
		ProgramTester tester = ProgramTester.compile(test.getSourcePaths(), test.getEntity());
		for (TestDescription.TestData data : test.getTestData()) {
			tester.run(data.getInput(), data.getReference());
		}
	}
}
