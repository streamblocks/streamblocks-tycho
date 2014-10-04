package net.opendf.ir.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.opendf.errorhandling.ErrorModule;
import net.opendf.interp.exception.CALCompiletimeException;
import net.opendf.ir.IRNode;
import net.opendf.ir.IRNode.Identifier;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.common.decl.Decl;
import net.opendf.ir.net.ast.NetworkDefinition;
import net.opendf.ir.net.ast.evaluate.NetDefEvaluator;
import net.opendf.parser.SourceCodeOracle;
import net.opendf.parser.lth.CalParser;
import net.opendf.parser.lth.NlParser;


/**
 * This class manages the global namespace.
 * Given a name it will return a {@link Decl}. The returned {@link Decl} is either an instance of {@link Actor} or {@link NetworkDefinition}.
 * In the simulator the {@link Decl}s is instantiated by {@link NetDefEvaluator}. After this the nodes are represented by {@link net.opendf.ir.am.ActorMachine} and {@link net.opendf.ir.net.Network}
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
			Actor actor = parser.parse(file, srcPositions, this);
			em = parser.getErrorModule();
			em.printWarnings();
			em.abortIfError();
			//TODO semantic checks
			result = actor;
		} else {
			file = new File(filePath + File.separatorChar + name + ".nl");
			if(file.exists()){
				NlParser parser = new NlParser();
				NetworkDefinition net = parser.parse(file, srcPositions, this);
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
