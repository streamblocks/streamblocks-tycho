package se.lth.cs.tycho;

import org.apache.commons.io.IOUtils;
import se.lth.cs.tycho.comp.Compiler;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.SettingsManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProgramTester {
	private final Path executable;

	private ProgramTester(Path executable) {
		this.executable = executable;
	}

	public static ProgramTester compile(Path source, QID name) throws IOException, Configuration.Builder.UnknownKeyException, InterruptedException {
		Path target = Files.createTempDirectory(name.toString());
		SettingsManager settings = Compiler.defaultSettingsManager();
		Configuration config = Configuration.builder(settings)
				.set(Compiler.sourcePaths, Collections.singletonList(source))
				.set(Compiler.targetPath, target)
				.build();
		Compiler comp = new Compiler(config);
		if (comp.compile(name)) {
			Optional<Path> cfile = Files.list(target)
					.filter(file -> file.getFileName().toString().startsWith(name.getLast().toString()))
					.filter(file -> file.toString().endsWith(".c"))
					.findFirst();
			if (cfile.isPresent()) {
				Process cc = new ProcessBuilder("cc", "-std=c99", cfile.get().getFileName().toString())
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
					throw new RuntimeException(error);
				}
			} else {
				throw new RuntimeException("Compilation error." + Files.list(target).map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", ", "[", "]")));
			}
		} else {
			throw new RuntimeException("Compilation error.");
		}
	}

	public void run(List<Path> input, List<Path> reference) throws IOException, InterruptedException {
		Path temp = Files.createTempDirectory("test");
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
		String error = IOUtils.toString(program.getInputStream());
		if (program.waitFor() == 0) {

		} else {
			throw new RuntimeException("Exited with " + program.exitValue());
		}
	}
}
