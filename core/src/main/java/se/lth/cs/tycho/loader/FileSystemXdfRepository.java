package se.lth.cs.tycho.loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.DeclKind;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.entity.xdf.XDFNetwork;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.messages.Message;
import se.lth.cs.tycho.messages.MessageReporter;
import se.lth.cs.tycho.parsing.xdf.XDFReader;

public class FileSystemXdfRepository implements SourceCodeRepository {

	private final Path path;
	private final XDFReader reader;

	public FileSystemXdfRepository(Path path) {
		this.path = path;
		this.reader = new XDFReader();
	}

	@Override
	public List<SourceCodeUnit> findUnits(QID name, DeclKind kind) {
		if (kind == DeclKind.ENTITY) {
			Path file = getPath(name);
			if (Files.isRegularFile(file)) {
				return Collections.singletonList(new XdfCompilationUnit(name, file));
			}
		}
		return Collections.emptyList();
	}

	private Path getPath(QID qid) {
		String rel = qid.parts().stream().map(QID::toString).collect(Collectors.joining(File.separator, "", ".xdf"));
		return path.resolve(rel);
	}

	@Override
	public boolean checkRepository(MessageReporter messages) {
		if (!Files.isDirectory(path)) {
			messages.report(Message.error(path + " is not a directory."));
			return false;
		}
		return true;
	}

	private class XdfCompilationUnit implements SourceCodeUnit {
		private final QID qid;
		private final Path path;

		public XdfCompilationUnit(QID qid, Path path) {
			this.qid = qid;
			this.path = path;
		}

		@Override
		public NamespaceDecl load(MessageReporter messages) {
			try {
				XDFNetwork xDFNetwork = reader.read(Files.newInputStream(path));
				EntityDecl decl = EntityDecl.global(Availability.PUBLIC, qid.getLast().toString(), xDFNetwork);
				return new NamespaceDecl(qid.getButLast(), ImmutableList.empty(), ImmutableList.empty(), ImmutableList.empty(),
						ImmutableList.of(decl), ImmutableList.empty());
			} catch (ParserConfigurationException | SAXException | IOException e) {
				messages.report(Message.error(e.getMessage()));
				return null;
			}
		}

		@Override
		public String getLocationDescription() {
			return path.toAbsolutePath().toString();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof XdfCompilationUnit) {
				XdfCompilationUnit that = (XdfCompilationUnit) obj;
				return Objects.equals(this.qid, that.qid) && Objects.equals(this.path, that.path);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(qid, path);
		}

	}

}
