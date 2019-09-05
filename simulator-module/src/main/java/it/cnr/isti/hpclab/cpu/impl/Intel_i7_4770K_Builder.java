package it.cnr.isti.hpclab.cpu.impl;

import it.cnr.isti.hpclab.cpu.CPU;
import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.engine.ShardServer;

/**
 * A builder for the Intel i7-4770K CPU.
 * @author Matteo Catena
 *
 */
public class Intel_i7_4770K_Builder implements CPUBuilder {

	@Override
	public CPU newInstance(ShardServer server) {
		
		return new Intel_i7_4770K(server);
	}

	@Override
	public int[] getFrequencies() {
		
		return Intel_i7_4770K.frequencies;
	}

}
