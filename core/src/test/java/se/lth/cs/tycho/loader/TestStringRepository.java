package se.lth.cs.tycho.loader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static se.lth.cs.tycho.ir.decl.DeclKind.ENTITY;
import static se.lth.cs.tycho.ir.decl.DeclKind.TYPE;
import static se.lth.cs.tycho.ir.decl.DeclKind.VAR;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.GlobalDecl;
import se.lth.cs.tycho.loader.SourceCodeRepository;
import se.lth.cs.tycho.loader.SourceCodeUnit;

public class TestStringRepository {

	@Test
	public void testFindFormEmpty() {
		SourceCodeRepository repo = new StringRepository();
		assertTrue(repo.findUnits(QID.empty(), ENTITY).isEmpty());
		assertTrue(repo.findUnits(QID.parse("abc.def.ghi"), TYPE).isEmpty());
		assertTrue(repo.findUnits(QID.parse("abc"), VAR).isEmpty());
	}

	@Test
	public void testFindExisting() {
		StringRepository repo = new StringRepository();
		repo.add("namespace a.b: public int x = 7; end");
		QID a_b_x = QID.parse("a.b.x");
		List<SourceCodeUnit> units = repo.findUnits(a_b_x, VAR);
		assertEquals(1, units.size());
		NamespaceDecl ns = units.get(0).load(null);
		List<GlobalDecl> decls = findDecls(ns, a_b_x).collect(Collectors.toList());
		assertEquals(1, decls.size());
		assertEquals("x", decls.get(0).getName());
	}

	@Test
	public void testFindNotExisting() {
		StringRepository repo = new StringRepository();
		repo.add("namespace a.b: public int x = 7; end");
		assertTrue(repo.findUnits(QID.parse("a.b.x"), ENTITY).isEmpty());
		assertTrue(repo.findUnits(QID.parse("a.b.x"), TYPE).isEmpty());
		assertTrue(repo.findUnits(QID.parse("a.x"), VAR).isEmpty());
		assertTrue(repo.findUnits(QID.parse("x"), VAR).isEmpty());
	}

	public Stream<GlobalDecl> findDecls(NamespaceDecl ns, QID qid) {
		QID relative = qid.getWithoutPrefix(ns.getQID());
		if (relative == null) {
			return Stream.empty();
		} else {
			if (relative.getNameCount() == 1) {
				String name = relative.toString();
				return ns.getDecls().stream()
						.filter(d -> d.getName().equals(name));
			} else {
				return ns.getNamespaceDecls().stream()
						.flatMap(child -> findDecls(child, relative));
			}
		}
	}

}
