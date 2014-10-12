package se.lth.cs.tycho.ir.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import se.lth.cs.tycho.errorhandling.ErrorModule;
import se.lth.cs.tycho.interp.exception.CALCompiletimeException;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.IRNode.Identifier;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.cal.Actor;
import se.lth.cs.tycho.ir.entity.nl.NetworkDefinition;
import se.lth.cs.tycho.ir.entity.nl.evaluate.NetDefEvaluator;
import se.lth.cs.tycho.parser.SourceCodeOracle;
import se.lth.cs.tycho.parser.lth.CalParser;
import se.lth.cs.tycho.parser.lth.NlParser;


/**
 * This class manages the global namespace.
 * Given a name it will return a {@link Decl}. The returned {@link Decl} is either an instance of {@link Actor} or {@link NetworkDefinition}.
 * In the simulator the {@link Decl}s is instantiated by {@link NetDefEvaluator}. After this the nodes are represented by {@link se.lth.cs.tycho.instance.am.ActorMachine} and {@link se.lth.cs.tycho.instance.net.Network}
 * 
 * @author pera
 *
 */
public class DeclLoader implements SourceCodeOracle{
	private String filePath;
	
	private Map<String, Decl> namespace = new HashMap<>();
	
	private HashMap<Identifier, SourceCodePosition> srcPositions = new HashMap<Identifier, SourceCodePosition>();

	public DeclLoader(String filePath){
		this.filePath = filePath;
	}

	/**
	 * Fetch the {@link Actor} or {@link NetworkDefinition} associated with the name in the global namespace.
	 * If the {@link Decl} is not cached then it is loaded from the file system.
	 * 
	 * @param name
	 * @return The {$link Decl} associated with name in the global namespace.
	 * @throws CALCompiletimeException is a compilation error occurs
	 */
	public Decl getDecl(String name) throws CALCompiletimeException {
		if (namespace.containsKey(name)) {
			return namespace.get(name);
		} else {
			return loadDecl(name);
		}
	}

	/**
	 * Load a declaration from file.
	 * @param name
	 * @return
	 * @throws CALCompiletimeException is a compilation error occurs
	 */
	private Decl loadDecl(String name) throws CALCompiletimeException {
		Decl result = null;
		File file = new File(filePath + File.separatorChar + name + ".cal");
		ErrorModule em;
		if(file.exists()){
			CalParser parser = new CalParser();
			GlobalEntityDecl actor = parser.parse(file, srcPositions, this);
			em = parser.getErrorModule();
			em.printWarnings();
			em.abortIfError();
			//TODO semantic checks
			result = actor;
		} else {
			file = new File(filePath + File.separatorChar + name + ".nl");
			if(file.exists()){
				NlParser parser = new NlParser();
				GlobalEntityDecl net = parser.parse(file, srcPositions, this);
				em = parser.getErrorModule();
				em.printWarnings();
				em.abortIfError();
				//TODO semantic checks
				result = net;
			} else {
				throw new CALCompiletimeException("Can not find definition for entity " + name, null);
				//TODO name not found
			}
		}
		namespace.put(name,  result);
		return result;
	}
	
	public SourceCodePosition getSrcLocations(Identifier id){
		return srcPositions.get(id);
	}

	@Override
	public void register(IRNode node, SourceCodePosition pos) {
		srcPositions.put(node.getIdentifier(), pos);
	}
}
