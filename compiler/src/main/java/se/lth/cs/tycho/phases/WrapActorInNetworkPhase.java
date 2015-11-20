package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.nl.EntityExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.entity.nl.PortReference;
import se.lth.cs.tycho.ir.entity.nl.StructureConnectionStmt;
import se.lth.cs.tycho.ir.entity.nl.StructureStatement;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Map;
import java.util.stream.Stream;

public class WrapActorInNetworkPhase implements Phase {
	@Override
	public String getDescription() {
		return "Wraps the target in a network if the target is an actor.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		assert task.getIdentifier().getNameCount() == 1;
		String id = task.getIdentifier().toString();
		EntityDecl entityDecl = task.getTarget().getEntityDecls().stream()
				.filter(entity -> entity.getName().equals(id))
				.findFirst()
				.get();
		if (entityDecl.getEntity() instanceof NlNetwork) {
			return task;
		} else {
			long n = context.getUniqueNumbers().next();
			String netName = id + "Net_" + n;
			EntityDecl network = EntityDecl.global(Availability.PUBLIC, netName, wrapInNetwork(entityDecl.getName(), entityDecl.getEntity()));
			ImmutableList<EntityDecl> entities = ImmutableList.<EntityDecl> builder()
					.addAll(task.getTarget().getEntityDecls())
					.add(network)
					.build();
			return task.withTarget(task.getTarget().withEntityDecls(entities)).withIdentifier(QID.of(netName));
		}
	}

	private NlNetwork wrapInNetwork(String entityName, Entity entity) {
		ImmutableList<PortDecl> inputPorts = entity.getInputPorts().map(PortDecl::deepClone);
		ImmutableList<PortDecl> outputPorts = entity.getOutputPorts().map(PortDecl::deepClone);
		ImmutableList<VarDecl> valueParameters = entity.getValueParameters().map(VarDecl::deepClone);
		ImmutableList<TypeDecl> typeParameters = entity.getTypeParameters().map(TypeDecl::deepClone);

		ImmutableList<Map.Entry<String, Expression>> actualValueParameters = valueParameters.stream()
				.map(VarDecl::getName)
				.map(v -> ImmutableEntry.<String, Expression> of(v, new ExprVariable(Variable.variable(v))))
				.collect(ImmutableList.collector());

		ImmutableList<Map.Entry<String, TypeExpr>> actualTypeParameters = valueParameters.stream()
				.map(VarDecl::getName)
				.map(v -> ImmutableEntry.of(v, new TypeExpr(v)))
				.collect(ImmutableList.collector());

		String instanceName = "instance";

		ImmutableList<Map.Entry<String, EntityExpr>> entities =
				ImmutableList.of(ImmutableEntry.of(instanceName, new EntityInstanceExpr(entityName, actualValueParameters, null)));


		Stream<StructureStatement> inputConnections = inputPorts.stream().map(PortDecl::getName)
				.map(port -> new StructureConnectionStmt(new PortReference(null, null, port), new PortReference(instanceName, null, port), null));
		Stream<StructureStatement> outputConnections = outputPorts.stream().map(PortDecl::getName)
				.map(port -> new StructureConnectionStmt(new PortReference(instanceName, null, port), new PortReference(null, null, port), null));

		ImmutableList<StructureStatement> structure =
				Stream.concat(inputConnections, outputConnections).collect(ImmutableList.collector());

		return new NlNetwork(typeParameters, valueParameters, null, null, inputPorts, outputPorts, entities, structure, null);
	}
}
