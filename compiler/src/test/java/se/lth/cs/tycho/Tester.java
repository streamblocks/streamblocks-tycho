package se.lth.cs.tycho;

import se.lth.cs.tycho.settings.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class Tester {
	private final List<TestDescription> descriptions;
	private final Path targetPath;

	public Tester(List<TestDescription> descriptions, Path targetPath) {
		this.descriptions = descriptions;
		this.targetPath = targetPath;
	}

	public void run() throws InterruptedException, IOException, Configuration.Builder.UnknownKeyException {
		for (TestDescription test : descriptions) {
			ProgramTester tester = ProgramTester.compile(test.getSourcePaths(), test.getExternalSources(), test.getEntity(), targetPath);
			for (TestDescription.TestData data : test.getTestData()) {
				tester.run(data.getInput(), data.getReference(), targetPath);
			}
		}
	}

	private static void printUsage() {
		System.out.printf("Usage: java %s [options] <test-descriptions>\n", Tester.class.getCanonicalName());
		System.out.println();
		System.out.println("Available options:");
		System.out.println("--help");
		System.out.println("\tPrints this help message and exits.");
		System.out.println("--target-path <path>");
		System.out.println("\tOutput directory for the compiled files.");
	}

	public static void main(String[] args) {
		Deque<String> argDeque = new ArrayDeque<>(Arrays.asList(args));
		Path targetPath = null;
		while (!argDeque.isEmpty() && argDeque.getFirst().startsWith("--")) {
			String opt = argDeque.removeFirst();
			switch (opt) {
				case "--target-path":
					if (!argDeque.isEmpty()) {
						targetPath = Paths.get(argDeque.removeFirst());
					} else {
						printUsage();
						System.exit(1);
					}
					break;
				case "--help":
					printUsage();
					System.exit(0);
					break;
				default:
					System.err.println("Unknown option: "+opt);
					System.exit(1);
			}
		}
		List<TestDescription> testDescriptions = new ArrayList<>();
		for (String arg : argDeque) {
			try {
				Path testFile = Paths.get(arg);
				TestDescription test = TestDescription.fromFile(testFile).resolvePaths(testFile);
				testDescriptions.add(test);
			} catch (IOException e) {
				System.err.printf("Could not read test description from \'%s\':\n", arg);
				System.err.println(e.getMessage());
				System.exit(1);
			}
		}
		if (testDescriptions.isEmpty()) {
			printUsage();
			System.exit(1);
		}
		if (targetPath == null) {
			try {
				targetPath = Files.createTempDirectory("tychoc-test");
			} catch (IOException e) {
				System.err.println("Could not create temporary directory.");
				System.exit(1);
			}
		}
		Tester t = new Tester(testDescriptions, targetPath.toAbsolutePath());
		try {
			t.run();
		} catch (InterruptedException | IOException | Configuration.Builder.UnknownKeyException e) {
			System.err.println("Error: " + e.getMessage());
			System.exit(1);
		} catch (AssertionError e) {
			System.err.println("Test failed: " + e.getMessage());
		}
	}
}
