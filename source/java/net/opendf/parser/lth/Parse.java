package net.opendf.parser.lth;
/**
 *  copyright (c) 20011, Per Andersson
 *  all rights reserved
 **/

import java.io.BufferedReader;
import java.io.FileReader;

import beaver.Scanner;
import net.opendf.analyze.memory.VariableInitOrderTransformer;
import net.opendf.interp.BasicActorMachineSimulator;
import net.opendf.interp.BasicNetworkSimulator;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.net.ast.NetworkDefinition;
import net.opendf.parser.lth.CalParser;
import net.opendf.util.PrettyPrint;
import net.opendf.util.XMLWriter;

public class Parse{
	static final String usage = "Correct use: java net.opendf.parser.lth.Parse [options] path file" +
			"\nParse a CAL file and do a pretty print of the internal representation." +
			"\noptions:" +
			"\n-tokens print the sequence of tokens generated by the scanner." +
			"\n-pp pretty print" +
			"\n-am transform actor to actor machine, done before xml printing" +
			"\n-xml print an xml representation of th IR" +
			"\nThe file name should include the file extension, i.e. 'Add.cal'";

	private static void dumpScanner(String path, String fileName){
		try {
			FileReader fr = new FileReader(path + "/" + fileName);
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
		int index = 0;
		boolean tokens = false;
		boolean prettyPrint = false;
		boolean xml = false;
		boolean am = false;
		while(index<args.length && args[index].startsWith("-")){
			if(args[index].equals("-xml")){
				xml = true;
			} else if(args[index].equals("-pp")){
				prettyPrint = true;
			} else if(args[index].equals("-am")){
				am = true;
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
		String fileName = args[index++];

		System.out.println("------- " + System.getProperty("user.dir") + "/" + path + "/" + fileName + " (" +  new java.util.Date() + ") -------");
		if(tokens){
			dumpScanner(path, fileName);
		}
		if(fileName.endsWith(".cal")){
			CalParser parser = new CalParser();
			Actor actor = parser.parse(path, fileName);
			parser.printParseProblems();
			if(parser.parseProblems.isEmpty()){
				actor = VariableInitOrderTransformer.transformActor(actor);
				if(prettyPrint){
					PrettyPrint pp = new PrettyPrint();
					pp.print(actor);
				}

				if(am){
					ActorMachine actorMachine = BasicActorMachineSimulator.prepareActor(actor);
					
					if(xml){
						XMLWriter doc = new XMLWriter(actorMachine);
						String xmlString = doc.toString();

						System.out.println(xmlString);
				        
				        // convert to JSON
						// JSON package from http://www.json.org https://github.com/douglascrockford/JSON-java
						/*
						try {
				        	int PRETTY_PRINT_INDENT_FACTOR = 2;
				        	org.json.JSONObject xmlJSONObj = org.json.XML.toJSONObject(xmlString);
				            String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
				            System.out.println(jsonPrettyPrintString);
				        } catch (org.json.JSONException je) {
				            System.out.println(je.toString());
				        }
				        */
				        
					}
				}
				else if(xml){
					XMLWriter doc = new XMLWriter(actor);
					doc.print();
				}
			}
		} else if(fileName.endsWith(".nl")){
			NlParser parser = new NlParser();
			NetworkDefinition network = parser.parse(path, fileName);
			parser.printParseProblems();
			if(parser.parseProblems.isEmpty()){
				network = BasicNetworkSimulator.prepareNetworkDefinition(network);
				if(prettyPrint){
					PrettyPrint pp = new PrettyPrint();
					pp.print(network);
				}
				if(xml){
					XMLWriter doc = new XMLWriter(network);
					doc.print();
				}
			}
		} else{
			System.out.println("unknown file extension");
		}
		System.out.println("---- done ---");
	}
}