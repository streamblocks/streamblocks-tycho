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
		calStack.push(source);
	}
	
	public void pushCalStack(IRNode node){
		calStack.push(node);
	}

	public void printCalStack(PrintStream err, SourceCodeOracle oracle){
		for(IRNode node : calStack){
			SourceCodePosition pos = oracle.getSrcLocations(node.getIdentifier());
			if(pos != null){
				err.println(node.getClass().getSimpleName() + ": between [" + pos.getStartLine() + ", " + pos.getStartColumn() + "] and [" + pos.getEndLine() + ", " + pos.getEndColumn() + "] in "+ pos.getFileName());
			} else {
				err.println(node.getClass().getSimpleName());
			}
			
		}
	}
	
	private static final long serialVersionUID = 1L;
}
