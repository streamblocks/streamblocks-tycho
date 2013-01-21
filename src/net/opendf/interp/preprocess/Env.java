package net.opendf.interp.preprocess;

import net.opendf.ir.common.PortName;

public interface Env {
	public boolean hasPort(PortName port);

	public int getPort(PortName port);

	public void putPort(PortName port, int i);

	public boolean contains(String varName);

	public void putOnStack(String varName);

	public void putInMemory(String varName);

	public boolean isOnStack(String varName);

	public boolean isInMemory(String varName);

	public int getPosition(String varName);

	public Env createFrame();
}
