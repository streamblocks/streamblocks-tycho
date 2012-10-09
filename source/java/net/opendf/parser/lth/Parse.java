package net.opendf.parser.lth;
/**
 *  copyright (c) 20011, Per Andersson
 *  all rights reserved
 **/

import net.opendf.ir.cal.Actor;
import net.opendf.parser.lth.CalParser;
import net.opendf.util.PrettyPrint;

public class Parse{
	static final String usage = "Correct use: java net.opendf.parser.lth.Parse path file" +
			"\nParse a CAL file and do a pretty print of the internal representation." +
			"\nThe file name should include the file extension, i.e. 'Add.cal'";

	public static void main(String[] args){
		if(args.length != 2){
			System.err.println(usage);				
			return;
		}
		String path = args[0];
		String fileName = args[1];

		System.out.println("------- " + System.getProperty("user.dir") + "/" + path + "/" + fileName + " (" +  new java.util.Date() + ") -------");
		CalParser parser = new CalParser();
		Actor actor = parser.parse(path, fileName);
		parser.printParseProblems();
		if(parser.parseProblems.isEmpty()){
			PrettyPrint pp = new PrettyPrint();
			pp.print(actor);
		}
		System.out.println("---- done ---");
	}
}