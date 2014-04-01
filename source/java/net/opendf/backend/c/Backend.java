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
		register.register(ActorMachines.class, Assignments.class, BorderActors.class, Buffers.class, ConstantPropagation.class,
				Controllers.class, ControllerGraph.class, CTypes.class, FunctionApplications.class, Functions.class,
				Lists.class, Names.class, Networks.class, Ports.class, ScopeDependencies.class, ScopeInitializers.class, Scopes.class,
				SimpleExpressions.class, Statements.class, Transitions.class, TranslationUnit.class, Utilities.class);
		register.register(VariableBinding.class);
		AttributeEvaluator evaluator = register.getEvaluator(network, traverser);
		evaluator.evaluate("translate", network, out);
		out.flush();
	}
}
