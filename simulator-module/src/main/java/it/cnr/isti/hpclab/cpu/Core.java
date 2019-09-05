package it.cnr.isti.hpclab.cpu;

/**
 * A class which represents a CPU core.
 * @author Matteo Catena
 *
 */
public abstract class Core {

	/**
	 * The CPU which the core belongs to.
	 */
	protected CPU cpu;
	/**
	 * A variable which indicates wheter the core is busy performing some work or
	 * it is idle.
	 */
	protected boolean busy;

	/**
	 * 
	 * @param cpu The CPU which the core belongs to.
	 */
	protected Core(CPU cpu) {

		this.cpu = cpu;
		busy = false;
	}
	
	/**
	 * Get the frequency which the core is operating at.
	 * @return
	 */
	public abstract int getFrequency();
	
	/**
	 * Mark the core as busy at time {@code timeMicroseconds}
	 * @param timeMicroseconds The time at which the core becomes busy
	 */
	public void busy(long timeMicroseconds) {

		if (!busy) {
			busy = true;
			cpu.update(timeMicroseconds);
		}
		
	}

	/**
	 * Free the core at time {@code timeMicroseconds}
	 * @param timeMicroseconds The time at which the core becomes free
	 */
	public void free(long timeMicroseconds) {
		
		if (busy) {
			busy = false;
			cpu.update(timeMicroseconds);
		}
	}
	
	/**
	 * Is the core currently busy?
	 * @return
	 */
	public boolean isBusy() {
		
		return busy;
	}

	/**
	 * Set the core operating frequency to its maximum at time {@code timeMicroseconds} 
	 * @param timeMicroseconds The time at which the core switches frequency.
	 */
	public void setMaxFrequency(long timeMicroseconds) {
		
		setFrequency(cpu.getMaxFrequency(), timeMicroseconds);
	}

	/**
	 * Set the core operating frequency to its minimum at time {@code timeMicroseconds} 
	 * @param timeMicroseconds The time at which the core switches frequency.
	 */
	public void setMinFrequency(long timeMicroseconds) {

		setFrequency(cpu.getMinFrequency(), timeMicroseconds);
	}

	/**
	 * Set the core operating frequency to {@code frequency} at time {@code timeMicroseconds} 
	 * 
	 * @param frequency The target frequency
	 * @param timeMicroseconds The time at which the core switches frequency.
	 */
	public abstract void setFrequency(int frequency, long timeMicroseconds);

	/**
	 * Get a reference to the CPU which the core belongs to.
	 * @return
	 */
	public CPU getCpu() {
		
		return cpu;
	}
}
