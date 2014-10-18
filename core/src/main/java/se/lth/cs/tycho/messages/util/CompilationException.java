package se.lth.cs.tycho.messages.util;

import se.lth.cs.tycho.messages.Message;

public class CompilationException extends RuntimeException {
	private static final long serialVersionUID = -3307162795667124857L;
	private final Message message;

	public CompilationException(Message message) {
		super(message.getText());
		this.message = message;
	}

	/**
	 * Returns the message object that was provided to the constructor
	 * 
	 * @return the message object
	 */
	public Message getCompilationMessage() {
		return message;
	}
}
