package it.cnr.isti.hpclab.engine.pptq;

import java.util.LinkedList;
import java.util.Queue;

import eu.nicecode.queueing.Request;
import eu.nicecode.simulator.Agent;
import eu.nicecode.simulator.Simulator;
import it.cnr.isti.hpclab.cpu.Core;
import it.cnr.isti.hpclab.engine.ShardServer;

/**
 * A specialisation of {@link it.cnr.isti.hpclab.engine.RequestProcessingThread}, 
 * whose incoming requests are enqueued directly at processing threads (pptq stands for 
 * [p]er-[p]rocessing-[t]hread-[q]ueue).
 * @author Matteo Catena
 *
 */
public class RequestProcessingThread extends it.cnr.isti.hpclab.engine.RequestProcessingThread {

	protected Queue<Request> queue;
	
	public RequestProcessingThread(ShardServer shardServer, Core core) {
		super(shardServer, core);
		this.queue = new LinkedList<>();
	}

	@Override
	public Request nextRequest() {
		return null;
	}

	@Override
	public void sendRequest(Request request, Agent to, Simulator simulator) {
		// do nothing
	}

	@Override
	public void receiveRequest(Request request, Agent from, Simulator simulator) {
			
		if (!core.isBusy() && queue.isEmpty()) {

			processRequest(request, simulator);
			
		} else {
			
			queue.offer(request);
		}
	}

	@Override
	public void afterRequestCompletion(Request request, Simulator simulator) {

		if (!queue.isEmpty()) {
			
			processRequest(queue.poll(), simulator);

		} else {
			
			Request q = ((it.cnr.isti.hpclab.engine.pptq.ShardServer) server).stealRequest();
			if (q != null) {
				
				processRequest(q, simulator);
				
			} else {
				
				core.free(simulator.now().getTimeMicroseconds());
				
			}
		}
	}

	/**
	 * Get the load of this thread (i.e., number of requests to process)
	 * @return the number of requests to process
	 */
	public int getLoad() {
		
		return core.isBusy() ? queue.size() + 1 : queue.size();
	}

	/**
	 * Remove a request from the queue
	 * @return the dequeued request
	 */
	protected Request dequeueRequest() {
		
		return queue.poll();
	}

}
