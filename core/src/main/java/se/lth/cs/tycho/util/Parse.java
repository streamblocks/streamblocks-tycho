package se.lth.cs.tycho.util;
/**
 *  copyright (c) 20011, Per Andersson
 *  all rights reserved
 **/

import java.io.PrintWriter;
import java.nio.file.Paths;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.interp.BasicActorMachineSimulator;
import se.lth.cs.tycho.interp.BasicNetworkSimulator;
import se.lth.cs.tycho.interp.exception.CALCompiletimeException;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.loader.AmbiguityException;
import se.lth.cs.tycho.loader.DeclarationLoader;
import se.lth.cs.tycho.loader.FileSystemCalRepository;
import se.lth.cs.tycho.loader.FileSystemXdfRepository;
import se.lth.cs.tycho.messages.MessageWriter;

public class Parse{
	static final String usage = "Correct use: java se.lth.cs.tycho.parser.lth.Parse [options] path file" +
			"\nParse a CAL or NL file and print the internal representation. Default is xml format." +
			"\noptions:" +
			"\n-pp pretty print" +
			"\n-xml print an xml representation of the IR" +
			"\n-graph graphviz representation of the evaluated network. Implies -net" +
			"\n-am transform calActor to calActor machine, done before xml printing" +
			"\n-net evaluate the network and create the Network object before xml printing. Actors and subgraphs are inlined."
			;

	public static void main(String[] args){
		boolean prettyPrint = false;
		boolean xml = true;
		boolean am = false;
		boolean netEval = false;
		boolean graph = false;

		int index = 0;

		while(index<args.length && args[index].startsWith("-")){
			if(args[index].equals("-xml")){
				xml = true;
			} else if(args[index].equals("-pp")){
				prettyPrint = true;
				xml = false;
			} else if(args[index].equals("-graph")){
				xml = false;
				graph = true;
				netEval = true;
			} else if(args[index].equals("-am")){
				am = true;
			} else if(args[index].equals("-net")){
				netEval = true;
				xml = true;
			} else {
				System.err.println(usage);
				return;
			}
			index++;
		}
		if(index+2 != args.length){
			System.err.println(usage);				
			return;
		}

		String path = args[index++];
		String name = args[index++];

		try{
			DeclarationLoader declLoader= new DeclarationLoader(new MessageWriter());
			declLoader.addRepository(new FileSystemCalRepository(Paths.get(path)));
			declLoader.addRepository(new FileSystemXdfRepository(Paths.get(path)));
			EntityDecl decl = declLoader.loadEntity(QID.of(name), null);
			if (decl instanceof EntityDecl) {
				Entity entity = decl.getEntity();
				//		System.out.println("------- " + System.getProperty("user.dir") + "/" + path + "/" + name + " (" +  new java.util.Date() + ") -------");
				if(entity instanceof CalActor){
					CalActor calActor = (CalActor)entity;
	//					calActor = VariableInitOrderTransformer.transformActor(calActor, declLoader);
	
					if(prettyPrint){
						PrettyPrint pp = new PrettyPrint();
						pp.print(calActor, decl.getName());
					}
					if(am){
						ActorMachine actorMachine = BasicActorMachineSimulator.prepareActor(calActor);
	
						if(xml){
							XMLWriter doc = new XMLWriter(actorMachine);
							doc.print();
						}
					} else if(xml){
						XMLWriter doc = new XMLWriter(calActor);
						doc.print();
					}
				} else if(entity instanceof NlNetwork){
					NlNetwork network = (NlNetwork)entity;
					if(netEval){
						Network net = BasicNetworkSimulator.prepareNetworkDefinition(network, declLoader);
						if(xml){
							XMLWriter doc = new XMLWriter(net);
							doc.print();
						}
						if(graph){
							NetworkToGraphviz.print(net, name, new PrintWriter(System.out));
						}
					} else if(xml){
						XMLWriter doc = new XMLWriter(network);
						doc.print();
					}
					if(prettyPrint){
						PrettyPrint pp = new PrettyPrint();
						pp.print(network, decl.getName());
					}
				}
			} else {
				throw new UnsupportedOperationException("DeclLoader returned an unexpected type during network evaluation." + name + "is instance of class" + decl.getClass().getCanonicalName());
			}
		} catch(CALCompiletimeException | AmbiguityException e){
			System.err.println("ERROR: " + e.getMessage());
		}
	}
}