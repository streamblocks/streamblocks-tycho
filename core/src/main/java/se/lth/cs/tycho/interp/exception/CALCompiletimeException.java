package se.lth.cs.tycho.interp.exception;

import se.lth.cs.tycho.errorhandling.ErrorModule;

public class CALCompiletimeException extends java.lang.RuntimeException{
	private ErrorModule errors;
	
	public CALCompiletimeException(String msg, ErrorModule errors) {
		super(msg);
		this.errors = errors;
	}

	public ErrorModule getErrorModule(){
		return errors;
	}
	private static final long serialVersionUID = 1L;

}
