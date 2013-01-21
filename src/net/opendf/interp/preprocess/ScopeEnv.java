package net.opendf.interp.preprocess;

public final class ScopeEnv extends HashMapEnv {
	@Override
	public void putOnStack(String varName) {
		throw new UnsupportedOperationException();
	}

	public ScopeEnv(ScopeEnv parent, IntCounter memoryCounter) {
		super(parent, memoryCounter, 0);
	}

	public ScopeEnv() {
		super();
	}
}
