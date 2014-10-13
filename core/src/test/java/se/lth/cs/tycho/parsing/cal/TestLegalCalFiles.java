package se.lth.cs.tycho.parsing.cal;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import se.lth.cs.tycho.ir.NamespaceDecl;

@RunWith(Parameterized.class)
public class TestLegalCalFiles {
	private static List<Path> roots() {
		return Arrays.asList(Paths.get("../../orc-apps/RVC"), Paths.get("../../caltoopia/org.caltoopia.tests/cal-src"));
	}

	@Parameters(name = "{index}: Parsing {0}")
	public static List<Path[]> files() {
		return roots().stream()
				.flatMap(f -> getCalPaths(f))
				.map(f -> new Path[] { f })
				.collect(Collectors.toList());
	}

	private static Stream<Path> getCalPaths(Path base) {
		try {
			return Files.walk(base)
					.filter(f -> f.getFileName()
							.toString()
							.endsWith(".cal"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private final Path input;

	public TestLegalCalFiles(Path input) {
		this.input = input;
	}

	@Test
	public void testParseCalFile() throws IOException, ParseException {
		CalParser parser = new CalParser(Files.newInputStream(input));
		NamespaceDecl ns = parser.CompilationUnit();
		assertNotNull(ns);
	}

}
