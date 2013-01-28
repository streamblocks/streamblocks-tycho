package net.opendf.interp;

import net.opendf.interp.values.Ref;
import net.opendf.interp.values.RefView;

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
