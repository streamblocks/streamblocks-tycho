package se.lth.cs.tycho.errorhandling;

import java.util.ArrayList;

import se.lth.cs.tycho.interp.exception.CALCompiletimeException;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.parser.SourceCodeOracle;
import se.lth.cs.tycho.parser.SourceCodeOracle.SourceCodePosition;

public class BasicErrorModule implements ErrorModule {
	private SourceCodeOracle sourceOracle;
	private ArrayList<String> errors = new ArrayList<String>();
	private ArrayList<String> warnings = new ArrayList<String>();
	
	public BasicErrorModule(SourceCodeOracle sourceOracle){
		this.sourceOracle = sourceOracle;
	}

	@Override
	public void error(String msg, IRNode position){
		SourceCodePosition pos = null;
		if(sourceOracle != null && position != null){
			pos = sourceOracle.getSrcLocations(position.getIdentifier());
		}
		if(pos == null){
			errors.add(msg);
		} else {
			errors.add(msg + " between " + pos.getStartLine() + ":" + pos.getStartColumn() + " and " + pos.getEndLine() + ":" + pos.getEndColumn() + " in " + pos.getFileName());
		}
	}

	@Override
	public void warning(String msg, IRNode position){
		SourceCodePosition pos = null;
		if(sourceOracle != null && position != null){
			pos = sourceOracle.getSrcLocations(position.getIdentifier());
		}
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
