package net.opendf.interp;

import java.util.BitSet;

import net.opendf.interp.values.BasicRef;
import net.opendf.interp.values.Ref;

public class BasicMemory implements Memory {
	
	private final Ref[] mem;
	private final BitSet closure;
	
	public BasicMemory(int size) {
		mem = new BasicRef[size];
		for (int i = 0; i < size; i++) {
			mem[i] = new BasicRef();
		}
		closure = new BitSet(size);
	}
	
	private BasicMemory(Ref[] mem) {
		this.mem = mem;
		closure = new BitSet(mem.length);
	}

	@Override
	public Ref get(int index) {
		return mem[index];
	}

	@Override
	public Ref declare(int index) {
		if (closure.get(index)) {
			mem[index] = new BasicRef();
			closure.clear(index);
		}
		return mem[index];
	}

	@Override
	public BasicMemory closure(int[] select, Ref[] add) {
		Ref[] c = new Ref[select.length + add.length];
		int i = 0;
		for (int index : select) {
			c[i++] = mem[index];
			closure.set(index);
		}
		for (Ref r : add) {
			c[i++] = r;
		}
		return new BasicMemory(c);
	}

}
