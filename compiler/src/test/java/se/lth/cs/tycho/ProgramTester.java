package se.lth.cs.tycho;

import org.apache.commons.io.IOUtils;
import se.lth.cs.tycho.comp.Compiler;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.SettingsManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class ProgramTester {
	private final Path executable;

	private ProgramTester(Path executable) {
		this.executable = executable;
	}

	public static ProgramTester compile(TestDescription test, Path target) throws IOException, Configuration.Builder.UnknownKeyException, InterruptedException {
		SettingsManager settings = Compiler.defaultSettingsManager();
		Configuration config = Configuration.builder(settings)
				.set(Compiler.sourcePaths, test.getSourcePaths())
				.set(Compiler.orccSourcePaths, test.getOrccSourcePaths())
				.set(Compiler.xdfSourcePaths, test.getXDFSourcePaths())
				.set(Compiler.targetPath, target)
				.build();
		Compiler comp = new Compiler(config);
		QID name = test.getEntity();
		if (comp.compile(name)) {
			Optional<Path> cfile = Files.list(target)
					.filter(file -> file.getFileName().toString().startsWith(name.getLast().toString()))
					.filter(file -> file.toString().endsWith(".c"))
					.findFirst();
			if (cfile.isPresent()) {
				List<String> command = new ArrayList<>();
				command.add("cc");
				command.add("-std=c99");
				command.add(cfile.get().getFileName().toString());
				test.getExternalSources().forEach(p -> command.add(p.toAbsolutePath().toString()));
				Process cc = new ProcessBuilder(command)
						.directory(target.toFile())
						.start();
				String error = IOUtils.toString(cc.getErrorStream());
				if (cc.waitFor() == 0) {
					Path aout = target.resolve("a.out");
					if (!Files.exists(aout)) {
						throw new RuntimeException("a.out does not exist");
					}
					return new ProgramTester(aout);
				} else {
					throw new RuntimeException(String.format("Compilation error in %s:\n%s", cfile.get(), error));
				}
			} else {
				throw new RuntimeException("Compilation error." + Files.list(target).map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", ", "[", "]")));
			}
		} else {
			throw new RuntimeException("Compilation error.");
		}
	}

	public void run(List<Path> input, List<Path> reference, Path temp) throws IOException, InterruptedException {
		List<Path> in = input.stream()
				.map(Path::toAbsolutePath)
				.collect(Collectors.toList());
		List<Path> out = reference.stream()
				.map(Path::getFileName)
				.map(temp::resolve)
				.collect(Collectors.toList());
		List<String> args = Stream.concat(Stream.of(executable), Stream.concat(in.stream(), out.stream()))
				.map(Path::toString)
				.collect(Collectors.toList());
		Process program = new ProcessBuilder(args)
				.directory(temp.toFile())
				.start();
		String error = IOUtils.toString(program.getErrorStream());
		if (program.waitFor() == 0) {
			diff(reference, out);
		} else {
			fail("Program failed with output:\n" + error);
		}
	}

	private void diff(List<Path> expected, List<Path> actual) throws IOException {
		assert expected.size() == actual.size();
		Iterator<Path> expIter = expected.iterator();
		Iterator<Path> actIter = actual.iterator();
		while (expIter.hasNext() && actIter.hasNext()) {
			Path exp = expIter.next();
			Path act = actIter.next();
			diff(exp, act);
		}
		assert !expIter.hasNext();
		assert !actIter.hasNext();
	}
	private void diff(Path expected, Path actual) throws IOException {
		assertEquals(String.format("Wrong size of output, comparing \"%s\" with \"%s\".", expected, actual), Files.size(expected), Files.size(actual));
		assertArrayEquals(String.format("Wrong content of output, comparing \"%s\" with \"%s\".", expected, actual), Files.readAllBytes(expected), Files.readAllBytes(actual));
	}
}
