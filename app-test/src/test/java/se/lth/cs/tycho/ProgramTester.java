package se.lth.cs.tycho;

import org.apache.commons.io.IOUtils;
import se.lth.cs.tycho.compiler.Compiler;
import se.lth.cs.tycho.platform.Platform;
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

	public static Optional<ProgramTester> compile(TestDescription test, Path target) throws IOException, Configuration.Builder.UnknownKeyException, InterruptedException {
		Platform platform = Compiler.defaultPlatform();
		SettingsManager initialSettings = SettingsManager.initialSettingManager();
		SettingsManager settings = new SettingsManager.Builder()
				.addAll(initialSettings.getAllSettings())
				.addAll(platform.settingsManager()).build();
		Configuration config = Configuration.builder(settings)
				.set(Compiler.sourcePaths, test.getSourcePaths())
				.set(Compiler.orccSourcePaths, test.getOrccSourcePaths())
				.set(Compiler.xdfSourcePaths, test.getXDFSourcePaths())
				.set(Compiler.targetPath, target)
				.build();
		OutputCapturer capturer = new OutputCapturer();
		capturer.start();
		ProgramTester tester = null;
		Compiler comp = new Compiler(platform, config);
		QID name = test.getEntity();
		if (comp.compile(name)) {
			List<Path> cfiles = Files.list(target)
					.filter(file -> file.toString().endsWith(".c"))
					.collect(Collectors.toList());
			if (!cfiles.isEmpty()) {
				List<String> command = new ArrayList<>();
				command.add("cc");
				command.add("-std=c99");
				command.add(String.format("-I%s", target));
				cfiles.forEach(p -> command.add(p.toAbsolutePath().toString()));
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
					tester = new ProgramTester(aout);
				} else {
					throw new RuntimeException(String.format("Compilation error in %s:\n%s", cfiles, error));
				}
			} else {
				throw new RuntimeException("Compilation error." + Files.list(target).map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", ", "[", "]")));
			}
		}
		check(test.getCheckPaths(), capturer.stop());
		return Optional.ofNullable(tester);
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

	private static void check(List<Path> paths, String output) throws IOException {
		for (Path path : paths) {
			List<Path> files = Files.walk(path).filter(Files::isRegularFile).collect(Collectors.toList());
			for (Path file : files) {
				String check = new String(Files.readAllBytes(file)).replaceAll("\\s+", " ");
				assertTrue(String.format("Check %s failed.", file.toAbsolutePath()), output.contains(check));
			}
		}
	}
}
