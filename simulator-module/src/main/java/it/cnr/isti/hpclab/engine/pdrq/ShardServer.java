package it.cnr.isti.hpclab.engine.pdrq;

import java.util.Collections;

import eu.nicecode.queueing.Request;
import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.cpu.Core;
import it.cnr.isti.hpclab.engine.DatastoreReplica;
import it.cnr.isti.hpclab.engine.RequestProcessingThread;
import it.cnr.isti.hpclab.engine.Shard;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * A specialisation of {@link it.cnr.isti.hpclab.engine.ShardServer}, 
 * whose incoming requests are enqueued at datastore replicas (pdrq stands for 
 * [p]er-[d]atastore-[r]eplica-[q]ueue).
 * @author Matteo Catena
 *
 */
public class ShardServer extends it.cnr.isti.hpclab.engine.ShardServer {

	public ShardServer(DatastoreReplica replicaManager, Shard shard, CPUBuilder cpuBuilder, int id) {
		super(replicaManager, shard, cpuBuilder, id);
	}

	@Override
	protected RequestProcessingThread newRequestProcessingThreadInstance(Core core) {
		
		return new it.cnr.isti.hpclab.engine.pdrq.RequestProcessingThread(this, core);
	}
	
	/**
	 * Fetch the next request to be processed
	 * @return
	 */
	public Request getNextRequest() {
		
		return ((it.cnr.isti.hpclab.engine.pdrq.DatastoreReplica)replica).getNextRequest(this.getId());
	}

	boolean hasIdlingResources() {
		
		for (RequestProcessingThread m : threads) {
			
			it.cnr.isti.hpclab.engine.pdrq.RequestProcessingThread m1 = (it.cnr.isti.hpclab.engine.pdrq.RequestProcessingThread) m;
			if (m1.isIdle()) return true;
			
		}
		return false;		
		
	}

	@Override
	public void receiveRequest(Request request) {
		
		IntList slackingMatcher = new IntArrayList(threads.length);
		for (int i = 0; i < threads.length; i++) if (((it.cnr.isti.hpclab.engine.pdrq.RequestProcessingThread) threads[i]).isIdle()) slackingMatcher.add(i);
		
		if (slackingMatcher.size() > 1) {
			
			Collections.shuffle(slackingMatcher);
			
		}
		threads[slackingMatcher.getInt(0)].processRequest(request, getSimulator());		
	}

	@Override
	public int getLoad() {
		
		int cnt = 0;
		for (RequestProcessingThread m : threads) if (!((it.cnr.isti.hpclab.engine.pdrq.RequestProcessingThread) m).isIdle()) cnt++;
		return cnt;
		
	}

}
