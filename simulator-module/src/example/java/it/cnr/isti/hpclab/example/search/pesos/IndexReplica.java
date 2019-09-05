package it.cnr.isti.hpclab.example.search.pesos;

import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.engine.Shard;
import it.cnr.isti.hpclab.engine.ShardServer;
import it.cnr.isti.hpclab.engine.pptq.DatastoreReplica;

/**
 * A search engine-specific implementation of {@link it.cnr.isti.hpclab.engine.pptq.DatastoreReplica}
 * for the PESOS algorithm [1].
 * 
 * [1] Catena and Tonellotto. 2017. Energy-Efficient Query Processing in Web Search Engines. TKDE.
 * 
 * @author Matteo Catena
 */
public class IndexReplica extends DatastoreReplica {

	public IndexReplica(QueryBroker broker, CPUBuilder cpuBuilder, int id, Shard... shards) {
		super(broker, cpuBuilder, id, shards);
	}

	@Override
	protected ShardServer newShardServerInstance(Shard shard, CPUBuilder cpuBuilder, int id) {

		return new it.cnr.isti.hpclab.example.search.pesos.ShardServer(this, shard, cpuBuilder, id);
	}
	
	

}