package net.opendf.interp.values;

import net.opendf.interp.exception.CALRuntimeException;

public interface RefView {

	public Value getValue() throws CALRuntimeException;

	public long getLong() throws CALRuntimeException;

	public double getDouble() throws CALRuntimeException;
	
	public String getString() throws CALRuntimeException;

	public void assignTo(Ref r);

}