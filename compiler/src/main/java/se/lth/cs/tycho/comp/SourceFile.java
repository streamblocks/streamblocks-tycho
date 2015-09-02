package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.NamespaceDecl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SourceFile implements SourceUnit {
	private final Path file;
	private final NamespaceDecl tree;

	public SourceFile(Path file, NamespaceDecl tree) {
		this.file = file;
		this.tree = tree;
	}

	@Override
	public NamespaceDecl getTree() {
		return tree;
	}

	@Override
	public String getLocation() {
		return file.toString();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return Files.newInputStream(file);
	}

}
