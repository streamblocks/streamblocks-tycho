package net.opendf.interp;

import net.opendf.analyze.memory.FreeVariablesTransformer;
import net.opendf.interp.preprocess.VariableOffsetTransformer;
import net.opendf.ir.net.ast.NetworkDefinition;

public class BasicNetworkSimulator {


	/**
	 * Transform an Actor to an ActorMachine which is prepared for interpretation
	 * @param actor
	 * @return
	 */
	public static NetworkDefinition prepareNetworkDefinition(NetworkDefinition def){
		// order variable initializations
		def = FreeVariablesTransformer.transformNetworkDefinition(def);
		// compute variable offsets
		VariableOffsetTransformer varT = new VariableOffsetTransformer();
		def = varT.transformNetworkDefinition(def);
		
		return def;
	}
	
}
