package se.lth.cs.tycho;

import org.junit.BeforeClass;
import org.junit.Test;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.settings.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class SimpleTest {
	private static final Path testPath = Paths.get("testdata").toAbsolutePath();
	private static ProgramTester tester;

	@BeforeClass
	public static void compile() throws IOException, Configuration.Builder.UnknownKeyException, InterruptedException {
		tester = ProgramTester.compile(testPath.resolve("idtest"), QID.of("Id"));
	}

	@Test
	public void testIdActorWithNetwork() throws IOException, InterruptedException {
		List<Path> file = Collections.singletonList(testPath.resolve("idtest/testdata"));
		tester.run(file, file);
	}
}
