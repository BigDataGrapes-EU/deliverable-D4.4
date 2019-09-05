package it.cnr.isti.hpclab.cpu.impl;

import it.cnr.isti.hpclab.cpu.CPU;
import it.cnr.isti.hpclab.cpu.Core;

/**
 * A core of the Intel i7-4770K CPU
 * @author Matteo Catena
 *
 */
public class Intel_i7_4770K_Core extends Core {

	protected int currentFrequency;

	Intel_i7_4770K_Core(CPU cpu) {
		super(cpu);
	}

	@Override
	public int getFrequency() {
		
		return ((Intel_i7_4770K)cpu).getCurrentMaxFrequency();
	}
	
	@Override
	public void setFrequency(int frequency, long timeMicroseconds) {

		int previousFrequency = currentFrequency;
		currentFrequency = frequency;		
		if (currentFrequency != previousFrequency) 
			((Intel_i7_4770K)cpu).update(timeMicroseconds);
		
	}

}
