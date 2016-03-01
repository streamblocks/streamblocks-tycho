package se.lth.cs.tycho.util;

import se.lth.cs.tycho.ir.entity.nl.*;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class NetworkUtils {
	private NetworkUtils() {}

	public static NlNetwork removeConnectedInstance(NlNetwork network, String name) {
		Predicate<StructureConnectionStmt> connectedToInstance =
				conn -> Objects.equals(conn.getSrc().getEntityName(), name) || Objects.equals(conn.getDst().getEntityName(), name);
		RemoveConnection remove = new RemoveConnection(connectedToInstance);
		NlNetwork result = network.withStructure(remove.removeFrom(network.getStructure()));
		return removeInstance(result, name);
	}
	public static NlNetwork removeInstance(NlNetwork network, String name) {
		ImmutableList.Builder<Map.Entry<String, EntityExpr>> builder = ImmutableList.builder();
		for (Map.Entry<String, EntityExpr> entry : network.getEntities()) {
			if (entry.getKey().equals(name)) {
				if (entry.getValue() instanceof EntityListExpr) {
					throw new IllegalArgumentException("Cannot remove a list of instances.");
				}
			} else {
				builder.add(entry);
			}
		}
		ImmutableList<Map.Entry<String, EntityExpr>> entities = builder.build();
		int removed = network.getEntities().size() - entities.size();
		if (removed == 0) {
			throw new NoSuchElementException("No such instance found.");
		} else if (removed > 1) {
			throw new IllegalStateException("Malformed network: More than one instance with the given name.");
		}
		network.getStructure().forEach(s -> s.accept(checkNotConnected, name));
		return network.withEntities(entities);
	}

	private static final CheckNotConnected checkNotConnected = new CheckNotConnected();
	private static final class CheckNotConnected implements StructureStmtVisitor<Void, String> {
		private void throwIsConnectedException() {
			throw new IllegalArgumentException("Instance is connected.");
		}

		@Override
		public Void visitStructureConnectionStmt(StructureConnectionStmt stmt, String name) {
			if (Objects.equals(stmt.getSrc().getEntityName(), name)) {
				throwIsConnectedException();
			}
			if (Objects.equals(stmt.getDst().getEntityName(), name)) {
				throwIsConnectedException();
			}
			return null;
		}

		@Override
		public Void visitStructureIfStmt(StructureIfStmt stmt, String name) {
			stmt.getTrueStmt().forEach(s -> s.accept(this, name));
			stmt.getFalseStmt().forEach(s -> s.accept(this, name));
			return null;
		}

		@Override
		public Void visitStructureForeachStmt(StructureForeachStmt stmt, String name) {
			stmt.getStatements().forEach(s -> s.accept(this, name));
			return null;
		}
	}

	public static NlNetwork addInstance(NlNetwork network, String name, EntityInstanceExpr instance) {
		if (network.getEntities().stream().map(Map.Entry::getKey).anyMatch(name::equals)) {
			throw new IllegalArgumentException("Name is already used.");
		}
		return network.withEntities(
				ImmutableList.<Map.Entry<String, EntityExpr>> builder()
						.addAll(network.getEntities())
						.add(ImmutableEntry.of(name, instance))
						.build());
	}

	public static NlNetwork removeConnection(NlNetwork network, StructureConnectionStmt connection) {
		if (!connection.getSrc().getEntityIndex().isEmpty()) {
			throw new IllegalArgumentException("Cannot remove connections from indexed entities.");
		}
		if (!connection.getDst().getEntityIndex().isEmpty()) {
			throw new IllegalArgumentException("Cannot remove connections to indexed entities.");
		}
		RemoveConnection visitor = new RemoveConnection(connection);
		ImmutableList<StructureStatement> structure = visitor.removeFrom(network.getStructure());
		if (visitor.removed == 0) {
			throw new IllegalArgumentException("No such connection found.");
		} else if (visitor.removed > 1) {
			throw new IllegalStateException("Malformed network. More than one connection found.");
		}
		return network.withStructure(structure);
	}


	private static final class RemoveConnection implements StructureStmtVisitor<Stream<StructureStatement>, Void> {
		private final Predicate<StructureConnectionStmt> remove;
		private int removed = 0;

		public RemoveConnection(StructureConnectionStmt remove) {
			this.remove = stmt -> samePortRef(stmt.getSrc(), remove.getSrc()) && samePortRef(stmt.getDst(), remove.getDst());
		}

		public RemoveConnection(Predicate<StructureConnectionStmt> remove) {
			this.remove = remove;
		}

		private ImmutableList<StructureStatement> removeFrom(ImmutableList<StructureStatement> list) {
			return list.stream().flatMap(s -> s.accept(this)).collect(ImmutableList.collector());
		}
		@Override
		public Stream<StructureStatement> visitStructureConnectionStmt(StructureConnectionStmt stmt, Void aVoid) {
			if (remove.test(stmt)) {
				removed++;
				return Stream.empty();
			} else {
				return Stream.of(stmt);
			}
		}

		@Override
		public Stream<StructureStatement> visitStructureIfStmt(StructureIfStmt stmt, Void aVoid) {
			return Stream.of(stmt.copy(stmt.getCondition(), removeFrom(stmt.getTrueStmt()), removeFrom(stmt.getFalseStmt())));
		}

		@Override
		public Stream<StructureStatement> visitStructureForeachStmt(StructureForeachStmt stmt, Void aVoid) {
			return Stream.of(stmt.copy(stmt.getGenerators(), removeFrom(stmt.getStatements())));
		}
	}

	private static boolean samePortRef(PortReference a, PortReference b) {
		return Objects.equals(a.getEntityName(), b.getEntityName())
				&& Objects.equals(a.getPortName(), b.getPortName())
				&& Objects.equals(a.getEntityIndex(), b.getEntityIndex());
	}

	public static NlNetwork addConnection(NlNetwork network, StructureConnectionStmt connection) {
		network.getStructure().forEach(s -> s.accept(assertNotConnected, connection.getDst()));
		return network.withStructure(
				ImmutableList.<StructureStatement> builder()
						.addAll(network.getStructure())
						.add(connection).build());
	}

	private static final AssertNotConnected assertNotConnected = new AssertNotConnected();
	private static final class AssertNotConnected implements StructureStmtVisitor<Void,PortReference> {

		@Override
		public Void visitStructureConnectionStmt(StructureConnectionStmt stmt, PortReference target) {
			if (samePortRef(stmt.getDst(), target)) {
				throw new IllegalArgumentException("Target port is already connected");
			}
			return null;
		}

		@Override
		public Void visitStructureIfStmt(StructureIfStmt stmt, PortReference target) {
			stmt.getTrueStmt().forEach(s -> s.accept(this, target));
			stmt.getFalseStmt().forEach(s -> s.accept(this, target));
			return null;
		}

		@Override
		public Void visitStructureForeachStmt(StructureForeachStmt stmt, PortReference target) {
			stmt.getStatements().forEach(s -> s.accept(this, target));
			return null;
		}
	}
}
