package it.cnr.isti.hpclab.example.search.pesos;

import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.cpu.Core;
import it.cnr.isti.hpclab.engine.RequestProcessingThread;
import it.cnr.isti.hpclab.engine.Shard;


/**
 * A search engine-specific implementation of {@link it.cnr.isti.hpclab.engine.ShardServer}
 * for the PESOS algorithm [1].
 * 
 * [1] Catena and Tonellotto. 2017. Energy-Efficient Query Processing in Web Search Engines. TKDE.
 * 
 * @author Matteo Catena
 */
public class ShardServer extends it.cnr.isti.hpclab.engine.pptq.ShardServer {

	public ShardServer(IndexReplica replicaManager, Shard shard, CPUBuilder cpuBuilder, int id) {
		super(replicaManager, shard, cpuBuilder, id);

	}
	@Override
	protected RequestProcessingThread newRequestProcessingThreadInstance(Core core) {
		
		return new QueryMatcher(this, core);
	}
	
	

}