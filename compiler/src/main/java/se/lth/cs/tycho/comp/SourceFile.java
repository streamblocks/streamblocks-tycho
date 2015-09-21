package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SourceFile implements SourceUnit {
	private final Path file;
	private final NamespaceDecl tree;
	private final InputLanguage language;

	public SourceFile(Path file, NamespaceDecl tree, InputLanguage language) {
		this.file = file;
		this.tree = tree;
		this.language = language;
	}

	@Override
	public InputLanguage getLanguage() {
		return language;
	}

	@Override
	public NamespaceDecl getTree() {
		return tree;
	}

	@Override
	public SourceUnit withTree(NamespaceDecl tree) {
		return new SourceFile(file, tree, language);
	}

	@Override
	public String getLocation() {
		return file.toString();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return Files.newInputStream(file);
	}

	@Override
	public IRNode clone() {
		try {
			return (IRNode) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error(e);
		}
	}
}
