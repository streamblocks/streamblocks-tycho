package se.lth.cs.tycho.interp;

import java.util.BitSet;

import se.lth.cs.tycho.interp.values.BasicRef;
import se.lth.cs.tycho.interp.values.Ref;
import se.lth.cs.tycho.interp.values.RefView;

public class BasicStack implements Stack {
	private final BasicRef[] stack;
	private int next;
	private final BitSet closure;
	
	public int currentHeight() {
		return next;
	}
	
	public BasicStack(int size) {
		closure = new BitSet(size);
		stack = new BasicRef[size];
		for (int i = 0; i < size; i++) {
			stack[i] = new BasicRef();
		}
		next = 0;
	}

	@Override
	public Ref pop() {
		next--;
		if (closure.get(next)) {
			stack[next] = new BasicRef();
			closure.clear(next);
		}
//        System.out.println("pop " + stack[next]);
		return stack[next];
	}

	@Override
	public void remove(int n) {
		for (int i = closure.nextSetBit(next-n); i >= 0; i = closure.nextSetBit(i+1)) {
			stack[i] = new BasicRef();
			closure.clear(i);
		}
		next -= n;
	}

	@Override
	public void push(RefView r) {
//		System.out.println("push " + r);
		r.assignTo(stack[next++]);
	}

	@Override
	public Ref push() {
		return stack[next++];
	}

	@Override
	public void alloca(int n) {
		next += n;
	}

	@Override
	public Ref peek(int i) {
		return stack[next - i - 1];
	}

	@Override
	public boolean isEmpty() {
		return next==0;
	}

	@Override
	public Ref closure(int select) {
		closure.set(select);
		return peek(select);
	}

}
