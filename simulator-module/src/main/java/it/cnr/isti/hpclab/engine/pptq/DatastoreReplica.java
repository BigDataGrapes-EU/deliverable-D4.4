package it.cnr.isti.hpclab.engine.pptq;

import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.engine.RequestBroker;
import it.cnr.isti.hpclab.engine.Shard;
import it.cnr.isti.hpclab.engine.ShardServer;
import it.cnr.isti.hpclab.request.Request;

/**
 * A specialisation of {@link it.cnr.isti.hpclab.engine.DatastoreReplica}, 
 * whose incoming requests are enqueued directly at processing threads (pptq stands for 
 * [p]er-[p]rocessing-[t]hread-[q]ueue).
 * @author Matteo Catena
 *
 */
public class DatastoreReplica extends it.cnr.isti.hpclab.engine.DatastoreReplica {

	public DatastoreReplica(RequestBroker broker, CPUBuilder cpuBuilder, int id, Shard[] shards) {
		super(broker, cpuBuilder, id, shards);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ShardServer newShardServerInstance(Shard shard, CPUBuilder cpuBuilder, int id) {
		
		return new it.cnr.isti.hpclab.engine.pptq.ShardServer(this, shard, cpuBuilder, id);
	}

	@Override
	public void receiveRequest(Request request) {

		for (ShardServer s : servers) {
			
			it.cnr.isti.hpclab.engine.pptq.ShardServer s1 = (it.cnr.isti.hpclab.engine.pptq.ShardServer) s;
			s1.receiveRequest(request);
			
		}
		
	}

	@Override
	public int getLoad() {
		
		int load = 0;
		for (ShardServer s : servers)
			load += ((it.cnr.isti.hpclab.engine.pptq.ShardServer)s).getLoad();
		return load;
	}
}
