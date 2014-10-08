package se.lth.cs.tycho.classifier;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javarag.AttributeEvaluator;
import javarag.AttributeRegister;
import javarag.TreeTraverser;
import javarag.impl.reg.BasicAttributeRegister;
import se.lth.cs.tycho.classifier.attributes.CycloStaticActorModule;
import se.lth.cs.tycho.classifier.attributes.DecisionPathModule;
import se.lth.cs.tycho.classifier.attributes.DeterminacyModule;
import se.lth.cs.tycho.classifier.attributes.GraphModule;
import se.lth.cs.tycho.classifier.attributes.KahnProcessModule;
import se.lth.cs.tycho.classifier.attributes.MonotonicityModule;
import se.lth.cs.tycho.classifier.attributes.PortDeclModule;
import se.lth.cs.tycho.classifier.attributes.SynchronousActorModule;
import se.lth.cs.tycho.classifier.util.ControllerTraverser;
import net.opendf.ir.IRNode;
import net.opendf.ir.entity.am.ActorMachine;

public class Classifier {
	private static Map<String, String> classes = new LinkedHashMap<>();
	static {
		classes.put("deterministic", "isDeterministic");
		classes.put("monotonic", "isMonotonic");
		classes.put("kahn", "isKahnProcess");
		classes.put("cyclo-static", "isCycloStatic");
		classes.put("synchronous", "isSynchronous");
	}
	private static AttributeRegister register = new BasicAttributeRegister();
	private static TreeTraverser<IRNode> traverser = new ControllerTraverser();
	static {
		register.register(PortDeclModule.class);
		register.register(GraphModule.class);
		register.register(DecisionPathModule.class);
		register.register(DeterminacyModule.class);
		register.register(MonotonicityModule.class);
		register.register(KahnProcessModule.class);
		register.register(CycloStaticActorModule.class);
		register.register(SynchronousActorModule.class);
	}

	private AttributeEvaluator evaluator;
	private ActorMachine actorMachine;
	
	public Classifier(AttributeEvaluator evaluator, ActorMachine actorMachine) {
		this.evaluator = evaluator;
		this.actorMachine = actorMachine;
	}

	public static Classifier getInstance(ActorMachine actorMachine) {
		return new Classifier(register.getEvaluator(actorMachine, traverser), actorMachine);
	}
	
	public boolean isOfClass(String c) {
		if (!classes.containsKey(c)) {
			throw new IllegalArgumentException("No such class: \"" + c + "\"");
		} else {
			return evaluator.evaluate(classes.get(c), actorMachine);
		}
	}
	
	public boolean canClassify(String c) {
		return classes.containsKey(c);
	}
	
	public Collection<String> getClasses() {
		return Collections.unmodifiableSet(classes.keySet());
	}

}
