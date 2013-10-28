package net.opendf.interp.values;


public abstract class ConstRef implements RefView {
	private static final String wrongType = "Wrong type";
	
	private ConstRef() {}
	
	public static ConstRef of(Value v) {
		return new ConstValueRef(v);
	}
	
	public static ConstRef of(double d) {
		return new ConstDoubleRef(d);
	}
	
	public static ConstRef of(long l) {
		return new ConstLongRef(l);
	}
	
	private static class ConstValueRef extends ConstRef {
		private final Value v;
		
		public ConstValueRef(Value v) {
			this.v = v;
		}
		
		@Override
		public Value getValue() {
			return v;
		}

		@Override
		public void assignTo(Ref r) {
			r.setValue(v.copy());
		}
	}
	
	private static class ConstDoubleRef extends ConstRef {
		private final double d;
		
		public ConstDoubleRef(double d) {
			this.d = d;
		}
		
		@Override
		public double getDouble() {
			return d;
		}
		
		@Override
		public void assignTo(Ref r) {
			r.setDouble(d);
		}
	}
	
	private static class ConstLongRef extends ConstRef {
		private final long l;
		
		public ConstLongRef(long l) {
			this.l = l;
		}
		
		@Override
		public long getLong() {
			return l;
		}
		
		@Override
		public void assignTo(Ref r) {
			r.setLong(l);
		}
	}

	@Override
	public Value getValue() {
		throw new IllegalStateException(wrongType);
	}

	@Override
	public long getLong() {
		throw new IllegalStateException(wrongType);
	}

	@Override
	public double getDouble() {
		throw new IllegalStateException(wrongType);
	}

	@Override
	public String getString() {
		throw new IllegalStateException(wrongType);
	}

	@Override
	public abstract void assignTo(Ref r);
}
