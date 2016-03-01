package se.lth.cs.tycho.apps;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.parsing.orcc.OrccParser;
import se.lth.cs.tycho.parsing.orcc.ParseException;
import se.lth.cs.tycho.parsing.orcc.TokenMgrError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

public class CountActions {
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.printf("Usage: java %s <path...>\n", CountTokens.class.getCanonicalName());
			System.exit(1);
		}
		Arrays.stream(args)
				.map(Paths::get)
				.flatMap(CountActions::findCalFiles)
				.forEach(cal -> {
					int a = countActions(cal);
					System.out.printf("%s: %d\n", cal, a);
				});

	}

	private static Stream<Path> findCalFiles(Path path) {
		try {
			return Files.walk(path).filter(p -> p.toString().endsWith(".cal"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static int countActions(Path cal) {
		try {
			OrccParser parser = new OrccParser(Files.newInputStream(cal));
			NamespaceDecl ns = parser.CompilationUnit();
			if (ns.getEntityDecls().isEmpty()) {
				return 0;
			} else if (ns.getEntityDecls().size() == 1) {
				return countActions(ns.getEntityDecls().get(0).getEntity());
			} else {
				throw new RuntimeException();
			}
		} catch (IOException | ParseException | TokenMgrError e) {
			throw new RuntimeException(cal.toString() + ": " + e.getMessage());
		}
	}

	private static int countActions(Entity entity) {
		if (entity instanceof CalActor) {
			CalActor actor = (CalActor) entity;
			return actor.getInitializers().size() + actor.getActions().size();
		} else {
			throw new RuntimeException();
		}
	}
}
