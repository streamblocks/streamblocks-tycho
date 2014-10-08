package net.opendf.backend.c;

import java.io.PrintWriter;

import javarag.AttributeEvaluator;
import javarag.TreeTraverser;
import javarag.impl.reg.BasicAttributeRegister;
import net.opendf.analyze.names.VariableBinding;
import net.opendf.backend.c.att.ActorMachines;
import net.opendf.backend.c.att.Assignments;
import net.opendf.backend.c.att.BorderActors;
import net.opendf.backend.c.att.Buffers;
import net.opendf.backend.c.att.CTypes;
import net.opendf.backend.c.att.ConstantPropagation;
import net.opendf.backend.c.att.ControllerGraph;
import net.opendf.backend.c.att.Controllers;
import net.opendf.backend.c.att.ControllersWithStats;
import net.opendf.backend.c.att.FunctionApplications;
import net.opendf.backend.c.att.Functions;
import net.opendf.backend.c.att.Lists;
import net.opendf.backend.c.att.Names;
import net.opendf.backend.c.att.Networks;
import net.opendf.backend.c.att.Ports;
import net.opendf.backend.c.att.ScopeDependencies;
import net.opendf.backend.c.att.ScopeInitializers;
import net.opendf.backend.c.att.Scopes;
import net.opendf.backend.c.att.SimpleExpressions;
import net.opendf.backend.c.att.Statements;
import net.opendf.backend.c.att.Transitions;
import net.opendf.backend.c.att.TranslationUnit;
import net.opendf.backend.c.att.Utilities;
import net.opendf.ir.net.Network;

public class Backend {

	private static TreeTraverser<Object> traverser = new IRNodeTraverser();

	public static void generateCode(Network network, PrintWriter out) {
		BasicAttributeRegister register = new BasicAttributeRegister();
		register.register(ActorMachines.class);
		register.register(Assignments.class);
		register.register(BorderActors.class);
		register.register(Buffers.class);
		register.register(ConstantPropagation.class);
		register.register(Controllers.class);
		// register.register(ControllersWithStats.class);
		register.register(ControllerGraph.class);
		register.register(CTypes.class);
		register.register(FunctionApplications.class);
		register.register(Functions.class);
		register.register(Lists.class);
		register.register(Names.class);
		register.register(Networks.class);
		register.register(Ports.class);
		register.register(ScopeDependencies.class);
		register.register(ScopeInitializers.class);
		register.register(Scopes.class);
		register.register(SimpleExpressions.class);
		register.register(Statements.class);
		register.register(Transitions.class);
		register.register(TranslationUnit.class);
		register.register(Utilities.class);
		register.register(VariableBinding.class);
		AttributeEvaluator evaluator = register.getEvaluator(network, traverser);
		evaluator.evaluate("translate", network, out);
		out.flush();
	}
}
