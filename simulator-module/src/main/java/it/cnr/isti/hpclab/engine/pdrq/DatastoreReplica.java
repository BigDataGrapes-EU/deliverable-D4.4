package it.cnr.isti.hpclab.engine.pdrq;

import java.util.LinkedList;
import java.util.Queue;

import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.engine.RequestBroker;
import it.cnr.isti.hpclab.engine.Shard;
import it.cnr.isti.hpclab.engine.ShardServer;
import it.cnr.isti.hpclab.request.Request;

/**
 * A specialisation of {@link it.cnr.isti.hpclab.engine.DatastoreReplica}, 
 * whose incoming requests are enqueued at datastore replicas (pdrq stands for 
 * [p]er-[d]atastore-[r]eplica-[q]ueue).
 * @author Matteo Catena
 *
 */
public class DatastoreReplica extends it.cnr.isti.hpclab.engine.DatastoreReplica {

	/**
	 * The request queue, requests waiting to be processed
	 */
	protected Queue<Request>[] queues;
	
	@SuppressWarnings("unchecked")
	public DatastoreReplica(RequestBroker broker, CPUBuilder cpuBuilder, int id, Shard[] shards) {
		super(broker, cpuBuilder, id, shards);
		queues = new Queue[shards.length];
		for (int i = 0; i < shards.length; i++) {
			queues[i] = new LinkedList<>();
		}
	}

	@Override
	protected ShardServer newShardServerInstance(Shard shard, CPUBuilder cpuBuilder, int id) {
		
		return new it.cnr.isti.hpclab.engine.pdrq.ShardServer(this, shard, cpuBuilder, id);
	}

	/**
	 * Pop the next request to be processed by the shard server identified by
	 * {@code shardServerId}
	 * @param shardServerId The identifier of the server requiring the next request to be processed
	 * @return
	 */
	public Request getNextRequest(int shardServerId) {
		
		return queues[shardServerId].poll(); 

	}

	@Override
	public void receiveRequest(Request request) {

		for (int i = 0; i < servers.length; i++) {
			
			it.cnr.isti.hpclab.engine.pdrq.ShardServer s1 = (it.cnr.isti.hpclab.engine.pdrq.ShardServer) servers[i];
			if (s1.hasIdlingResources()) {
				s1.receiveRequest((Request) request);
			} else {
				queues[i].offer(request);
			}			
		}
	}

	boolean hasIdlingResources() {

		for (ShardServer s : servers) {
			
			it.cnr.isti.hpclab.engine.pdrq.ShardServer s1 = (it.cnr.isti.hpclab.engine.pdrq.ShardServer) s;
			if (s1.hasIdlingResources()) return true;
			
		}
		return false;	
		
	}

	@Override
	public int getLoad() {
	
		int cnt = 0;
		for (int i = 0; i < servers.length; i++) {
			
			cnt += queues[i].size();
			cnt += servers[i].getLoad();
		}
		return cnt;
	}
}
