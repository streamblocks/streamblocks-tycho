package net.opendf.interp.exception;

import net.opendf.ir.IRNode;

public class CALIndexOutOfBoundsException extends CALRuntimeException {

	private static final long serialVersionUID = 1L;

	public CALIndexOutOfBoundsException(String msg) {
		super(msg);
	}
	public CALIndexOutOfBoundsException(String msg, IRNode source) {
		super(msg, source);
	}

}
