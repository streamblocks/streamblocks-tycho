package se.lth.cs.tycho.messages;

import se.lth.cs.tycho.messages.Message;
import se.lth.cs.tycho.messages.MessageReporter;


public class NullMessageReporter implements MessageReporter {

	@Override
	public void report(Message message) {
	}

}
