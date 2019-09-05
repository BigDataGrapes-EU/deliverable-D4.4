package it.cnr.isti.hpclab.cpu;

/**
 * An interface mimicking some of the functionalities of the Intel [R]unning [A]verage [P]ower [L]imit 
 * technology. Link: https://01.org/blogs/2014/running-average-power-limit-%E2%80%93-rapl
 * @author matteo
 *
 */
public interface RAPL {

	/**
	 * Get the system power cap
	 * @return
	 */
	public double getPowerCap();	
	/**
	 * Set the maximimum power cap at time {@code timeMicroseconds}
	 * @param timeMicroseconds the time at which the power cap is set.
	 */
	public void setMaxPowerCap(long timeMicroseconds);
	/**
	 * Set a {@code powerCap} power cap at time {@code timeMicroseconds}. In other words, we
	 * limit the power consumption of the cpu to {@code powerCap} at time {@code timeMicroseconds}.
	 * As a consequence, this limits the range of available CPU core frequencies. 
	 * @param powerCap The power cap
	 * @param timeMicroseconds the time at which the power cap is set.
	 */
	public void setPowerCap(double powerCap, long timeMicroseconds);	
}
