package net.opendf.interp;

import java.util.NoSuchElementException;

import net.opendf.interp.values.BasicRef;
import net.opendf.interp.values.Ref;
import net.opendf.interp.values.RefView;

public class BasicFixedSizeChannel implements Channel {
	private final Ref[] buffer;
	private int first;
	private int elements;
	
	public BasicFixedSizeChannel(int length) {
		buffer = new Ref[length];
		for (int i = 0; i < length; i++) {
			buffer[i] = new BasicRef();
		}
		first = 0;
		elements = 0;
	}

	@Override
	public void peek(int i, Ref r) {
		if (i >= elements || i < 0) {
			throw new NoSuchElementException();
		}
		int j = (first + i) % buffer.length;
		buffer[j].assignTo(r);
	}

	@Override
	public void write(RefView r) {
		if (elements >= buffer.length) {
			throw new IllegalStateException("Buffer is full");
		}
		int i = (first + elements) % buffer.length;
		elements += 1;
		r.assignTo(buffer[i]);
	}

	@Override
	public void remove(int n) {
		if (n > elements || n < 0) {
			throw new IllegalArgumentException();
		}
		elements -= n;
		first = (first + n) % buffer.length;
	}

	@Override
	public boolean tokens(int n) {
		return n <= elements;
	}

	@Override
	public boolean space(int n) {
		return n + elements <= buffer.length;
	}
}
