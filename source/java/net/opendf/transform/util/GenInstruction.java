package net.opendf.transform.util;

import java.util.Map;

import net.opendf.ir.am.ICall;
import net.opendf.ir.am.ITest;
import net.opendf.ir.am.IWait;

public abstract class GenInstruction<S> {

	public abstract S[] destinations();

	public boolean isCall() {
		return false;
	}

	public boolean isTest() {
		return false;
	}

	public boolean isWait() {
		return false;
	}

	public abstract net.opendf.ir.am.Instruction generateInstruction(Map<S, Integer> stateMap);

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
		public IWait generateInstruction(Map<S, Integer> stateMap) {
			return new IWait(stateMap.get(s));
		}
	}
}
