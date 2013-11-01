package net.opendf.interp.exception;

import net.opendf.ir.IRNode;

public class CALNumberFormatException extends CALRuntimeException {

	private static final long serialVersionUID = 1L;

	public CALNumberFormatException(String msg) {
		super(msg);
	}
	public CALNumberFormatException(String msg, IRNode source) {
		super(msg, source);
	}

}
