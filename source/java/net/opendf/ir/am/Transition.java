package net.opendf.ir.am;

import java.util.Map;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.common.PortName;
import net.opendf.ir.common.Statement;

/**
 * Objects of this class contain the information necessary to execute the code that needs to be run during a {@link ICall call instruction}.
 * This information includes the number of input tokens consumed and output tokens produced at each port during the transition, the identifier
 * of the <it>scope</it> (see {@link ActorMachine} for details) the code is to be run in, and the code itself.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class Transition extends AbstractIRNode {

	public Map<PortName, Integer>	getInputRates() { return inputRates; }

	public int						getInputRate(PortName p) {
		if (inputRates.containsKey(p)) {
			return inputRates.get(p);
		} else {
			return 0;
		}
	}
	
	public Map<PortName, Integer>	getOutputRates() { return outputRates; }

	public int						getOutputRate(PortName p) {
		if (outputRates.containsKey(p)) {
			return outputRates.get(p);
		} else {
			return 0;
		}
	}
	
	public int		  getScope() { return scope; }
	
	public Statement[]  getBody() { return body; }
	
	//
	//  Ctor
	//
	
	public Transition(Map<PortName, Integer> inputRates, Map<PortName, Integer> outputRates, int scope, Statement [] body) {
		this.inputRates = inputRates;
		this.outputRates = outputRates;
		this.scope = scope;
		this.body = body;
	}
	

	private int						scope;
	private Statement []			body;
	private Map<PortName, Integer>  inputRates;
	private Map<PortName, Integer>	outputRates;	
}
