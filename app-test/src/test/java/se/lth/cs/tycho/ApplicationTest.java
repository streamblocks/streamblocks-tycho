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
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class ApplicationTest {
	private final TestDescription test;

    private static int testsSkipped = 0; // Comment this
    private final int EXPECTED_UNCOMPILED_TESTS = 38;

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
        Optional<ProgramTester> optionalTester = ProgramTester.compile(test, target);
        if (optionalTester.isPresent()) {
            ProgramTester tester = optionalTester.get();
            for (TestDescription.TestData data : test.getTestData()) {
                tester.run(data.getInput(), data.getReference(), temp);
            }
        } else {
            // Note by Gareth Callanan on 03/01/2023. A number of the unit tests do not compile, and if this
            // occurs the unit test is skipped, not marked as a failure. This is very annoying when trying to
            // create new unit tests and not being sure if they are passing because they work or because they
            // do not compile. I do not have time to fix the broken tests, but I have instead printed out
            // the broken tests. This makes it easier to figure out if your new test works or just does not
            // compile.
            //
            // Additionally, at my last check, exactly EXPECTED_UNCOMPILED_TESTS unit tests were failing to compile.
            // I track the number of failed compilations and throw an error if it exceeds the expected number,
            // this should prevent additional failing tests from being added accidentally.
            if (testsSkipped == 0) {
                System.out.println("NOTE: We expect exactly " + EXPECTED_UNCOMPILED_TESTS +
                        " tests to fail compilation. Any more and an error will be thrown. See code comments by this " +
                        "message for more information on why this happens.");
            }

            testsSkipped++;
            System.out.println(testsSkipped + ": Could not compile test '" + test.getEntity().toString() + "'. " +
                "Skipping...");
            if (testsSkipped > EXPECTED_UNCOMPILED_TESTS) {
				throw new RuntimeException("It is expected that exactly " + EXPECTED_UNCOMPILED_TESTS + " tests fail " +
						"to compile. This is test number " + testsSkipped + " to fail. This means a new non-compiling " +
						"unit test has been added to Tÿcho and needs to be fixed in order for building to succeed.");
			}
        }
    }
}
