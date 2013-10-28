package net.opendf.transform.util;

import java.util.ArrayList;

import net.opendf.errorhandling.ErrorModule;
import net.opendf.interp.exception.CALCompiletimeException;
import net.opendf.ir.IRNode;
import net.opendf.parser.SourceCodeOracle;
import net.opendf.parser.SourceCodeOracle.SourceCodePosition;

public class ErrorAwareBasicTransformer<P> extends AbstractBasicTransformer<P> implements ErrorModule {
	protected SourceCodeOracle sourceOracle;
	private ArrayList<String> errors = new ArrayList<String>();
	private ArrayList<String> warnings = new ArrayList<String>();
	
	public ErrorAwareBasicTransformer(SourceCodeOracle sourceOracle){
		this.sourceOracle = sourceOracle;
	}

	@Override
	public void error(String msg, IRNode position){
		SourceCodePosition pos = sourceOracle.getSrcLocations(position.getIdentifier());
		if(pos == null){
			errors.add(msg);
		} else {
			errors.add(msg + " between " + pos.getStartLine() + ":" + pos.getStartColumn() + " and " + pos.getEndLine() + ":" + pos.getEndColumn() + " in " + pos.getFileName());
		}
	}

	@Override
	public void warning(String msg, IRNode position){
		SourceCodePosition pos = sourceOracle.getSrcLocations(position.getIdentifier());
		if(pos == null){
			warnings.add(msg);
		} else {
			warnings.add(msg + " between " + pos.getStartLine() + ":" + pos.getStartColumn() + " and " + pos.getEndLine() + ":" + pos.getEndColumn() + " in " + pos.getFileName());
		}
	}
	
	@Override
	public void abortIfError() throws CALCompiletimeException{
		if(!errors.isEmpty()){
			throw new CALCompiletimeException("", this);
		}
	}
	
	@Override
	public void printMessages(){
		for(String w : warnings){
			System.err.println("WARNING: " + w);
		}
		for(String e : errors){
			System.err.println("ERROR: " + e);
		}
	}
	@Override
	public void printWarnings(){
		for(String w : warnings){
			System.err.println("WARNING: " + w);
		}
	}
	@Override
	public void printErrors(){
		for(String e : errors){
			System.err.println("ERROR: " + e);
		}
	}
	@Override
	public boolean hasWarning(){
		return !warnings.isEmpty();
	}
	@Override
	public boolean hasError(){
		return !errors.isEmpty();
	}
	@Override
	public boolean hasProblem(){
		return hasWarning() || hasError();
	}
}
