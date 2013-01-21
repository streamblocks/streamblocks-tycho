package net.opendf.interp.preprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import net.opendf.ir.common.PortName;

public class HashMapEnv implements Env {
	private final Env parent;
	private final IntCounter memoryCounter;
	private int stackCounter;
	private Map<String, VarPos> varPos;
	private Map<PortName, Integer> portPos;

	public HashMapEnv(Env parent, IntCounter memoryCounter, int stackCounter) {
		this.parent = parent;
		this.memoryCounter = memoryCounter;
		varPos = new HashMap<String, VarPos>();
		this.stackCounter = stackCounter;
		portPos = new HashMap<PortName, Integer>();
	}
	
	public HashMapEnv() {
		this(null, new IntCounter(), 0);
	}
	
	@Override
	public void putPort(PortName port, int i) {
		portPos.put(port, i);
	}
	
	@Override
	public int getPort(PortName port) {
		Integer i = portPos.get(port);
		if (i == null) {
			if (parent == null) {
				throw new NoSuchElementException(port.toString());
			} else {
				return parent.getPort(port);
			}
		} else {
			return i;
		}
	}
	
	@Override
	public boolean hasPort(PortName port) {
		return portPos.containsKey(port) || parent != null && parent.hasPort(port);
	}
	
	@Override
	public void putOnStack(String varName) {
		if (varPos.containsKey(varName)) {
			throw new IllegalArgumentException(varName);
		}
		varPos.put(varName, VarPos.stack(stackCounter++));
	}

	@Override
	public void putInMemory(String varName) {
		if (varPos.containsKey(varName)) {
			throw new IllegalArgumentException(varName);
		}
		varPos.put(varName, VarPos.mem(memoryCounter.next()));
	}

	@Override
	public boolean isOnStack(String varName) {
		VarPos pos = varPos.get(varName);
		if (pos == null) {
			if (parent == null) {
				throw new NoSuchElementException();
			} else {
				return parent.isOnStack(varName);
			}
		} else {
			return pos.isOnStack();
		}
	}

	@Override
	public boolean isInMemory(String varName) {
		VarPos pos = varPos.get(varName);
		if (pos == null) {
			if (parent == null) {
				throw new NoSuchElementException();
			} else {
				return parent.isInMemory(varName);
			}
		} else {
			return pos.isInMemory();
		}
	}

	@Override
	public int getPosition(String varName) {
		VarPos pos = varPos.get(varName);
		if (pos == null) {
			if (parent == null) {
				throw new NoSuchElementException();
			} else {
				return parent.getPosition(varName);
			}
		} else {
			if (pos.isOnStack()) {
				return stackCounter - pos.getPosition() - 1;
			} else {
				return pos.getPosition();
			}
		}
	}

	@Override
	public Env createFrame() {
		return new HashMapEnv(this, memoryCounter, stackCounter);
	}

	@Override
	public boolean contains(String varName) {
		if (varPos.containsKey(varName)) return true;
		if (parent != null) return parent.contains(varName);
		return false;
	}

}
