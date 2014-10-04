package net.opendf.util;
/**
 *  copyright (c) 20011, Per Andersson
 *  all rights reserved
 **/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;

import beaver.Scanner;
import net.opendf.analyze.memory.VariableInitOrderTransformer;
import net.opendf.errorhandling.ErrorModule;
import net.opendf.interp.BasicActorMachineSimulator;
import net.opendf.interp.BasicNetworkSimulator;
import net.opendf.interp.exception.CALCompiletimeException;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.common.decl.Decl;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.ast.NetworkDefinition;
import net.opendf.ir.util.DeclLoader;
import net.opendf.parser.lth.CalParser;
import net.opendf.parser.lth.CalScanner;

public class Parse{
	static final String usage = "Correct use: java net.opendf.parser.lth.Parse [options] path file" +
			"\nParse a CAL or NL file and print the internal representation. Default is xml format." +
			"\noptions:" +
			"\n-tokens print the sequence of tokens generated by the scanner." +
			"\n-pp pretty print" +
			"\n-xml print an xml representation of the IR" +
			"\n-graph graphviz representation of the evaluated network. Implies -net" +
			"\n-am transform actor to actor machine, done before xml printing" +
			"\n-net evaluate the network and create the Network object before xml printing. Actors and subgraphs are inlined."
			;

	private static void dumpScanner(String path, String fileName){
		try {
			FileReader fr = new FileReader(path + "/" + fileName + ".cal");
			Scanner scanner = new CalScanner(new BufferedReader(fr));
			System.out.println("dumping scanner token stream:");
			beaver.Symbol symbol;
			symbol = scanner.nextToken();
			while(symbol.getId() != CalParser.Terminals.EOF){
				try{
					System.out.println(CalParser.Terminals.NAMES[symbol.getId()] + " " + symbol.value);
				} catch(java.lang.ArrayIndexOutOfBoundsException e) {
					System.out.println(symbol.getId() + " " + symbol.value);
				}
				symbol = scanner.nextToken();
			}
			System.out.println("-----------------------");
		} catch (Exception e) {
			System.err.println(new java.util.Date());
			System.err.println("Exception: " + e);
			e.printStackTrace(System.err);
		}
	}

	public static void main(String[] args){
		boolean tokens = false;
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
			} else if(args[index].equals("-tokens")){
				tokens = true;
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

		if(tokens){
			dumpScanner(path, name);
		}

		try{
			DeclLoader declLoader= new DeclLoader(path);
			Decl decl = declLoader.getDecl(name);

			//		System.out.println("------- " + System.getProperty("user.dir") + "/" + path + "/" + name + " (" +  new java.util.Date() + ") -------");
			switch(decl.getKind()){
			case type:
				System.err.println("Type declaration is not supported");
			case value:
				System.err.println("Value declaration is not supported");
			case entity:
				if(decl instanceof Actor){
					Actor actor = (Actor)decl;
//					actor = VariableInitOrderTransformer.transformActor(actor, declLoader);

					if(prettyPrint){
						PrettyPrint pp = new PrettyPrint();
						pp.print(actor);
					}
					if(am){
						ActorMachine actorMachine = BasicActorMachineSimulator.prepareActor(actor, declLoader);

						if(xml){
							XMLWriter doc = new XMLWriter(actorMachine, declLoader);
							doc.print();
						}
					} else if(xml){
						XMLWriter doc = new XMLWriter(actor, declLoader);
						doc.print();
					}
				} else if(decl instanceof NetworkDefinition){
					NetworkDefinition network = (NetworkDefinition)decl;
					if(netEval){
						Network net = BasicNetworkSimulator.prepareNetworkDefinition(network, declLoader);
						if(xml){
							XMLWriter doc = new XMLWriter(net, declLoader);
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
						pp.print(network);
					}
				} else {
					throw new UnsupportedOperationException("DeclLoader returned an unexpected type during network evaluation." + name + "is instance of class" + decl.getClass().getCanonicalName());
				}
			}
		} catch(CALCompiletimeException e){
			ErrorModule em = e.getErrorModule();
			if(em != null){
				em.printErrors();
			} else {
				System.err.println("ERROR: " + e.getMessage());
			}
		}
	}
}