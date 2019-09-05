package it.cnr.isti.hpclab.example.search.pegasus;

import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.engine.RequestBroker;
import it.cnr.isti.hpclab.engine.Shard;
import it.cnr.isti.hpclab.engine.ShardServer;

/**
 * A search engine-specific implementation of {@link it.cnr.isti.hpclab.engine.pdrq.DatastoreReplica}
 * for the PEGASUS algorithm [1].
 * 
 * [1] Lo et al. 2014. Towards energy proportionality for large-scale latency-critical workloads. ISCA '14.
 * 
 * @author Matteo Catena
 *
 */
public class IndexReplica extends it.cnr.isti.hpclab.engine.pdrq.DatastoreReplica {

	public IndexReplica(RequestBroker broker, CPUBuilder cpuBuilder, int id, Shard... shards) {
		super(broker, cpuBuilder, id, shards);
	}

	/**
	 * 
	 * Set the CPU power cap to its maximum available value
	 * 
	 * @param timeMicroseconds the time at which the change take place
	 */
	public void setMaxCPUPowerCap(long timeMicroseconds) {

		for (ShardServer s : servers)
			((it.cnr.isti.hpclab.example.search.pegasus.ShardServer)s).setMaxCPUPowerCap(timeMicroseconds);
		
	}
	
	@Override
	protected ShardServer newShardServerInstance(Shard shard, CPUBuilder cpuBuilder, int id) {
		
		return new it.cnr.isti.hpclab.example.search.pegasus.ShardServer(this, shard, cpuBuilder, id);
	}

	/**
	 * Multiply and update the current CPU power cap by a factor {@code d}. 
	 * @param d The multiplying factor
	 * @param timeMicroseconds the time at which the change take place
	 */
	public void multiplyCPUPowerCapBy(double d, long timeMicroseconds) {
		
		for (ShardServer s : servers)
			((it.cnr.isti.hpclab.example.search.pegasus.ShardServer)s).multiplyCPUPowerCapBy(d, timeMicroseconds);		
	}

}