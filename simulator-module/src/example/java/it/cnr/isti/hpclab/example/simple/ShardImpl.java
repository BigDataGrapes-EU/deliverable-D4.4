package it.cnr.isti.hpclab.example.simple;

import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import eu.nicecode.simulator.Time;
import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.engine.Shard;

/**
 * This simple Shard implementation can be used to determine the service time which is
 * require to complete a request. Service times are drawn from an exponential distribution
 * given a mean service time.
 * 
 * @author Matteo Catena
 *
 */
public class ShardImpl extends Shard {

	private int maxFreq;
	
	private ExponentialDistribution expDistribution;
	
	/**
	 * 
	 * @param cpuModel the cpu model used by the shard servers
	 * @param meanServTimeMillis the mean service time required to complete a request
	 */
	public ShardImpl(CPUBuilder cpuModel, int meanServTimeMillis) {
		
		maxFreq=Integer.MIN_VALUE;
		for (int f : cpuModel.getFrequencies()) maxFreq=Math.max(maxFreq, f);
		expDistribution = new ExponentialDistribution(meanServTimeMillis);
		
	}
	
	@Override
	public Time getServiceTime(long rid, int frequency) {

		expDistribution.reseedRandomGenerator(rid + Short.MAX_VALUE);
		
		double minServiceTime = expDistribution.sample();		
		long serviceTime = (long) ((maxFreq * minServiceTime) / ((double)frequency));
		
		return new Time(serviceTime, TimeUnit.MILLISECONDS);
	}

}
