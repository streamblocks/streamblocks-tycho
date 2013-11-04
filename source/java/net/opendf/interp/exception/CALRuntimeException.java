package net.opendf.interp.exception;

import java.io.PrintStream;
import java.util.Stack;

import net.opendf.ir.IRNode;
import net.opendf.parser.SourceCodeOracle;
import net.opendf.parser.SourceCodeOracle.SourceCodePosition;

public class CALRuntimeException extends java.lang.RuntimeException{
	/**
	 * source is the point in the cal program that caused the problem.
	 */
	private Stack<IRNode> calStack = new Stack<IRNode>();
	
	public CALRuntimeException(String msg) {
		super(msg);
	}
	public CALRuntimeException(String msg, IRNode source) {
		super(msg);
		assert source != null;
		calStack.push(source);
	}
	
	public void pushCalStack(IRNode node){
		calStack.push(node);
	}

	public void printCalStack(PrintStream err, SourceCodeOracle oracle){
		err.print(calStackToString(oracle));
	}
	
	public String calStackToString(SourceCodeOracle oracle){
		StringBuffer sb = new StringBuffer();
		for(IRNode node : calStack){
			SourceCodePosition pos = oracle == null ? null : oracle.getSrcLocations(node.getIdentifier());
			if(pos != null){
				sb.append(node.getClass().getSimpleName() + ": between [" + pos.getStartLine() + ", " + pos.getStartColumn());
				sb.append("] and [" + pos.getEndLine() + ", " + pos.getEndColumn() + "] in "+ pos.getFileName());
				sb.append("\n");
			} else {
				sb.append(node.getClass().getSimpleName());
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	private static final long serialVersionUID = 1L;
}
