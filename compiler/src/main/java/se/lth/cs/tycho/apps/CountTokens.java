package se.lth.cs.tycho.apps;

import se.lth.cs.tycho.parsing.cal.CalParser;
import se.lth.cs.tycho.parsing.cal.CalParserConstants;
import se.lth.cs.tycho.parsing.cal.Token;
import se.lth.cs.tycho.parsing.orcc.OrccParser;
import se.lth.cs.tycho.parsing.orcc.OrccParserConstants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CountTokens {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.printf("Usage: java %s <cal|orcc> <file>\n", CountTokens.class.getCanonicalName());
			System.exit(1);
		}
		String variant = args[0];
		String path = args[1];

		InputStream inputStream = Files.newInputStream(Paths.get(path));
		switch (variant) {
			case "cal": {
				CalParser parser = new CalParser(inputStream);
				int i = 0;
				while (true) {
					Token t = parser.getNextToken();
					if (t.kind == CalParserConstants.EOF) {
						break;
					}
					i = i + 1;
				}
				System.out.println(i);
				break;
			}
			case "orcc": {
				OrccParser parser = new OrccParser(inputStream);
				int i = 0;
				while (true) {
					se.lth.cs.tycho.parsing.orcc.Token t = parser.getNextToken();
					if (t.kind == OrccParserConstants.EOF) {
						break;
					}
					i = i + 1;
				}
				System.out.println(i);
				break;
			}
			default: {
				System.exit(1);
			}
		}
	}
}
