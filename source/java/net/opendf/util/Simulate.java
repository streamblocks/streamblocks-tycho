package net.opendf.util;

import java.util.Map;

import net.opendf.interp.BasicInterpreter;
import net.opendf.interp.BasicNetworkSimulator;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.Expression;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.ast.NetworkDefinition;
import net.opendf.ir.net.ast.evaluate.NetDefEvaluator;
import net.opendf.ir.util.DeclLoader;
import net.opendf.ir.util.ImmutableList;

public class Simulate {
	static final String usage = "Correct use: java net.opendf.util.Simulate path entityName" +
			"\nThe entityName should not include the file extension, i.e. use 'Top', not 'Top.nl'";

	public static void main(String[] args){
		int index = 0;

		if(index+2 != args.length){
			System.err.println(usage);				
			return;
		}

		String path = args[index++];
		String entityName = args[index++];
		DeclLoader declLoader= new DeclLoader(path);
		Decl e = declLoader.getDecl(entityName);
		if(e instanceof NetworkDefinition){
			NetworkDefinition netDef = (NetworkDefinition)e;
			Network net = BasicNetworkSimulator.prepareNetworkDefinition(netDef, declLoader);
			BasicNetworkSimulator simulator = new BasicNetworkSimulator(net, 100);

			// run the simulation
			while(simulator.step()){ 			}

			StringBuffer sb = new StringBuffer();
			simulator.scopesToString(sb);
			System.out.println(sb);				
		} else {
			System.err.println(entityName + " is not a network.");
		}
	}
}
