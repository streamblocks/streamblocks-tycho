package se.lth.cs.tycho.phases.cbackend;

import org.multij.Module;
import se.lth.cs.tycho.types.Type;

@Module
public interface Channels {
	void channelCode(Type type);
	void inputActorCode(Type type);
	void outputActorCode(Type type);
}
