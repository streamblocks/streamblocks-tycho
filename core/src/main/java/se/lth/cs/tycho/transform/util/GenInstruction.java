package se.lth.cs.tycho.transform.util;

import java.util.Map;

import se.lth.cs.tycho.instance.am.ICall;
import se.lth.cs.tycho.instance.am.ITest;
import se.lth.cs.tycho.instance.am.IWait;

public abstract class GenInstruction<S> {

	private GenInstruction() {
	}

	public abstract S[] destinations();

	public abstract <R, P> R accept(Visitor<S, R, P> visitor, P param);

	public <R> R accept(Visitor<S, R, Void> visitor) {
		return accept(visitor, null);
	}

	public boolean isCall() {
		return false;
	}

	public boolean isTest() {
		return false;
	}

	public boolean isWait() {
		return false;
	}
	
	public Call<S> asCall() {
		throw new ClassCastException();
	}
	
	public Test<S> asTest() {
		throw new ClassCastException();
	}
	
	public Wait<S> asWait() {
		throw new ClassCastException();
	}

	public abstract se.lth.cs.tycho.instance.am.Instruction generateInstruction(Map<S, Integer> stateMap);

	public static class Call<S> extends GenInstruction<S> {
		private final int t;
		private final S s;

		public Call(int t, S s) {
			this.t = t;
			this.s = s;
		}

		public int T() {
			return t;
		}

		public S S() {
			return s;
		}

		@Override
		public <R, P> R accept(Visitor<S, R, P> visitor, P parameter) {
			return visitor.visitCall(this, parameter);
		}

		@Override
		public S[] destinations() {
			@SuppressWarnings("unchecked")
			S[] states = (S[]) new Object[] { s };
			return states;
		}

		@Override
		public boolean isCall() {
			return true;
		}
		
		@Override
		public Call<S> asCall() {
			return this;
		}

		@Override
		public ICall generateInstruction(Map<S, Integer> stateMap) {
			return new ICall(t, stateMap.get(s));
		}
	}

	public static class Test<S> extends GenInstruction<S> {
		private final int c;
		private final S s0;
		private final S s1;

		public Test(int c, S s1, S s0) {
			this.c = c;
			this.s0 = s0;
			this.s1 = s1;
		}

		public int C() {
			return c;
		}

		public S S0() {
			return s0;
		}

		public S S1() {
			return s1;
		}

		@Override
		public <R, P> R accept(Visitor<S, R, P> visitor, P parameter) {
			return visitor.visitTest(this, parameter);
		}

		@Override
		public S[] destinations() {
			@SuppressWarnings("unchecked")
			S[] states = (S[]) new Object[] { s0, s1 };
			return states;
		}

		@Override
		public boolean isTest() {
			return true;
		}
		
		@Override
		public Test<S> asTest() {
			return this;
		}

		@Override
		public ITest generateInstruction(Map<S, Integer> stateMap) {
			return new ITest(c, stateMap.get(s1), stateMap.get(s0));
		}
	}

	public static class Wait<S> extends GenInstruction<S> {
		private final S s;

		public Wait(S s) {
			this.s = s;
		}

		public S S() {
			return s;
		}

		@Override
		public <R, P> R accept(Visitor<S, R, P> visitor, P parameter) {
			return visitor.visitWait(this, parameter);
		}

		public S[] destinations() {
			@SuppressWarnings("unchecked")
			S[] states = (S[]) new Object[] { s };
			return states;
		}

		@Override
		public boolean isWait() {
			return true;
		}
		
		@Override
		public Wait<S> asWait() {
			return this;
		}

		@Override
		public IWait generateInstruction(Map<S, Integer> stateMap) {
			return new IWait(stateMap.get(s));
		}
	}

	public interface Visitor<S, R, P> {
		public R visitCall(Call<S> call, P parameter);

		public R visitTest(Test<S> test, P parameter);

		public R visitWait(Wait<S> wait, P parameter);
	}
}
