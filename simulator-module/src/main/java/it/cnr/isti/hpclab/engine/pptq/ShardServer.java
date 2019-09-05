package it.cnr.isti.hpclab.engine.pptq;

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
 * whose incoming requests are enqueued directly at processing threads (pptq stands for 
 * [p]er-[p]rocessing-[t]hread-[q]ueue).
 * @author Matteo Catena
 *
 */
public class ShardServer extends it.cnr.isti.hpclab.engine.ShardServer {

	public ShardServer(DatastoreReplica replicaManager, Shard shard, CPUBuilder cpuBuilder, int id) {
		super(replicaManager, shard, cpuBuilder, id);
	}

	@Override
	protected RequestProcessingThread newRequestProcessingThreadInstance(Core core) {
	
		return new it.cnr.isti.hpclab.engine.pptq.RequestProcessingThread(this, core);
	}
	
	@Override
	public void receiveRequest(Request request) {

		IntList leastLoadedMatcherList = new IntArrayList(threads.length);
		int load = Integer.MAX_VALUE;
		for (int i = 0; i < threads.length; i++) {
			
			int mLoad = ((it.cnr.isti.hpclab.engine.pptq.RequestProcessingThread)threads[i]).getLoad();
			if (mLoad <= load) {
				
				if (mLoad < load) {
					load = mLoad;
					leastLoadedMatcherList.clear();
				}
				leastLoadedMatcherList.add(i);
			}			
		}
		
		if (leastLoadedMatcherList.size() > 1) {

			Collections.shuffle(leastLoadedMatcherList);
		}
		
		threads[leastLoadedMatcherList.getInt(0)].receiveRequest(request, null, this.getSimulator());		
	}

	@Override
	public int getLoad() {
		
		int load = 0;
		for (RequestProcessingThread qm : threads)
			load += ((it.cnr.isti.hpclab.engine.pptq.RequestProcessingThread) qm).getLoad();
		return load;
		
	}

	Request stealRequest() {
			
		IntList mostLoadedMatcherList = new IntArrayList(threads.length);
		int load = Integer.MIN_VALUE;
		for (int i = 0; i < threads.length; i++) {
			
			int mLoad = ((it.cnr.isti.hpclab.engine.pptq.RequestProcessingThread)threads[i]).getLoad();
			if (mLoad > 1 && mLoad >= load) {
				
				if (mLoad > load) {
					load = mLoad;
					mostLoadedMatcherList.clear();
				}
				mostLoadedMatcherList.add(i);
			}			
		}
		
		if (!mostLoadedMatcherList.isEmpty()) {
			
			if (mostLoadedMatcherList.size() > 1) {
				
				Collections.shuffle(mostLoadedMatcherList);
			}
			
			return ((it.cnr.isti.hpclab.engine.pptq.RequestProcessingThread)threads[mostLoadedMatcherList.getInt(0)]).dequeueRequest();
			
		} else {
			
			return null;
		}
	}

}
