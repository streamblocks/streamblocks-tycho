package net.opendf.interp.preprocess;

import java.util.List;
import java.util.NoSuchElementException;

import net.opendf.ir.common.PortName;

public class ScopeListEnv implements Env {

	private final Iterable<ScopeEnv> list;
	private final IntCounter memoryCounter;

	public ScopeListEnv(List<ScopeEnv> list, IntCounter memoryCounter) {
		this.list = list;
		this.memoryCounter = memoryCounter;
	}

	@Override
	public void putOnStack(String varName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putInMemory(String varName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOnStack(String varName) {
		for (ScopeEnv env : list) {
			if (env.contains(varName)) {
				return env.isOnStack(varName);
			}
		}
		throw new NoSuchElementException(varName);
	}

	@Override
	public boolean isInMemory(String varName) {
		for (ScopeEnv env : list) {
			if (env.contains(varName)) {
				return env.isInMemory(varName);
			}
		}
		throw new NoSuchElementException(varName);
	}

	@Override
	public int getPosition(String varName) {
		for (ScopeEnv env : list) {
			if (env.contains(varName)) {
				return env.getPosition(varName);
			}
		}
		throw new NoSuchElementException(varName);
	}

	@Override
	public Env createFrame() {
		return new HashMapEnv(this, memoryCounter, 0);
	}

	@Override
	public boolean contains(String varName) {
		for (ScopeEnv env : list) {
			if (env.contains(varName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasPort(PortName port) {
		for (ScopeEnv env : list) {
			if (env.hasPort(port))
				return true;
		}
		return false;
	}

	@Override
	public int getPort(PortName port) {
		for (ScopeEnv env : list) {
			if (env.hasPort(port))
				return env.getPort(port);
		}
		throw new NoSuchElementException(port.toString());
	}

	@Override
	public void putPort(PortName port, int i) {
		throw new UnsupportedOperationException();
	}

}
