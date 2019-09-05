package it.cnr.isti.hpclab.example.search.pegasus;

import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.cpu.RAPL;
import it.cnr.isti.hpclab.engine.Shard;

/**
 * A search engine-specific implementation of {@link it.cnr.isti.hpclab.engine.ShardServer}
 * for the PEGASUS algorithm [1].
 * 
 * [1] Lo et al. 2014. Towards energy proportionality for large-scale latency-critical workloads. ISCA '14.
 * 
 * @author Matteo Catena
 *
 */
public class ShardServer extends it.cnr.isti.hpclab.engine.pdrq.ShardServer {

	public ShardServer(IndexReplica replicaManager, Shard shard, CPUBuilder cpuModel, int id) {
		super(replicaManager, shard, cpuModel, id);
	}

	/**
	 * 
	 * Set the CPU power cap to its maximum available value
	 * 
	 * @param timeMicroseconds the time at which the change take place
	 */
	public void setMaxCPUPowerCap(long timeMicroseconds) {

		((RAPL)cpu).setMaxPowerCap(timeMicroseconds);
	}

	/**
	 * Multiply and update the current CPU power cap by a factor {@code d}. 
	 * @param d The multiplying factor
	 * @param timeMicroseconds the time at which the change take place
	 */
	public void multiplyCPUPowerCapBy(double d, long timeMicroseconds) {

		((RAPL)cpu).setPowerCap(d * ((RAPL)cpu).getPowerCap(), timeMicroseconds);
	}

}