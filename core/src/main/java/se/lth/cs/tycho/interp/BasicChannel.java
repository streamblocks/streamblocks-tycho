package se.lth.cs.tycho.interp;

import java.util.Arrays;

import se.lth.cs.tycho.interp.values.BasicRef;
import se.lth.cs.tycho.interp.values.Ref;
import se.lth.cs.tycho.interp.values.RefView;

public class BasicChannel implements Channel {

	private final In inputEnd;
	private Out[] outputEnds;
	private final int size;

	public BasicChannel(int size) {
		this.size = size;
		inputEnd = new In();
		outputEnds = new Out[0];
	}

	@Override
	public In getInputEnd() {
		return inputEnd;
	}

	@Override
	public Out createOutputEnd() {
		outputEnds = Arrays.copyOf(outputEnds, outputEnds.length + 1);
		Out out = new Out();
		outputEnds[outputEnds.length - 1] = out;
		return out;
	}

	private class In implements Channel.InputEnd {

		@Override
		public void write(RefView r) {
			assert space(1);
			for (Out out : outputEnds) {
				int index = (out.head + out.length) % size;
				r.assignTo(out.buffer[index]);
				out.length += 1;
			}
		}

		@Override
		public boolean space(int n) {
			for (Out out : outputEnds) {
				if (size - out.length < n) {
					return false;
				}
			}
			return true;
		}

	}

	private class Out implements Channel.OutputEnd {
		private Ref[] buffer;
		private int head;
		private int length;

		private Out() {
			buffer = new BasicRef[size];
			for (int i = 0; i < size; i++) {
				buffer[i] = new BasicRef();
			}
		}

		@Override
		public void peek(int i, Ref r) {
			assert i < length;
			assert i >= 0;
			int index = (head + i) % size;
			buffer[index].assignTo(r);
		}

		@Override
		public void remove(int n) {
			assert n > 0;
			assert n <= length;
			head = (head + n) % size;
			length -= n;
		}

		@Override
		public boolean tokens(int n) {
			return n <= length;
		}

	}

}
