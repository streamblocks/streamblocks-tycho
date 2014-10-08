package se.lth.cs.tycho.interp;

import se.lth.cs.tycho.interp.values.Ref;
import se.lth.cs.tycho.interp.values.RefView;

public interface Channel {
	public InputEnd getInputEnd();

	public OutputEnd createOutputEnd();

	public static interface InputEnd {
		public void write(RefView r);

		public boolean space(int n);
	}

	public static interface OutputEnd {
		public void peek(int i, Ref r);

		public void remove(int n);

		public boolean tokens(int n);
	}
}
