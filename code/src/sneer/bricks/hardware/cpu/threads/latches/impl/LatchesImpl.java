package sneer.bricks.hardware.cpu.threads.latches.impl;

import sneer.bricks.hardware.cpu.threads.latches.Latch;
import sneer.bricks.hardware.cpu.threads.latches.Latches;

class LatchesImpl implements Latches {

	@Override
	public Latch produce() {
		return new LatchImpl();
	}

}
