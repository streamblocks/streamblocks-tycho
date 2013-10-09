package net.opendf.ir.util;

import java.io.File;

import net.opendf.interp.exception.CALCompiletimeException;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.Namespace;
import net.opendf.ir.common.NamespaceDecl;
import net.opendf.ir.net.ast.NetworkDefinition;
import net.opendf.parser.lth.CalParser;
import net.opendf.parser.lth.NlParser;

import net.opendf.ir.net.ast.evaluate.NetDefEvaluator;


/**
 * This class manages the global namespace.
 * Given a name it will return a {@link Decl}. The returned {@link Decl} is either an instance of {@link Actor} or {@link NetworkDefinition}.
 * In the simulator the {@link Decl}s is instantiated by {@link NetDefEvaluator}. After this the nodes are represented by {@link net.opendf.ir.am.ActorMachine} and {@link net.opendf.ir.net.Network}
 * 
 * @author pera
 *
 */
public class DeclLoader {
	private Namespace ns;
	private String filePath;

	public DeclLoader(String filePath){
		this.filePath = filePath;
		ns = Namespace.createTopLevelNamespace();
		ns.createNamespaceDecl();
	}

	/**
	 * Fetch the {@link Actor} or {@link NetworkDefinition} associated with the name in the global namespace.
	 * If the {@link Decl} is not cached then it is loaded from the file system.
	 * 
	 * @param name
	 * @return The {$link Decl} associated with name in the global namespace.
	 */
	public Decl getDecl(String name){
		for(NamespaceDecl nsDecl : ns.getDecls()){
			for(Decl decl : nsDecl.getDecls()){
				if(decl.getName().equals(name)){
					return decl;
				}
			}
		}
		return loadDecl(name);
	}

	private Decl loadDecl(String name) {
		Decl result = null;
		File file = new File(filePath + File.separatorChar + name + ".cal");
		if(file.exists()){
			CalParser parser = new CalParser();
			Actor actor = parser.parse(file);
			parser.printParseProblems();
			if(!parser.parseProblems.isEmpty()){
				//TODO handle errors
			}
			//TODO semantic checks and build the actor machine
			//ActorMachine am = BasicActorMachineSimulator.prepareActor(actor);
			result = actor;
		} else {
			file = new File(filePath + File.separatorChar + name + ".nl");
			if(file.exists()){
				NlParser parser = new NlParser();
				NetworkDefinition net = parser.parse(file);
				parser.printParseProblems();
				if(!parser.parseProblems.isEmpty()){
					//TODO handle errors
				}
				//TODO semantic checks
				result = net;
			} else {
				throw new CALCompiletimeException("Can not find definition for entity " + name);
				//TODO name not found
			}
		}
		if(result != null){
			//TODO place the decl in the right name space
			ns.getDecl(0).addDecl(result);
		}
		return result;
	}
}
