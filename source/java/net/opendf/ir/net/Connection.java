package net.opendf.ir.net;

import java.util.Objects;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.common.Port;

/**
 * A Connection links two ports.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class Connection extends AbstractIRNode {
	
	public Port getSrcPort() { return srcPort; }

	public String getSrcNodeName() { return srcNodeName; }
	
	public Port getDstPort() { return dstPort; }
	
	public String getDstNodeName(){ return dstNodeName; }
	//
	//  Ctor
	// 
	
	public Connection(String srcNodeName, Port srcPort, String dstNodeName, Port dstPort) {
		super(null);
	}
	
	protected Connection(Connection original, String srcNodeName, Port srcPort, String dstNodeName, Port dstPort) {
		super(original);
		this.srcNodeName = srcNodeName;
		this.srcPort = srcPort;
		this.dstNodeName = dstNodeName;
		this.dstPort = dstPort;
	}
	
	public Connection copy(String srcNodeName, Port srcPort, String dstNodeName, Port dstPort){
		if(Objects.equals(this.srcNodeName, srcNodeName) && Objects.equals(this.srcPort, srcPort) && Objects.equals(this.dstNodeName,  dstNodeName) && Objects.equals(this.dstPort,  dstPort)){
			return this;
		}
		return new Connection(this, srcNodeName, srcPort, dstNodeName, dstPort);
	}
	
	private Port srcPort, dstPort;
	private String srcNodeName, dstNodeName;
}
