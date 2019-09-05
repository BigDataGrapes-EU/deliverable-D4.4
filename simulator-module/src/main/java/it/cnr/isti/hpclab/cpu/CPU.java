package it.cnr.isti.hpclab.cpu;

import it.cnr.isti.hpclab.engine.ShardServer;

/**
 * This class represents a server CPU.
 * @author Matteo Catena
 */
public abstract class CPU {

	/**
	 * Number of CPU cores
	 */
	protected int numCores;
	/**
	 * Available operating frequencies
	 */
	protected int[] frequencies;
	/**
	 * CPU cores
	 */
	protected Core[] cores;
	/**
	 * The time at which the CPU changes its state (e.g., its operating frequency)
	 */
	protected long statusChangeTime;
	/**
	 * The server which is equipped with this CPU
	 */
	protected ShardServer server;
	
	/**
	 * 
	 * @param server The server which is equipped with this CPU
	 * @param numCores The number of cores for this CPU
	 */
	protected CPU(ShardServer server, int numCores) {

		this.numCores = numCores;
		
		this.statusChangeTime = 0;
		
		this.cores = new Core[numCores];
		for (int i = 0; i < numCores; i++) 
			cores[i] = getNewCoreInstance(this);
				
		this.server = server;
	}

	/**
	 * Instantiate a new CPU core
	 * @param cpu The CPU to which the core belongs to.
	 * @return
	 */
	protected abstract Core getNewCoreInstance(CPU cpu);

	/**
	 * Update the status of the CPU at time {@code timeMicroseconds}
	 * @param timeMicroseconds the time at which the update occurs.
	 */
	protected abstract void update(long timeMicroseconds);

	/**
	 * Get a reference to the i-th core of this CPU.
	 * @param i The core identifier.
	 * @return
	 */
	public Core getCore(int i) {

		return cores[i];
	}

	/**
	 * Turn off this CPU at time {@code timeMicroseconds}
	 * @param timeMicroseconds The time at which the CPU is turn-off
	 */
	public abstract void shutdown(long timeMicroseconds);

	/**
	 * Get the minimum operating frequency
	 * @return
	 */
	public abstract int getMinFrequency();

	/**
	 * Get the maximum operating frequency
	 * @return
	 */
	public abstract int getMaxFrequency();

	/**
	 * Get the operating frequencies
	 * @return
	 */
	public abstract int[] getFrequencies();
	
	/**
	 * Get the number of cores
	 * @return
	 */
	public int getNumCores() {
		
		return numCores;
	}
	
	/**
	 * Get the number of busy cores (i.e., cores which are perfoming some work)
	 * @return
	 */
	protected int activeCores() {

		int activeCores = 0;
		for (Core c : cores) {
			
			if (c.isBusy()) {				
				activeCores++;
			}
		}
		
		return activeCores;
	}


}
