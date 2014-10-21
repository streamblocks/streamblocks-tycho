package se.lth.cs.tycho.backend.c;

import java.io.PrintWriter;

import javarag.AttributeEvaluator;
import javarag.TreeTraverser;
import javarag.impl.reg.BasicAttributeRegister;
import se.lth.cs.tycho.analysis.name.Imports;
import se.lth.cs.tycho.analysis.name.NameAnalysis;
import se.lth.cs.tycho.analysis.name.NamespaceDecls;
import se.lth.cs.tycho.analysis.name.PortAnalysis;
import se.lth.cs.tycho.analysis.type.TypeAnalysis;
import se.lth.cs.tycho.analysis.util.IRNodeTraverserWithTreeRoot;
import se.lth.cs.tycho.analysis.util.TreeRoot;
import se.lth.cs.tycho.analysis.util.TreeRootModule;
import se.lth.cs.tycho.analysis.value.ConstantEvaluation;
import se.lth.cs.tycho.backend.c.att.ActorMachines;
import se.lth.cs.tycho.backend.c.att.Assignments;
import se.lth.cs.tycho.backend.c.att.BorderActors;
import se.lth.cs.tycho.backend.c.att.Buffers;
import se.lth.cs.tycho.backend.c.att.CTypes;
import se.lth.cs.tycho.backend.c.att.ConstantPropagation;
import se.lth.cs.tycho.backend.c.att.ControllerGraph;
import se.lth.cs.tycho.backend.c.att.Controllers;
import se.lth.cs.tycho.backend.c.att.FunctionApplications;
import se.lth.cs.tycho.backend.c.att.Functions;
import se.lth.cs.tycho.backend.c.att.Lists;
import se.lth.cs.tycho.backend.c.att.Names;
import se.lth.cs.tycho.backend.c.att.Networks;
import se.lth.cs.tycho.backend.c.att.ScopeDependencies;
import se.lth.cs.tycho.backend.c.att.ScopeInitializers;
import se.lth.cs.tycho.backend.c.att.Scopes;
import se.lth.cs.tycho.backend.c.att.SimpleExpressions;
import se.lth.cs.tycho.backend.c.att.Statements;
import se.lth.cs.tycho.backend.c.att.Transitions;
import se.lth.cs.tycho.backend.c.att.TranslationUnit;
import se.lth.cs.tycho.backend.c.att.Utilities;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.loader.DeclarationLoader;

public class Backend {

	private static TreeTraverser<Object> traverser = new IRNodeTraverserWithTreeRoot();

	public static void generateCode(DeclarationLoader loader, Network network, PrintWriter out) {
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
//		register.register(Ports.class);
		register.register(ScopeDependencies.class);
		register.register(ScopeInitializers.class);
		register.register(Scopes.class);
		register.register(SimpleExpressions.class);
		register.register(Statements.class);
		register.register(Transitions.class);
		register.register(TranslationUnit.class);
		register.register(Utilities.class);
		
//		register.register(VariableBinding.class);
		register.register(Imports.class);
		register.register(NameAnalysis.class);
		register.register(NamespaceDecls.class);
		register.register(TypeAnalysis.class);
		register.register(ConstantEvaluation.class);
		register.register(TreeRootModule.class);
		register.register(PortAnalysis.class);
		TreeRoot root = new TreeRoot(null, network);
		AttributeEvaluator evaluator = register.getEvaluator(root, traverser);
		evaluator.evaluate("translate", root, out);
		out.flush();
	}
}
