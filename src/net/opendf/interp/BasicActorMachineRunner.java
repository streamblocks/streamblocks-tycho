package net.opendf.interp;

import java.util.BitSet;

import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Instruction;

public class BasicActorMachineRunner implements ActorMachineRunner {
	
	private final ActorMachine actorMachine;
	private final Instr[][] controller;
	private final BitSet liveVariables;

	public BasicActorMachineRunner(ActorMachine actorMachine) {
		this.actorMachine = actorMachine;
		this.controller = new Instr[0][0];
		this.liveVariables = new BitSet();
	}
	
	@Override
	public void step() {
		
		
	}
	
	private class Instr {
		public final Instruction instruction;
		public final BitSet requiredVariables;
		public final BitSet invalidatedVariables;
		public Instr(Instruction instruction, BitSet requiredVariables, BitSet invalidatedVariables) {
			this.instruction = instruction;
			this.requiredVariables = requiredVariables;
			this.invalidatedVariables = invalidatedVariables;
		}
	}
}
