package it.cnr.isti.hpclab.cpu;

import it.cnr.isti.hpclab.engine.ShardServer;

/**
 * Use this class to build a CPU instance.
 * 
 * @author Matteo Catena
 */
public interface CPUBuilder {

	/**
	 * Create a new instance of CPU for {@code server}
	 * @param server The physical server equipped with the new CPU
	 * @return
	 */
	public CPU newInstance(ShardServer server);
	/**
	 * Get the operating frequencies available to this CPU
	 * @return
	 */
	public int[] getFrequencies();
}
