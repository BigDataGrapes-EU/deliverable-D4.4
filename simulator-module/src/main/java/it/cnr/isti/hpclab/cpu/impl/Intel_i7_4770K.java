package it.cnr.isti.hpclab.cpu.impl;

import java.util.Arrays;

import it.cnr.isti.hpclab.cpu.CPU;
import it.cnr.isti.hpclab.cpu.Core;
import it.cnr.isti.hpclab.cpu.RAPL;
import it.cnr.isti.hpclab.engine.ShardServer;
import it.cnr.isti.hpclab.simulator.OLDISimulator;

/**
 * A power consumpion model for the Intel i7-4770K, when the workload is  
 * neither completely CPU- nor IO-bound as per typical datacenter workloads [1]. 
 * The power model is derived by monitoring the CPU energy consumption while running 
 * the Terrier IR platform, as described in [2].
 * 
 * [1] S. Kanev, K. Hazelwood, G. Wei, and D. Brooks, "Tradeoffs between power management and tail latency in warehouse-scale applications". IISWC 2014.
 * [2] M. Catena, O. Frieder, and N. Tonellotto, "Efficient Energy Management in Distributed Web Search". CIKM 2018.
 * 
 * @author Matteo Catena
 *
 */
class PowerModel {
	
	//indexes are: n. active cores - 1, i-th power consumption, value is the consumed watts
	static final double[][] powerCap2frequency;
	//indexes are: n. active cores - 1, i-th frequency, value is the frequency in kHz
	static final int[][] frequency2powerCap;
	static final double IDLE;
	
	static final double minPowerCap;
	static final double maxPowerCap;
	
	private PowerModel() {}
	
	static {
		
		IDLE=0.8;
		
		powerCap2frequency = new double[Intel_i7_4770K.NUMCORES][15];
		frequency2powerCap = new int[Intel_i7_4770K.NUMCORES][15];
		
		powerCap2frequency[0][0]=1.2;
		frequency2powerCap[0][0]=800000;
		powerCap2frequency[0][1]=1.6;
		frequency2powerCap[0][1]=1000000;
		powerCap2frequency[0][2]=2.3;
		frequency2powerCap[0][2]=1200000;
		powerCap2frequency[0][3]=3.2;
		frequency2powerCap[0][3]=1400000;
		powerCap2frequency[0][4]=3.9;
		frequency2powerCap[0][4]=1600000;
		powerCap2frequency[0][5]=4.8;
		frequency2powerCap[0][5]=1800000;
		powerCap2frequency[0][6]=5.7;
		frequency2powerCap[0][6]=2000000;
		powerCap2frequency[0][7]=5.9;
		frequency2powerCap[0][7]=2100000;
		powerCap2frequency[0][8]=6.8;
		frequency2powerCap[0][8]=2300000;
		powerCap2frequency[0][9]=7.9;
		frequency2powerCap[0][9]=2500000;
		powerCap2frequency[0][10]=8.9;
		frequency2powerCap[0][10]=2700000;
		powerCap2frequency[0][11]=9.9;
		frequency2powerCap[0][11]=2900000;
		powerCap2frequency[0][12]=11.2;
		frequency2powerCap[0][12]=3100000;
		powerCap2frequency[0][13]=12.3;
		frequency2powerCap[0][13]=3300000;
		powerCap2frequency[0][14]=14.1;
		frequency2powerCap[0][14]=3500000;
		powerCap2frequency[1][0]=2.3;
		frequency2powerCap[1][0]=800000;
		powerCap2frequency[1][1]=3.3;
		frequency2powerCap[1][1]=1000000;
		powerCap2frequency[1][2]=5.1;
		frequency2powerCap[1][2]=1200000;
		powerCap2frequency[1][3]=6.2;
		frequency2powerCap[1][3]=1400000;
		powerCap2frequency[1][4]=7.5;
		frequency2powerCap[1][4]=1600000;
		powerCap2frequency[1][5]=8.7;
		frequency2powerCap[1][5]=1800000;
		powerCap2frequency[1][6]=9.6;
		frequency2powerCap[1][6]=2000000;
		powerCap2frequency[1][7]=10.0;
		frequency2powerCap[1][7]=2100000;
		powerCap2frequency[1][8]=11.3;
		frequency2powerCap[1][8]=2300000;
		powerCap2frequency[1][9]=13.5;
		frequency2powerCap[1][9]=2500000;
		powerCap2frequency[1][10]=14.9;
		frequency2powerCap[1][10]=2700000;
		powerCap2frequency[1][11]=16.1;
		frequency2powerCap[1][11]=2900000;
		powerCap2frequency[1][12]=17.7;
		frequency2powerCap[1][12]=3100000;
		powerCap2frequency[1][13]=19.6;
		frequency2powerCap[1][13]=3300000;
		powerCap2frequency[1][14]=22.4;
		frequency2powerCap[1][14]=3500000;
		powerCap2frequency[2][0]=3.7;
		frequency2powerCap[2][0]=800000;
		powerCap2frequency[2][1]=5.2;
		frequency2powerCap[2][1]=1000000;
		powerCap2frequency[2][2]=7.9;
		frequency2powerCap[2][2]=1200000;
		powerCap2frequency[2][3]=9.0;
		frequency2powerCap[2][3]=1400000;
		powerCap2frequency[2][4]=10.6;
		frequency2powerCap[2][4]=1600000;
		powerCap2frequency[2][5]=11.9;
		frequency2powerCap[2][5]=1800000;
		powerCap2frequency[2][6]=13.5;
		frequency2powerCap[2][6]=2000000;
		powerCap2frequency[2][7]=14.2;
		frequency2powerCap[2][7]=2100000;
		powerCap2frequency[2][8]=15.9;
		frequency2powerCap[2][8]=2300000;
		powerCap2frequency[2][9]=18.9;
		frequency2powerCap[2][9]=2500000;
		powerCap2frequency[2][10]=20.7;
		frequency2powerCap[2][10]=2700000;
		powerCap2frequency[2][11]=22.8;
		frequency2powerCap[2][11]=2900000;
		powerCap2frequency[2][12]=25.5;
		frequency2powerCap[2][12]=3100000;
		powerCap2frequency[2][13]=28.2;
		frequency2powerCap[2][13]=3300000;
		powerCap2frequency[2][14]=31.2;
		frequency2powerCap[2][14]=3500000;
		powerCap2frequency[3][0]=4.6;
		frequency2powerCap[3][0]=800000;
		powerCap2frequency[3][1]=5.7;
		frequency2powerCap[3][1]=1000000;
		powerCap2frequency[3][2]=8.7;
		frequency2powerCap[3][2]=1200000;
		powerCap2frequency[3][3]=10.2;
		frequency2powerCap[3][3]=1400000;
		powerCap2frequency[3][4]=11.5;
		frequency2powerCap[3][4]=1600000;
		powerCap2frequency[3][5]=13.0;
		frequency2powerCap[3][5]=1800000;
		powerCap2frequency[3][6]=14.7;
		frequency2powerCap[3][6]=2000000;
		powerCap2frequency[3][7]=15.6;
		frequency2powerCap[3][7]=2100000;
		powerCap2frequency[3][8]=17.6;
		frequency2powerCap[3][8]=2300000;
		powerCap2frequency[3][9]=20.5;
		frequency2powerCap[3][9]=2500000;
		powerCap2frequency[3][10]=23.0;
		frequency2powerCap[3][10]=2700000;
		powerCap2frequency[3][11]=25.4;
		frequency2powerCap[3][11]=2900000;
		powerCap2frequency[3][12]=28.3;
		frequency2powerCap[3][12]=3100000;
		powerCap2frequency[3][13]=31.0;
		frequency2powerCap[3][13]=3300000;
		powerCap2frequency[3][14]=34.2;
		frequency2powerCap[3][14]=3500000;

		
		minPowerCap = powerCap2frequency[3][0]; // we guarantee that all cores can run, at least at min freq
		maxPowerCap = powerCap2frequency[3][14];
		
	}

	public static double getPower(int activeCores, int currentMaxFrequency) {
		
		activeCores = Math.max(0, activeCores-1);
		
		int idx = Arrays.binarySearch(frequency2powerCap[activeCores], currentMaxFrequency);
		if (idx < 0) idx = - idx - 1;
		idx = Math.min(idx, frequency2powerCap[activeCores].length - 1);
		return powerCap2frequency[activeCores][idx];
		
	}

	public static int getFrequency(int activeCores, double powerCap) {

		activeCores = Math.max(0, activeCores-1);
		
		int idx = Arrays.binarySearch(powerCap2frequency[activeCores], powerCap);
		if (idx < 0) idx = - idx - 1;
		idx = Math.min(idx, powerCap2frequency[activeCores].length - 1);
		return frequency2powerCap[activeCores][idx];
	}
	
}

/**
 * The Intel i7-4770K CPU
 * @author Matteo Catena
 *
 */
public class Intel_i7_4770K extends CPU implements RAPL {

	
	static final int[] frequencies = PowerModel.frequency2powerCap[0];
	static final int NUMCORES = 4; //we ignore hyperthreading

	/**
	 * The CPU powerCap
	 */
	protected double powerCap;

	private int currentMaxFrequency;
	private int activeCores;

	Intel_i7_4770K(ShardServer server) {

		super(server, NUMCORES);

		this.powerCap = PowerModel.maxPowerCap;

		this.currentMaxFrequency = getMinFrequency();
		this.activeCores = 0;
	}

	@Override
	protected Core getNewCoreInstance(CPU cpu) {

		return new Intel_i7_4770K_Core(this);
	}

	@Override
	protected void update(long timeMicroseconds) {

		int currentMaxFrequency = getCurrentMaxFrequency();
		int activeCores = activeCores();
		
		if (timeMicroseconds != statusChangeTime) {			
			
			double power = 0.0;
			if (this.activeCores == 0) {
				power = PowerModel.IDLE;
			} else {
				power = PowerModel.getPower(this.activeCores, this.currentMaxFrequency);
			}
			
			((OLDISimulator)  server.getSimulator()).updateEnergyConsumption(power, this.statusChangeTime, (timeMicroseconds - this.statusChangeTime) / 1000000.0);
		}
		
		// new status
		this.statusChangeTime = timeMicroseconds;
		this.activeCores = activeCores;
		this.currentMaxFrequency = currentMaxFrequency;

	}

	@Override
	public void shutdown(long timeMicroseconds) {

		// it basically forces a last print of the stats
		update(timeMicroseconds);

	}
	
	int getCurrentMaxFrequency() {

		int currentMaxFrequency = getMinFrequency();

		int activeCores = activeCores();
		final int maxPossibleFrequency = PowerModel.getFrequency(activeCores, powerCap);

		for (Core c : cores) {

			if (c.isBusy())
				currentMaxFrequency = Math.max(currentMaxFrequency,
						Math.min(((Intel_i7_4770K_Core) c).currentFrequency, maxPossibleFrequency));

		}
		return currentMaxFrequency;
	}

//	int getFrequency(int frequency) {
//
//		int activeCores = activeCores();
//		return Math.min(frequency, PowerModel.getFrequency(activeCores, powerCap));
//
//	}

	@Override
	public int getMinFrequency() {

		return frequencies[0];
	}

	@Override
	public int getMaxFrequency() {

		return frequencies[frequencies.length - 1];
	}

	@Override
	public int[] getFrequencies() {

		return frequencies;
	}

	@Override
	public double getPowerCap() {

		return powerCap;
	}

	@Override
	public void setMaxPowerCap(long timeMicroseconds) {

		setPowerCap(PowerModel.maxPowerCap, timeMicroseconds);
	}

	@Override
	public void setPowerCap(double powerCap, long timeMicroseconds) {

		double prevPowerCap = this.powerCap;
		if (powerCap < PowerModel.minPowerCap)
			powerCap = PowerModel.minPowerCap;
		else if (powerCap > PowerModel.maxPowerCap)
			powerCap = PowerModel.maxPowerCap;
		this.powerCap = powerCap;
		if (this.powerCap != prevPowerCap)
			update(timeMicroseconds);
	}

}
