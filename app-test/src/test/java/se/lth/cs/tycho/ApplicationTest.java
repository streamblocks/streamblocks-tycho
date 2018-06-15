package se.lth.cs.tycho;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import se.lth.cs.tycho.settings.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class ApplicationTest {
	private final TestDescription test;

	@BeforeClass
	public static void setUp() throws Exception {
		Path testdata = Paths.get("testdata");
		Path orcApps = testdata.resolve("orc-apps");
		System.out.println("Path of orc-apps: " + orcApps);
		if (!Files.list(orcApps).findAny().isPresent()) {
			System.out.println("Directory orc-apps is empty.");
			List<String> command = new ArrayList<>();
			command.addAll(Arrays.asList("git submodule update --init".split(" ")));
			command.add(orcApps.toAbsolutePath().toString());
			Process process = new ProcessBuilder(command).inheritIO().start();
			System.out.println("Running command: " + command);
			int result = process.waitFor();
			if (result != 0) {
				Files.delete(orcApps);
				Assert.fail("Could not clone orc-apps from github.com");
			}
		}
	}

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

	public ApplicationTest(String description, TestDescription test) {
		this.test = test;
	}

	@Test
	public void test() throws IOException, Configuration.Builder.UnknownKeyException, InterruptedException {
		Path target = Files.createTempDirectory(test.getEntity().toString());
		Path temp = Files.createTempDirectory("temp");
		ProgramTester tester = ProgramTester.compile(test, target);
		for (TestDescription.TestData data : test.getTestData()) {
			tester.run(data.getInput(), data.getReference(), temp);
		}
	}
}
